package com.wix.pay.mercadopago

trait MercadopagoAuthorizationParser {
  def parse(authorizationKey: String): MercadopagoAuthorization
  def stringify(authorization: MercadopagoAuthorization): String
}
