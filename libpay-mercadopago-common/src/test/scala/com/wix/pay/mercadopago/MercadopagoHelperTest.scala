package com.wix.pay.mercadopago


import com.wix.pay.creditcard.{CreditCard, CreditCardOptionalFields, YearMonth}
import com.wix.pay.mercadopago.model.IdentificationTypes
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class MercadopagoHelperTest extends SpecWithJUnit {
  trait Ctx extends Scope {}

  "createTokenizeRequest" should {
    "use the given country's identification type" in new Ctx {
      val card = CreditCard(
        number = "4580458045804580",
        expiration = YearMonth(
          year = 2020,
          month = 1
        ),
        additionalFields = Some(CreditCardOptionalFields.withFields(
          holderName = Some("Test"),
          holderId = Some("12345")
        ))
      )

      MercadopagoHelper.createTokenizeRequest(
        creditCard = card,
        countryCode = "AR"
      ).cardholder.identification.`type` must beEqualTo(IdentificationTypes.dni)

      MercadopagoHelper.createTokenizeRequest(
        creditCard = card,
        countryCode = "BR"
      ).cardholder.identification.`type` must beEqualTo(IdentificationTypes.cpf)

      MercadopagoHelper.createTokenizeRequest(
        creditCard = card,
        countryCode = "CO"
      ).cardholder.identification.`type` must beEqualTo(IdentificationTypes.cc)

      MercadopagoHelper.createTokenizeRequest(
        creditCard = card,
        countryCode = "MX"
      ).cardholder.identification.`type` must beEqualTo(IdentificationTypes.rfc)

      MercadopagoHelper.createTokenizeRequest(
        creditCard = card,
        countryCode = "VE"
      ).cardholder.identification.`type` must beEqualTo(IdentificationTypes.ci)
    }
  }
}
