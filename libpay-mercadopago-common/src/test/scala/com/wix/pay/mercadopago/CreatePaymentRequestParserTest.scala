package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model._
import org.specs2.matcher.MustMatchers._
import org.specs2.matcher.{AlwaysMatcher, Matcher}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class CreatePaymentRequestParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    def beCreatePaymentRequest(amount: Matcher[Double] = AlwaysMatcher(),
                               sponsor_id: Matcher[Int] = AlwaysMatcher(),
                               card_token_id: Matcher[String] = AlwaysMatcher()): Matcher[CreatePaymentRequest] = {
      amount ^^ { (_: CreatePaymentRequest).amount aka "amount" } and
        sponsor_id ^^ { (_: CreatePaymentRequest).sponsor_id aka "sponsor_id" } and
          card_token_id ^^ { (_: CreatePaymentRequest).card_token_id aka "card_token_id" }
    }

    val someCreatePaymentRequest = CreatePaymentRequest(
      amount = 33.3,
      reason = "some reason",
      currency_id = "some currency ID",
      installments = 1,
      payment_method_id = Some(PaymentMethodIds.visa),
      card_token_id = "some card token ID",
      payer_email = "some payer email",
      external_reference = "some external reference",
      sponsor_id = 123456789
    )
  }

  "stringify and then parse" should {
    "yield an object similar to the original one" in new Ctx {
      val str = CreatePaymentRequestParser.stringify(someCreatePaymentRequest)
      CreatePaymentRequestParser.parse(str) must beCreatePaymentRequest(
        amount = ===(someCreatePaymentRequest.amount),
        sponsor_id = ===(someCreatePaymentRequest.sponsor_id),
        card_token_id = ===(someCreatePaymentRequest.card_token_id)
      )
    }
  }
}
