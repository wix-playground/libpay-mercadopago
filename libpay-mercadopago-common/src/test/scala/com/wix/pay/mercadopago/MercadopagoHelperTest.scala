package com.wix.pay.mercadopago

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class MercadopagoHelperTest extends Specification {

  "sponsor id helper" should {

    "get Argentina sponsor code" in new Scope {
      MercadopagoHelper.getSponsorId("AR") must beEqualTo(249880066)
    }

    "get Brazil sponsor code" in new Scope {
      MercadopagoHelper.getSponsorId("BR") must beEqualTo(249882304)
    }

    "get Mexico sponsor code" in new Scope {
      MercadopagoHelper.getSponsorId("MX") must beEqualTo(249886588)
    }

    "get Colombia sponsor code" in new Scope {
      MercadopagoHelper.getSponsorId("CO") must beEqualTo(249886998)
    }

    "get Chile sponsor code" in new Scope {
      MercadopagoHelper.getSponsorId("CL") must beEqualTo(249886882)
    }

    "get Peru sponsor code" in new Scope {
      MercadopagoHelper.getSponsorId("PE") must beEqualTo(249887185)
    }

    "get Uruguay sponsor code" in new Scope {
      MercadopagoHelper.getSponsorId("UY") must beEqualTo(249883989)
    }

    "get Venezuela sponsor code" in new Scope {
      MercadopagoHelper.getSponsorId("VE") must beEqualTo(249887375)
    }
  }
}
