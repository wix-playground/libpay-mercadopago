package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model._
import org.specs2.matcher.MustMatchers._
import org.specs2.matcher.{AlwaysMatcher, Matcher}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class TokenizeRequestParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    def beTokenizeRequest(card_number: Matcher[String] = AlwaysMatcher(),
                          security_code: Matcher[Option[String]] = AlwaysMatcher(),
                          expiration_month: Matcher[Int] = AlwaysMatcher(),
                          expiration_year: Matcher[Int] = AlwaysMatcher(),
                          cardholder: Matcher[CardHolder] = AlwaysMatcher()): Matcher[TokenizeRequest] = {
      card_number ^^ { (_: TokenizeRequest).card_number aka "card_number" } and
        security_code ^^ { (_: TokenizeRequest).security_code aka "security_code" } and
        expiration_month ^^ { (_: TokenizeRequest).expiration_month aka "expiration_month" } and
        expiration_year ^^ { (_: TokenizeRequest).expiration_year aka "expiration_year" } and
        cardholder ^^ { (_: TokenizeRequest).cardholder aka "cardholder" }
    }

    val someTokenizeRequest = TokenizeRequest(
      card_number = "some card number",
      security_code = Some("some security code"),
      expiration_month = 12,
      expiration_year = 2020,
      cardholder = CardHolder(
        name = "some cardholder name",
        identification = Identification(
          subtype = "some subtype",
          number = "some number",
          `type` = IdentificationTypes.cpf
        )
      )
    )
  }

  "stringify and then parse" should {
    "yield an object similar to the original one" in new Ctx {
      val str = TokenizeRequestParser.stringify(someTokenizeRequest)
      TokenizeRequestParser.parse(str) must beTokenizeRequest(
        card_number = ===(someTokenizeRequest.card_number),
        security_code = ===(someTokenizeRequest.security_code),
        expiration_month = ===(someTokenizeRequest.expiration_month),
        expiration_year = ===(someTokenizeRequest.expiration_year),
        cardholder = ===(someTokenizeRequest.cardholder)
      )
    }
  }
}
