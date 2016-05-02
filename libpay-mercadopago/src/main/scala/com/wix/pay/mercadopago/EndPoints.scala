package com.wix.pay.mercadopago


object OauthEndpoint {
  val production = "https://api.mercadolibre.com/oauth/token"
}

object TokenizationEndpoint {
  val production = "https://pagamento.mercadopago.com/card_tokens"
}

object PaymentsEndpoint {
  val production = "https://api.mercadolibre.com/checkout/custom/create_payment"
}
