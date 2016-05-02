package com.wix.pay.mercadopago

import com.google.api.client.http._

import scala.concurrent.duration.Duration


abstract class MercadoPagoHttpExecutor(requestFactory: HttpRequestFactory,
                                       connectTimeout: Option[Duration],
                                       readTimeout: Option[Duration],
                                       numberOfRetries: Int) {
  protected def executeHttpPostRequest[T <: AnyRef](endpointUrl: String,
                                                    accessToken: String,
                                                    requestJson: String): HttpResponse =
    executeHttpRequest { () =>
      requestFactory.buildPostRequest(
        new GenericUrl(s"$endpointUrl?access_token=$accessToken"),
        new ByteArrayContent(
          "application/json",
          requestJson.getBytes("UTF-8")))
    }

  private def executeHttpRequest(f: () => HttpRequest): HttpResponse = {
    val httpRequest = f.apply
    connectTimeout foreach (to => httpRequest.setConnectTimeout(to.toMillis.toInt))
    readTimeout foreach (to => httpRequest.setReadTimeout(to.toMillis.toInt))
    httpRequest.setNumberOfRetries(numberOfRetries)

    httpRequest.setThrowExceptionOnExecuteError(false)

    httpRequest.execute()
  }

  protected def executeHttpGetRequest[T <: AnyRef](endpointUrl: String,
                                                   accessToken: String): HttpResponse =
    executeHttpRequest { () =>
      val httpRequest = requestFactory.buildGetRequest(
        new GenericUrl(s"$endpointUrl?access_token=$accessToken"))

      httpRequest.getHeaders().setAccept("application/json")
      httpRequest
    }

  protected def extractResponseAndClose(httpResponse: HttpResponse): String =
    try {
      httpResponse.parseAsString()
    } finally {
      httpResponse.ignore()
    }
}
