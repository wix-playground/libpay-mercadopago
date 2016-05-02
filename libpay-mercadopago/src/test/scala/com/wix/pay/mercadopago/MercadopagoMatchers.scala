package com.wix.pay.mercadopago

import org.specs2.matcher.{AlwaysMatcher, Matcher, Matchers}

trait MercadopagoMatchers extends Matchers {
  def authorizationParser: MercadopagoAuthorizationParser

  def beMerchant(clientId: Matcher[String] = AlwaysMatcher(),
                 clientSecret: Matcher[String] = AlwaysMatcher(),
                 countryCode: Matcher[String] = AlwaysMatcher()): Matcher[MercadopagoMerchant] = {
    clientId ^^ { (_: MercadopagoMerchant).clientId aka "client ID" } and
      clientSecret ^^ { (_: MercadopagoMerchant).clientSecret aka "client Secret" } and
      countryCode ^^ { (_: MercadopagoMerchant).countryCode aka "country code" }
  }

  def beAuthorization(): Matcher[MercadopagoAuthorization] = {
    AlwaysMatcher()
  }

  def beAuthorizationKey(authorization: Matcher[MercadopagoAuthorization]): Matcher[String] = {
    authorization ^^ { authorizationParser.parse(_: String) aka "parsed authorization"}
  }
}

object MercadopagoMatchers extends MercadopagoMatchers {
  override val authorizationParser = new JsonMercadopagoAuthorizationParser()
}