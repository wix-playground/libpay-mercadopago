package com.wix.pay.mercadopago.model

case class TokenizeRequest(card_number: String,
                           security_code: Option[String],
                           expiration_month: Int,
                           expiration_year: Int,
                           cardholder: CardHolder)