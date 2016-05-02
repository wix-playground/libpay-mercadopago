package com.wix.pay.mercadopago.testkit

import java.util.{List => JList}

import com.google.api.client.http.UrlEncodedParser
import com.wix.hoopoe.http.testkit.EmbeddedHttpProbe
import com.wix.pay.mercadopago.model.{OauthErrorResponse, OauthResponse}
import com.wix.pay.mercadopago.{OauthErrorResponseParser, OauthResponseParser}
import spray.http._

import scala.collection.JavaConversions._
import scala.collection.mutable

class MercadopagoOauthDriver(port: Int) {
  private val probe = new EmbeddedHttpProbe(port, EmbeddedHttpProbe.NotFoundHandler)

  private val responseParser = new OauthResponseParser
  private val errorResponseParser = new OauthErrorResponseParser

  def startProbe() {
    probe.doStart()
  }

  def stopProbe() {
    probe.doStop()
  }

  def resetProbe() {
    probe.handlers.clear()
  }

  def aRequestFor(params: Map[String, String]): RequestCtx = {
    new RequestCtx(params)
  }

  class RequestCtx(params: Map[String, String]) {
    def returns(response: OauthResponse) {
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

    def errors(errorResponse: OauthErrorResponse) {
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
      val requestParams = urlDecode(entity.asString)

      params.forall {
        case (k, v) => requestParams.contains(k) && v == requestParams(k)
      }
    }

    private def urlDecode(str: String): Map[String, String] = {
      val params = mutable.LinkedHashMap[String, JList[String]]()
      UrlEncodedParser.parse(str, mutableMapAsJavaMap(params))
      params.mapValues( _(0) ).toMap
    }
  }
}
