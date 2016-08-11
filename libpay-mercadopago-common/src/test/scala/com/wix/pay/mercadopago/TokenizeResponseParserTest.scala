package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class TokenizeResponseParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val someTokenizeResponse = TokenizeResponse(
      public_key = "some public key",
      card_id = "some card ID",
      luhn_validation = true,
      status = "some status",
      used_date = "some user date",
      live_mode = true,
      card_number_length = 16,
      id = "some ID",
      creation_date = "some creation date",
      trunc_card_number = "1234",
      security_code_length = Some(3),
      expiration_year = 2020,
      expiration_month = 12,
      last_modified_date = "some last modified date",
      cardholder = CardHolder(
        name = "some cardholder name",
        identification = Identification(
          subtype = "some subtype",
          number = "some number",
          `type` = IdentificationTypes.cpf
        )
      ),
      due_date = "some due date"
    )
  }

  "stringify and then parse" should {
    "yield an object similar to the original one" in new Ctx {
      val str = TokenizeResponseParser.stringify(someTokenizeResponse)
      TokenizeResponseParser.parse(str) must beEqualTo(someTokenizeResponse)
    }
  }
}
