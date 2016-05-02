package com.wix.pay.mercadopago


import com.wix.pay.mercadopago.MercadopagoMatchers._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class JsonMercadopagoAuthorizationParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val authorizationParser: MercadopagoAuthorizationParser = new JsonMercadopagoAuthorizationParser
  }

  "stringify and then parse" should {
    "yield an authorization similar to the original one" in new Ctx {
      val someAuthorization = MercadopagoAuthorization()

      val authorizationKey = authorizationParser.stringify(someAuthorization)
      authorizationParser.parse(authorizationKey) must beAuthorization()
    }
  }
}
