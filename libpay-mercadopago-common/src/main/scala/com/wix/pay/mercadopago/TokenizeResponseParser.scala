package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model.TokenizeResponse
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

object TokenizeResponseParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): TokenizeResponse = {
    Serialization.read[TokenizeResponse](str)
  }

  def stringify(obj: TokenizeResponse): String = {
    Serialization.write(obj)
  }
}
