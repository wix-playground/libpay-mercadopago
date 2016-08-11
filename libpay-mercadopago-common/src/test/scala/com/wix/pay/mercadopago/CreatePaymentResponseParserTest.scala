package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model._
import org.specs2.matcher.MustMatchers._
import org.specs2.matcher.{AlwaysMatcher, Matcher}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class CreatePaymentResponseParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    def beCreatePaymentResponse(payment_id: Matcher[String] = AlwaysMatcher()): Matcher[CreatePaymentResponse] = {
      payment_id ^^ { (_: CreatePaymentResponse).payment_id aka "payment_id" }
    }

    val someCreatePaymentResponse = CreatePaymentResponse(
      payment_id = "some payment ID",
      status = Statuses.approved,
      status_code = Some("some status code"),
      status_detail = "some status detail",
      amount = 11.1,
      installments = 1,
      total_paid_amount = 11.1,
      finance_charge = 0,
      marketplace_fee = Some(0),
      currency_id = "BRL",
      payment_method_id = PaymentMethodIds.visa,
      card_issuer_id = 1234,
      payment_type = PaymentTypes.creditcard,
      operation_type = "some operation type",
      external_reference = "some external reference",
      payer_email = "some payer email",
      reason = "some reason",
      card_token_id = "some card token ID",
      statement_descriptor = Some("some statement descriptor")
    )
  }

  "stringify and then parse" should {
    "yield an object similar to the original one" in new Ctx {
      val str = CreatePaymentResponseParser.stringify(someCreatePaymentResponse)
      CreatePaymentResponseParser.parse(str) must beCreatePaymentResponse(
        payment_id = ===(someCreatePaymentResponse.payment_id)
      )
    }
  }
}
