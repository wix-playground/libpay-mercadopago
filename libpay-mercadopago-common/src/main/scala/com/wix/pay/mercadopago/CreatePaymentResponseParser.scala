package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model.CreatePaymentResponse
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

object CreatePaymentResponseParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): CreatePaymentResponse = {
    Serialization.read[CreatePaymentResponse](str)
  }

  def stringify(obj: CreatePaymentResponse): String = {
    Serialization.write(obj)
  }
}
