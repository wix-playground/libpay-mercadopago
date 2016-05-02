package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model.OauthErrorResponse
import org.specs2.matcher.MustMatchers._
import org.specs2.matcher.{AlwaysMatcher, Matcher}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class OauthErrorResponseParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val parser = new OauthErrorResponseParser

    def beOauthErrorResponse(message: Matcher[String] = AlwaysMatcher(),
                             error: Matcher[String] = AlwaysMatcher(),
                             status: Matcher[Int] = AlwaysMatcher()): Matcher[OauthErrorResponse] = {
      message ^^ { (_: OauthErrorResponse).message aka "message" } and
        error ^^ { (_: OauthErrorResponse).error aka "error" } and
        status ^^ { (_: OauthErrorResponse).status aka "status" }
    }

    val someOauthErrorResponse = OauthErrorResponse(
      message = "some error message",
      error = "some error",
      status = 666
    )
  }

  "stringify and then parse" should {
    "yield an object similar to the original one" in new Ctx {
      val str = parser.stringify(someOauthErrorResponse)
      parser.parse(str) must beOauthErrorResponse(
        message = ===(someOauthErrorResponse.message),
        error = ===(someOauthErrorResponse.error),
        status = ===(someOauthErrorResponse.status)
      )
    }
  }
}
