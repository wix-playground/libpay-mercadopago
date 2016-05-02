package com.wix.pay.mercadopago.it

import com.google.api.client.http.javanet.NetHttpTransport
import com.wix.pay.creditcard.{CreditCard, CreditCardOptionalFields, YearMonth}
import com.wix.pay.mercadopago._
import com.wix.pay.mercadopago.model._
import com.wix.pay.mercadopago.testkit.{MercadopagoOauthDriver, MercadopagoPaymentsDriver, MercadopagoTokenizationDriver}
import com.wix.pay.model.{CurrencyAmount, Customer, Deal}
import com.wix.pay.{PaymentErrorException, PaymentGateway, PaymentRejectedException}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class MercadopagoGatewayIT extends SpecWithJUnit {
  val mercadopagoOauthPort = 10015
  val mercadopagoTokenizationPort = 10016
  val mercadopagoPaymentsPort = 10017

  val requestFactory = new NetHttpTransport().createRequestFactory()
  val oauthDriver = new MercadopagoOauthDriver(port = mercadopagoOauthPort)
  val tokenizationDriver = new MercadopagoTokenizationDriver(port = mercadopagoTokenizationPort)
  val paymentsDriver = new MercadopagoPaymentsDriver(port = mercadopagoPaymentsPort)

  step {
    oauthDriver.startProbe()
    tokenizationDriver.startProbe()
    paymentsDriver.startProbe()
  }

  sequential

  trait Ctx extends Scope {
    val merchantParser = new JsonMercadopagoMerchantParser()
    val authorizationParser = new JsonMercadopagoAuthorizationParser()

    val someCountryCode = "BR"
    val someMerchant = MercadopagoMerchant(
      clientId = "some client ID",
      clientSecret = "some client secret",
      countryCode = someCountryCode
    )
    val merchantKey = merchantParser.stringify(someMerchant)

    val someCreditCard = CreditCard(
      number = "4012888818888",
      expiration = YearMonth(
        year = 2020,
        month = 1
      ),
      additionalFields = Some(CreditCardOptionalFields.withFields(
        csc = Some("123"),
        holderName = Some("Johninio Smitharez"),
          holderId = Some("12345678")

      ))
    )

    val someCurrencyAmount = CurrencyAmount(
      currency = "BRL",
      amount = 33.3
    )

    val someDeal = Deal(
      id = System.currentTimeMillis().toString,
      title = Some("some deal title"),
      description = Some("some deal description")
    )

    val someCustomer = Customer(
      email = Some("example@example.org")
    )

    def anOauthRequest(): Map[String, String] = {
      MercadopagoHelper.createOauthRequest(
        clientId = someMerchant.clientId,
        clientSecret = someMerchant.clientSecret
      )
    }

    def anOauthErrorResponse(errorMessage: String): OauthErrorResponse = {
      OauthErrorResponse(
        message = errorMessage,
        error = "some error",
        status = 666
      )
    }

    val someAccessToken = "some access token"
    def aSuccessfulOauthResponse(): OauthResponse = {
      OauthResponse(
        access_token = someAccessToken,
        token_type = "some token type",
        expires_in = 1000,
        scope = "some scope",
        refresh_token = "some refresh token"
      )
    }

    def aTokenizeRequest(): TokenizeRequest = {
      MercadopagoHelper.createTokenizeRequest(someCreditCard, someCountryCode)
    }

    def anErrorResponse(errorMessage: String): ErrorResponse = {
      ErrorResponse(
        error = "some error",
        message = errorMessage,
        cause = List()
      )
    }

    val someCardTokenId = "some token ID"
    def aSuccessfulTokenizationResponse(): TokenizeResponse = {
      TokenizeResponse(
        public_key = "some public key",
        card_id = "some card ID",
        luhn_validation = true,
        status = "some status",
        used_date = "some user date",
        live_mode = true,
        card_number_length = 16,
        id = someCardTokenId,
        creation_date = "some creation date",
        trunc_card_number = "1234",
        security_code_length = Some(3),
        expiration_year = 2020,
        expiration_month = 12,
        last_modified_date = "some last modified date",
        cardholder = CardHolder(
          name = "some cardholder name",
          identification = Identification(
            subtype = "some subtype",
            number = "some number",
            `type` = IdentificationTypes.cpf
          )
        ),
        due_date = "some due date"
      )
    }

    def aCreatePaymentRequest(): CreatePaymentRequest = {
      MercadopagoHelper.createCreatePaymentRequest(
        currencyAmount = someCurrencyAmount,
        dealId = someDeal.id,
        dealTitle = someDeal.title.get,
        cardTokenId = someCardTokenId,
        paymentMethodId = MercadopagoHelper.toPaymentMethodId(someCreditCard),
        customerEmail = someCustomer.email.get
      )
    }

    def anApprovedPaymentResponse(): CreatePaymentResponse = {
      aCreatePaymentResponse(
        status = Statuses.approved,
        statusDetail = "some status detail"
      )
    }

    def aRejectedPaymentResponse(message: String): CreatePaymentResponse = {
      aCreatePaymentResponse(
        status = Statuses.rejected,
        statusDetail = message
      )
    }

    def anInvalidCardNumberLengthErrorResponse(): ErrorResponse = {
      ErrorResponse(
        error = Errors.badRequest,
        message = "Invalid card_number_length",
        cause = List(Error(
          code = ErrorCodes.invalidCardNumberLength,
          description = "Invalid card_number_length"
        ))
      )
    }

    private def aCreatePaymentResponse(status: String, statusDetail: String): CreatePaymentResponse = {
      CreatePaymentResponse(
        payment_id = somePaymentId,
        status = status,
        status_code = Some("some status code"),
        status_detail = statusDetail,
        amount = someCurrencyAmount.amount,
        installments = 1,
        total_paid_amount = someCurrencyAmount.amount,
        finance_charge = 0,
        marketplace_fee = Some(0),
        currency_id = someCurrencyAmount.currency,
        payment_method_id = PaymentMethodIds.visa,
        card_issuer_id = 1234,
        payment_type = PaymentTypes.creditcard,
        operation_type = "some operation type",
        external_reference = "some external reference",
        payer_email = someCustomer.email.get,
        reason = "some reason",
        card_token_id = someCardTokenId,
        statement_descriptor = "some statement descriptor"
      )
    }

    val somePaymentId = "some payment ID"

    val mercadopago: PaymentGateway = new MercadopagoGateway(
      requestFactory = requestFactory,
      oauthEndpointUrl = s"http://localhost:$mercadopagoOauthPort/",
      tokenizationEndpointUrl = s"http://localhost:$mercadopagoTokenizationPort/",
      paymentsEndpointUrl = s"http://localhost:$mercadopagoPaymentsPort/",
      merchantParser = merchantParser,
      authorizationParser = authorizationParser)

    oauthDriver.resetProbe()
    tokenizationDriver.resetProbe()
    paymentsDriver.resetProbe()
  }

  "sale request via MercadoPago gateway" should {
    "gracefully fail on invalid merchant" in new Ctx {
      val errorMessage = "some error message"
      oauthDriver.aRequestFor(anOauthRequest()) errors anOauthErrorResponse(errorMessage)

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must beEqualTo(errorMessage)
      }
    }

    "gracefully fail when tokenization fails" in new Ctx {
      oauthDriver.aRequestFor(anOauthRequest()) returns aSuccessfulOauthResponse()
      val errorMessage = "some error message"
      tokenizationDriver.aRequestFor(aTokenizeRequest()) errors anErrorResponse(errorMessage)

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must beEqualTo(errorMessage)
      }
    }

    "gracefully fail when creating a payment fails" in new Ctx {
      oauthDriver.aRequestFor(anOauthRequest()) returns aSuccessfulOauthResponse()
      tokenizationDriver.aRequestFor(aTokenizeRequest()) returns aSuccessfulTokenizationResponse()
      val errorMessage = "some error message"
      paymentsDriver.aRequestFor(aCreatePaymentRequest()) errors anErrorResponse(errorMessage)

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must beEqualTo(errorMessage)
      }
    }

    "gracefully fail when the user is not authorized" in new Ctx {
      oauthDriver.aRequestFor(anOauthRequest()) returns aSuccessfulOauthResponse()
      tokenizationDriver.aRequestFor(aTokenizeRequest()) returns aSuccessfulTokenizationResponse()
      paymentsDriver.aRequestFor(aCreatePaymentRequest()) failsUnauthorized()

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must contain("401")
      }
    }

    "gracefully fail when a payment is rejected" in new Ctx {
      oauthDriver.aRequestFor(anOauthRequest()) returns aSuccessfulOauthResponse()
      tokenizationDriver.aRequestFor(aTokenizeRequest()) returns aSuccessfulTokenizationResponse()
      val errorMessage = "some error message"
      paymentsDriver.aRequestFor(aCreatePaymentRequest()) returns aRejectedPaymentResponse(errorMessage)

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must beEqualTo(errorMessage)
      }
    }

    "gracefully fail when a payment is rejected due solely to invalid card number length" in new Ctx {
      oauthDriver.aRequestFor(anOauthRequest()) returns aSuccessfulOauthResponse()
      tokenizationDriver.aRequestFor(aTokenizeRequest()) returns aSuccessfulTokenizationResponse()
      paymentsDriver.aRequestFor(aCreatePaymentRequest()) errors anInvalidCardNumberLengthErrorResponse()

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry(
        check = beAnInstanceOf[PaymentRejectedException]
      )
    }

    "successfully yield a transaction ID on valid request" in new Ctx {
      oauthDriver.aRequestFor(anOauthRequest()) returns aSuccessfulOauthResponse()
      tokenizationDriver.aRequestFor(aTokenizeRequest()) returns aSuccessfulTokenizationResponse()
      paymentsDriver.aRequestFor(aCreatePaymentRequest()) returns anApprovedPaymentResponse()

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beASuccessfulTry(
        check = ===(somePaymentId)
      )
    }
  }

  step {
    oauthDriver.stopProbe()
    tokenizationDriver.stopProbe()
    paymentsDriver.stopProbe()
  }
}
