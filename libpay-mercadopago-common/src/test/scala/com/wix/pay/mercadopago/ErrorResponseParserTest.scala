package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model._
import org.specs2.matcher.MustMatchers._
import org.specs2.matcher.{AlwaysMatcher, Matcher}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class ErrorResponseParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    def beErrorResponse(error: Matcher[String] = AlwaysMatcher(),
                        message: Matcher[String] = AlwaysMatcher(),
                        cause: Matcher[List[Error]] = AlwaysMatcher()): Matcher[ErrorResponse] = {
      error ^^ { (_: ErrorResponse).error aka "error" } and
        message ^^ { (_: ErrorResponse).message aka "message" } and
        cause ^^ { (_: ErrorResponse).cause aka "cause" }
    }

    val someErrorResponse = ErrorResponse(
      error = "some error",
      message = "some message",
      cause = List(
        Error(code = "someCode", description = "1st error description"),
        Error(code = "someCode2", description = "2nd error description")
      )
    )
  }

  "stringify and then parse" should {
    "yield an object similar to the original one" in new Ctx {
      val str = ErrorResponseParser.stringify(someErrorResponse)
      ErrorResponseParser.parse(str) must beErrorResponse(
        error = ===(someErrorResponse.error),
        message = ===(someErrorResponse.message),
        cause = ===(someErrorResponse.cause)
      )
    }
  }
}
