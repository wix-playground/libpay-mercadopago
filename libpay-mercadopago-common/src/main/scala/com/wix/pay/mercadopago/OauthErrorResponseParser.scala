package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model.OauthErrorResponse
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class OauthErrorResponseParser() {
  private implicit val formats = DefaultFormats

  def parse(str: String): OauthErrorResponse = {
    Serialization.read[OauthErrorResponse](str)
  }

  def stringify(obj: OauthErrorResponse): String = {
    Serialization.write(obj)
  }
}
