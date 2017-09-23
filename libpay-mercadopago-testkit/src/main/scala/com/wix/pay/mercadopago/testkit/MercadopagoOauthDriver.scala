package com.wix.pay.mercadopago.testkit

import java.util.{List => JList}

import com.google.api.client.http.UrlEncodedParser
import com.wix.hoopoe.http.testkit.EmbeddedHttpProbe
import com.wix.hoopoe.http.testkit.EmbeddedHttpProbe.NotFoundHandler
import com.wix.pay.mercadopago.model.{OauthErrorResponse, OauthResponse}
import com.wix.pay.mercadopago.{MercadopagoHelper, OauthErrorResponseParser, OauthResponseParser}
import spray.http._

import scala.collection.JavaConversions._
import scala.collection.mutable

class MercadopagoOauthDriver(probe: EmbeddedHttpProbe) {
  def this(port: Int) = this(new EmbeddedHttpProbe(port, NotFoundHandler))

  def start() {
    probe.doStart()
  }

  def stop() {
    probe.doStop()
  }

  def reset() {
    probe.handlers.clear()
  }

  def aRequestFor(clientId: String, clientSecret: String): RequestCtx = {
    new RequestCtx(MercadopagoHelper.createOauthRequest(
      clientId = clientId,
      clientSecret = clientSecret
    ))
  }

  class RequestCtx(params: Map[String, String]) {
    def returns(accessToken: String): Unit = {
      returns(OauthResponse(
        access_token = accessToken,
        token_type = "some token type",
        expires_in = 1000,
        scope = "some scope",
        refresh_token = "some refresh token"
      ))
    }

    def failsWith(errorMessage: String): Unit = {
      errors(OauthErrorResponse(
        message = errorMessage,
        error = "some error",
        status = 666
      ))
    }

    def returns(response: OauthResponse): Unit = {
      probe.handlers += {
        case HttpRequest(
        HttpMethods.POST,
        Uri.Path("/"),
        _,
        entity,
        _) if isStubbedRequest(entity) =>
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(ContentTypes.`application/json`, OauthResponseParser.stringify(response)))
      }
    }

    def errors(errorResponse: OauthErrorResponse): Unit = {
      probe.handlers += {
        case HttpRequest(
        HttpMethods.POST,
        Uri.Path("/"),
        _,
        entity,
        _) if isStubbedRequest(entity) =>
          HttpResponse(
            status = StatusCodes.Forbidden,
            entity = HttpEntity(ContentTypes.`application/json`, OauthErrorResponseParser.stringify(errorResponse)))
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
      params.mapValues(_ (0)).toMap
    }
  }
}
