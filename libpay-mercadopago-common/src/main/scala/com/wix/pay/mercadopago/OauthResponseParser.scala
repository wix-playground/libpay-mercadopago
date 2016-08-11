package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model.OauthResponse
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

object OauthResponseParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): OauthResponse = {
    Serialization.read[OauthResponse](str)
  }

  def stringify(obj: OauthResponse): String = {
    Serialization.write(obj)
  }
}
