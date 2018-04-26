package com.wix.pay.mercadopago.testkit


import java.util.{List => JList}

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import com.google.api.client.http.UrlEncodedParser
import com.wix.e2e.http.api.StubWebServer
import com.wix.e2e.http.client.extractors.HttpMessageExtractors._
import com.wix.e2e.http.server.WebServerFactory.aStubWebServer
import com.wix.pay.mercadopago.model.{OauthErrorResponse, OauthResponse}
import com.wix.pay.mercadopago.{MercadopagoHelper, OauthErrorResponseParser, OauthResponseParser}

import scala.collection.JavaConversions._
import scala.collection.mutable


class MercadopagoOauthDriver(server: StubWebServer) {
  def this(port: Int) = this(aStubWebServer.onPort(port).build)

  def start(): Unit = server.start()
  def stop(): Unit = server.stop()
  def reset(): Unit = server.replaceWith()


  def aRequestFor(clientId: String, clientSecret: String): RequestCtx = {
    new RequestCtx(MercadopagoHelper.createOauthRequest(
      clientId = clientId,
      clientSecret = clientSecret))
  }

  class RequestCtx(params: Map[String, String]) {
    def returns(accessToken: String): Unit = {
      returns(OauthResponse(
        access_token = accessToken,
        token_type = "some token type",
        expires_in = Some(1000),
        scope = "some scope",
        refresh_token = Some("some refresh token")))
    }

    def failsWith(errorMessage: String): Unit = {
      errors(OauthErrorResponse(
        message = errorMessage,
        error = "some error",
        status = 666))
    }

    def returns(response: OauthResponse): Unit = {
      server.appendAll {
        case HttpRequest(
          HttpMethods.POST,
          Path("/"),
          _,
          entity,
          _) if isStubbedRequest(entity) =>
            HttpResponse(
              status = StatusCodes.OK,
              entity = HttpEntity(ContentTypes.`application/json`, OauthResponseParser.stringify(response)))
      }
    }

    def errors(errorResponse: OauthErrorResponse): Unit = {
      server.appendAll {
        case HttpRequest(
          HttpMethods.POST,
          Path("/"),
          _,
          entity,
          _) if isStubbedRequest(entity) =>
            HttpResponse(
              status = StatusCodes.Forbidden,
              entity = HttpEntity(ContentTypes.`application/json`, OauthErrorResponseParser.stringify(errorResponse)))
      }
    }

    private def isStubbedRequest(entity: HttpEntity): Boolean = {
      val requestParams = urlDecode(entity.extractAsString)

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
