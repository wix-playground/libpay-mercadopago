package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model.TokenizeRequest
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

object TokenizeRequestParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): TokenizeRequest = {
    Serialization.read[TokenizeRequest](str)
  }

  def stringify(obj: TokenizeRequest): String = {
    Serialization.write(obj)
  }
}
