package com.wix.pay.mercadopago

import com.google.api.client.http.{GenericUrl, HttpRequestFactory, HttpResponse, UrlEncodedContent}
import com.wix.pay.PaymentErrorException

import scala.collection.JavaConversions
import scala.concurrent.duration.Duration
import scala.util.Try

class AccessTokenRetriever(requestFactory: HttpRequestFactory,
                           connectTimeout: Option[Duration] = None,
                           readTimeout: Option[Duration] = None,
                           numberOfRetries: Int = 0,
                           endpointUrl: String) {
  
  def retrieveAccessToken(clientId: String, clientSecret: String): Try[String] = {
    Try {
      val params = MercadopagoHelper.createOauthRequest(clientId, clientSecret)

      val httpRequest = requestFactory.buildPostRequest(
        new GenericUrl(endpointUrl),
        new UrlEncodedContent(JavaConversions.mapAsJavaMap(params))
      )

      connectTimeout foreach (to => httpRequest.setConnectTimeout(to.toMillis.toInt))
      readTimeout foreach (to => httpRequest.setReadTimeout(to.toMillis.toInt))
      httpRequest.setNumberOfRetries(numberOfRetries)

      httpRequest.setThrowExceptionOnExecuteError(false)

      val httpResponse = httpRequest.execute()
      val responseJson = extractResponseAndClose(httpResponse)
      if (httpResponse.isSuccessStatusCode) {
        val oauthResponse = OauthResponseParser.parse(responseJson)
        oauthResponse.access_token
      } else {
        val oauthErrorResponse = OauthErrorResponseParser.parse(responseJson)
        throw PaymentErrorException(oauthErrorResponse.message)
      }
    }
  }

  private def extractResponseAndClose(httpResponse: HttpResponse): String = {
    try {
      httpResponse.parseAsString()
    } finally {
      httpResponse.ignore()
    }
  }
}
