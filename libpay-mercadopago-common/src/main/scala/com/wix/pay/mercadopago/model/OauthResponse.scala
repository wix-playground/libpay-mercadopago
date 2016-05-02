package com.wix.pay.mercadopago.model

case class OauthResponse(access_token: String, token_type: String, expires_in: Long, scope: String, refresh_token: String)
