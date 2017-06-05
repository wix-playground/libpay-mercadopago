package com.wix.pay.mercadopago

import com.google.api.client.http._
import com.wix.pay.creditcard.CreditCard
import com.wix.pay.mercadopago.model.{ErrorCodes, ErrorResponse, Errors, Statuses}
import com.wix.pay.model.{CurrencyAmount, Customer, Deal, Payment}
import com.wix.pay.{PaymentErrorException, PaymentException, PaymentGateway, PaymentRejectedException}

import scala.concurrent.duration.Duration
import scala.util.Try


class MercadopagoGateway(requestFactory: HttpRequestFactory,
                         connectTimeout: Option[Duration] = None,
                         readTimeout: Option[Duration] = None,
                         numberOfRetries: Int = 0,
                         defaultEmail: String = "example@example.org",
                         oauthEndpointUrl: String = OauthEndpoint.production,
                         tokenizationEndpointUrl: String = TokenizationEndpoint.production,
                         paymentsEndpointUrl: String = PaymentsEndpoint.production,
                         merchantParser: MercadopagoMerchantParser = new JsonMercadopagoMerchantParser,
                         authorizationParser: MercadopagoAuthorizationParser = new JsonMercadopagoAuthorizationParser)
  extends MercadoPagoHttpExecutor(requestFactory, connectTimeout, readTimeout, numberOfRetries)
  with PaymentGateway {

  private val accessTokenRetriever = new AccessTokenRetriever(
    requestFactory,
    connectTimeout,
    readTimeout,
    numberOfRetries = 3,
    oauthEndpointUrl)

  override def authorize(merchantKey: String, creditCard: CreditCard, payment: Payment, customer: Option[Customer], deal: Option[Deal]): Try[String] = {
    Try {
      throw PaymentErrorException("MercadoPago does not support two-step payments")
    }
  }

  override def capture(merchantKey: String, authorizationKey: String, amount: Double): Try[String] = {
    Try {
      throw PaymentErrorException("MercadoPago does not support two-step payments")
    }
  }

  override def sale(merchantKey: String, creditCard: CreditCard, payment: Payment, customer: Option[Customer], deal: Option[Deal]): Try[String] = {
    Try {
      require(deal.isDefined, "Deal is mandatory for MercadoPago")
      require(deal.get.title.isDefined, "Deal title is mandatory for MercadoPago")
      require(creditCard.holderName.isDefined, "Card holder name is mandatory for MercadoPago")
      require(creditCard.holderId.isDefined, "Card holder ID is mandatory for MercadoPago")
      require(creditCard.csc.isDefined, "CSC is mandatory for MercadoPago")
      require(payment.installments == 1, "MercadoPago does not support installments")

      val merchant = merchantParser.parse(merchantKey)

      val accessToken = accessTokenRetriever.retrieveAccessToken(
        merchant.clientId,
        merchant.clientSecret
      ).get

      val cardTokenId = tokenize(
        accessToken = accessToken,
        creditCard = creditCard,
        countryCode = merchant.countryCode
      ).get

      createPayment(
        accessToken,
        payment.currencyAmount,
        dealId = deal.get.id,
        dealTitle = deal.get.title.get,
        cardTokenId,
        paymentMethodId = MercadopagoHelper.toPaymentMethodId(creditCard),
        customerEmail = getMail(customer),
        sponsorId = MercadopagoHelper.getSponsorId(merchant.countryCode)
      ).get
    }
  }

  private def getMail(customer: Option[Customer]): String =
    if (customer.isDefined && customer.get.email.isDefined) customer.get.email.get else defaultEmail

  override def voidAuthorization(merchantKey: String, authorizationKey: String): Try[String] = {
    Try {
      throw PaymentErrorException("MercadoPago does not support two-step payments")
    }
  }

  private def createPayment(accessToken: String,
                            currencyAmount: CurrencyAmount,
                            dealId: String,
                            dealTitle: String,
                            cardTokenId: String,
                            paymentMethodId: Option[String],
                            customerEmail: String,
                            sponsorId: Int): Try[String] = {
    Try {
      val request = MercadopagoHelper.createCreatePaymentRequest(
        currencyAmount = currencyAmount,
        dealId = dealId,
        dealTitle = dealTitle,
        cardTokenId = cardTokenId,
        paymentMethodId = paymentMethodId,
        customerEmail = customerEmail,
        sponsorId = sponsorId
      )

      val requestJson = CreatePaymentRequestParser.stringify(request)
      val httpResponse = executeHttpPostRequest(paymentsEndpointUrl, accessToken, requestJson)
      val responseJson = extractResponseAndClose(httpResponse)
      if (httpResponse.isSuccessStatusCode) {
        val response = CreatePaymentResponseParser.parse(responseJson)

        response.status match {
          case Statuses.approved => response.payment_id
          case Statuses.rejected => throw PaymentRejectedException(response.status_detail)
          case otherStatus => throw PaymentErrorException(response.status_detail)
        }
      } else if (responseJson.length > 0) {
        val errorResponse = ErrorResponseParser.parse(responseJson)
        throw MercadopagoGateway.translateError(errorResponse)
      } else {
        // MercadoPago sometimes returns an empty body, e.g. on 401 "No Autorizado".
        // According to their engineers, the merchant needs to fill the form "Eu quero ir para produção" located
        // at https://www.mercadopago.com/mlb/account/credentials
        throw PaymentErrorException(s"${httpResponse.getStatusCode}|${httpResponse.getStatusMessage}")
      }
    }
  }

  private def tokenize(accessToken: String, creditCard: CreditCard, countryCode: String): Try[String] = {
    Try {
      val request = MercadopagoHelper.createTokenizeRequest(creditCard = creditCard, countryCode = countryCode)
      val requestJson = TokenizeRequestParser.stringify(request)

      val httpResponse = executeHttpPostRequest(tokenizationEndpointUrl, accessToken, requestJson)

      val responseJson = extractResponseAndClose(httpResponse)
      if (httpResponse.isSuccessStatusCode) {
        val response = TokenizeResponseParser.parse(responseJson)
        response.id
      } else {
        val errorResponse = ErrorResponseParser.parse(responseJson)
        throw MercadopagoGateway.translateError(errorResponse)
      }
    }
  }
}

object MercadopagoGateway {
  val ID = "com.mercadopago"

  private def translateError(errorResponse: ErrorResponse): PaymentException = {
    if ((errorResponse.error == Errors.badRequest) && (errorResponse.cause.length == 1) &&
      ErrorCodes.paymentRejectedBadRequestErrorCodes.contains(errorResponse.cause.head.code)) {

      PaymentRejectedException(errorResponse.toString)
    } else {
      PaymentErrorException(errorResponse.toString)
    }
  }
}
