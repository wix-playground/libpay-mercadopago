package com.wix.pay.mercadopago.testkit

import com.wix.hoopoe.http.testkit.EmbeddedHttpProbe
import com.wix.pay.mercadopago.model._
import com.wix.pay.mercadopago.{ErrorResponseParser, TokenizeRequestParser, TokenizeResponseParser}
import spray.http._

class MercadopagoTokenizationDriver(port: Int) {
  private val probe = new EmbeddedHttpProbe(port, EmbeddedHttpProbe.NotFoundHandler)

  private val requestParser = new TokenizeRequestParser
  private val responseParser = new TokenizeResponseParser
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

  def aRequestFor(request: TokenizeRequest): RequestCtx = {
    new RequestCtx(request)
  }

  class RequestCtx(request: TokenizeRequest) {
    def returns(response: TokenizeResponse) {
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

    private def isStubbedRequest(entity: HttpEntity): Boolean = {
      val parsedRequest = requestParser.parse(entity.asString)
      parsedRequest == request
    }
  }
}
