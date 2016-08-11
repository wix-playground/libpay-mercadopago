package com.wix.pay.mercadopago

import com.wix.pay.mercadopago.model.OauthResponse
import org.specs2.matcher.MustMatchers._
import org.specs2.matcher.{AlwaysMatcher, Matcher}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class OauthResponseParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    def beOauthResponse(access_token: Matcher[String] = AlwaysMatcher(),
                        token_type: Matcher[String] = AlwaysMatcher(),
                        expires_in: Matcher[Long] = AlwaysMatcher(),
                        scope: Matcher[String] = AlwaysMatcher(),
                        refresh_token: Matcher[String] = AlwaysMatcher()): Matcher[OauthResponse] = {
      access_token ^^ { (_: OauthResponse).access_token aka "access_token" } and
        token_type ^^ { (_: OauthResponse).token_type aka "token_type" } and
        expires_in ^^ { (_: OauthResponse).expires_in aka "expires_in" } and
        scope ^^ { (_: OauthResponse).scope aka "scope" } and
        refresh_token ^^ { (_: OauthResponse).refresh_token aka "refresh_token" }
    }

    val someOauthResponse = OauthResponse(
      access_token = "some access token",
      token_type = "some token type",
      expires_in = 1234,
      scope = "some scope",
      refresh_token = "some refresh token"
    )
  }

  "stringify and then parse" should {
    "yield an object similar to the original one" in new Ctx {
      val str = OauthResponseParser.stringify(someOauthResponse)
      OauthResponseParser.parse(str) must beOauthResponse(
        access_token = ===(someOauthResponse.access_token),
        token_type = ===(someOauthResponse.token_type),
        expires_in = ===(someOauthResponse.expires_in),
        scope = ===(someOauthResponse.scope),
        refresh_token = ===(someOauthResponse.refresh_token)
      )
    }
  }
}
