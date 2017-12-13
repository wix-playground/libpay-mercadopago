package com.wix.pay.mercadopago.testkit


import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import com.wix.e2e.http.api.StubWebServer
import com.wix.e2e.http.client.extractors.HttpMessageExtractors._
import com.wix.e2e.http.server.WebServerFactory.aStubWebServer
import com.wix.pay.creditcard.CreditCard
import com.wix.pay.mercadopago.model._
import com.wix.pay.mercadopago.{CreatePaymentRequestParser, CreatePaymentResponseParser, ErrorResponseParser, MercadopagoHelper}
import com.wix.pay.model.{CurrencyAmount, Customer, Deal}


class MercadopagoPaymentsDriver(port: Int) {
  private val server: StubWebServer = aStubWebServer.onPort(port).build

  def start(): Unit = server.start()
  def stop(): Unit = server.stop()
  def reset(): Unit = server.replaceWith()


  def aCreatePaymentFor(card: CreditCard,
                        cardTokenId: String,
                        currencyAmount: CurrencyAmount,
                        customer: Customer,
                        deal: Deal): RequestCtx = {
    new RequestCtx(
      card = card,
      cardTokenId = cardTokenId,
      currencyAmount = currencyAmount,
      customer = customer,
      deal = deal)
  }

  class RequestCtx(card: CreditCard,
                   cardTokenId: String,
                   currencyAmount: CurrencyAmount,
                   customer: Customer,
                   deal: Deal) {
    private val request = MercadopagoHelper.createCreatePaymentRequest(
      currencyAmount = currencyAmount,
      dealId = deal.id,
      dealTitle = deal.title.get,
      cardTokenId = cardTokenId,
      paymentMethodId = MercadopagoHelper.toPaymentMethodId(card),
      customerEmail = customer.email.get)

    def returns(transactionId: String): Unit = {
      returns(
        status = Statuses.approved,
        statusDetail = "some status detail",
        transactionId = transactionId)
    }

    def failsWith(errorMessage: String): Unit = {
      errors(ErrorResponse(
        error = "some error",
        message = errorMessage,
        cause = List()))
    }

    def failsOnInvalidCardNumberLength(): Unit = {
      errors(ErrorResponse(
        error = Errors.badRequest,
        message = "Invalid card_number_length",
        cause = List(Error(
          code = ErrorCodes.invalidCardNumberLength,
          description = "Invalid card_number_length"))))
    }

    def getsRejected(transactionId: String, message: String): Unit = {
      returns(
        status = Statuses.rejected,
        statusDetail = message,
        transactionId = transactionId)
    }

    def returns(status: String, statusDetail: String, transactionId: String): Unit = {
      returns(CreatePaymentResponse(
        payment_id = transactionId,
        status = status,
        status_code = Some("some status code"),
        status_detail = statusDetail,
        amount = currencyAmount.amount,
        installments = 1,
        total_paid_amount = currencyAmount.amount,
        finance_charge = 0,
        marketplace_fee = Some(0),
        currency_id = currencyAmount.currency,
        payment_method_id = PaymentMethodIds.visa,
        card_issuer_id = 1234,
        payment_type = PaymentTypes.creditcard,
        operation_type = "some operation type",
        external_reference = "some external reference",
        payer_email = customer.email.get,
        reason = "some reason",
        card_token_id = cardTokenId,
        statement_descriptor = Some("some statement descriptor")))
    }

    def returns(response: CreatePaymentResponse): Unit = {
      server.appendAll {
        case HttpRequest(
          HttpMethods.POST,
          Path("/"),
          _,
          entity,
          _) if isStubbedRequest(entity) =>
            HttpResponse(
              status = StatusCodes.OK,
              entity = HttpEntity(ContentTypes.`application/json`, CreatePaymentResponseParser.stringify(response)))
      }
    }

    def errors(errorResponse: ErrorResponse): Unit = {
      server.appendAll {
        case HttpRequest(
          HttpMethods.POST,
          Path("/"),
          _,
          entity,
          _) if isStubbedRequest(entity) =>
            HttpResponse(
              status = StatusCodes.Forbidden,
              entity = HttpEntity(ContentTypes.`application/json`, ErrorResponseParser.stringify(errorResponse)))
      }
    }

    def failsUnauthorized(): Unit = {
      server.appendAll {
        case HttpRequest(
          HttpMethods.POST,
          Path("/"),
          _,
          entity,
          _) if isStubbedRequest(entity) =>
            HttpResponse(
              status = StatusCodes.Unauthorized,
              entity = HttpEntity(ContentTypes.`application/json`, ""))
      }
    }

    private def isStubbedRequest(entity: HttpEntity): Boolean = {
      val parsedRequest = CreatePaymentRequestParser.parse(entity.extractAsString)
      parsedRequest == request
    }
  }
}
