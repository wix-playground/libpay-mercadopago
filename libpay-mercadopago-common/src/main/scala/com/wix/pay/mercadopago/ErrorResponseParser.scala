package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model.ErrorResponse
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

object ErrorResponseParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): ErrorResponse = {
    Serialization.read[ErrorResponse](str)
  }

  def stringify(obj: ErrorResponse): String = {
    Serialization.write(obj)
  }
}
