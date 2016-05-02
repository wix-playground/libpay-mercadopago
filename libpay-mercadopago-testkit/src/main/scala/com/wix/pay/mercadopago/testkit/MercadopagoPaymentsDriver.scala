package com.wix.pay.mercadopago.testkit

import com.wix.hoopoe.http.testkit.EmbeddedHttpProbe
import com.wix.pay.mercadopago.model._
import com.wix.pay.mercadopago.{CreatePaymentRequestParser, CreatePaymentResponseParser, ErrorResponseParser}
import spray.http._

class MercadopagoPaymentsDriver(port: Int) {
  private val probe = new EmbeddedHttpProbe(port, EmbeddedHttpProbe.NotFoundHandler)

  private val requestParser = new CreatePaymentRequestParser
  private val responseParser = new CreatePaymentResponseParser
  private val errorResponseParser = new ErrorResponseParser

  def startProbe() {
    probe.doStart()
  }

  def stopProbe() {
    probe.doStop()
  }

  def resetProbe() {
    probe.handlers.clear()
  }

  def aRequestFor(request: CreatePaymentRequest): RequestCtx = {
    new RequestCtx(request)
  }

  class RequestCtx(request: CreatePaymentRequest) {
    def returns(response: CreatePaymentResponse) {
      probe.handlers += {
        case HttpRequest(
        HttpMethods.POST,
        Uri.Path("/"),
        _,
        entity,
        _) if isStubbedRequest(entity) =>
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`application/json`, responseParser.stringify(response)))
      }
    }

    def errors(errorResponse: ErrorResponse) {
      probe.handlers += {
        case HttpRequest(
        HttpMethods.POST,
        Uri.Path("/"),
        _,
        entity,
        _) if isStubbedRequest(entity) =>
          HttpResponse(
            status = StatusCodes.Forbidden,
            entity = HttpEntity(ContentTypes.`application/json`, errorResponseParser.stringify(errorResponse)))
      }
    }

    def failsUnauthorized(): Unit = {
      probe.handlers += {
        case HttpRequest(
        HttpMethods.POST,
        Uri.Path("/"),
        _,
        entity,
        _) if isStubbedRequest(entity) =>
          HttpResponse(
            status = StatusCodes.Unauthorized,
            entity = HttpEntity(ContentTypes.`application/json`, ""))
      }
    }

    private def isStubbedRequest(entity: HttpEntity): Boolean = {
      val parsedRequest = requestParser.parse(entity.asString)
      parsedRequest == request
    }
  }
}
