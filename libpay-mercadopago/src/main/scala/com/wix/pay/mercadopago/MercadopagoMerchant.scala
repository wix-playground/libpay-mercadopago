package com.wix.pay.mercadopago

/**
 * @see <a href="https://developers.mercadopago.com/documentation/authentication">Get credentials</a>
 */
case class MercadopagoMerchant(clientId: String, clientSecret: String, countryCode: String)
