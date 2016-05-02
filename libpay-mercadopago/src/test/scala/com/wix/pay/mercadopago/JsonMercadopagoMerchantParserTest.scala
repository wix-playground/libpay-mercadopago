package com.wix.pay.mercadopago


import com.wix.pay.mercadopago.MercadopagoMatchers._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class JsonMercadopagoMerchantParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val parser: MercadopagoMerchantParser = new JsonMercadopagoMerchantParser
  }

  "stringify and then parse" should {
    "yield a merchant similar to the original one" in new Ctx {
      val someMerchant = MercadopagoMerchant(
        clientId = "some client ID",
        clientSecret = "some client secret",
        countryCode = "some country code"
      )

      val merchantKey = parser.stringify(someMerchant)
      parser.parse(merchantKey) must beMerchant(
        clientId = ===(someMerchant.clientId),
        clientSecret = === (someMerchant.clientSecret),
        countryCode = ===(someMerchant.countryCode)
      )
    }
  }
}
