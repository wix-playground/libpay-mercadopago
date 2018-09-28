package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class TokenizeResponseParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val someTokenizeResponse = TokenizeResponse(
      id = "some ID"
    )
  }

  "stringify and then parse" should {
    "yield an object similar to the original one" in new Ctx {
      val json: String = TokenizeResponseParser.stringify(someTokenizeResponse)
      TokenizeResponseParser.parse(json) must beEqualTo(someTokenizeResponse)
    }
  }
}
