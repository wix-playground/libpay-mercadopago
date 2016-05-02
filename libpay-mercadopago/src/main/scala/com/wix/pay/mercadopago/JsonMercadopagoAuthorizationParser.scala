package com.wix.pay.mercadopago

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class JsonMercadopagoAuthorizationParser() extends MercadopagoAuthorizationParser {
  private implicit val formats = DefaultFormats

  override def parse(authorizationKey: String): MercadopagoAuthorization = {
    Serialization.read[MercadopagoAuthorization](authorizationKey)
  }

  override def stringify(authorization: MercadopagoAuthorization): String = {
    Serialization.write(authorization)
  }
}
