package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model.CreatePaymentRequest
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class CreatePaymentRequestParser() {
  private implicit val formats = DefaultFormats

  def parse(str: String): CreatePaymentRequest = {
    Serialization.read[CreatePaymentRequest](str)
  }

  def stringify(obj: CreatePaymentRequest): String = {
    Serialization.write(obj)
  }
}
