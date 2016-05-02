package com.wix.pay.mercadopago.model

/** @see <a href="https://developers.mercadopago.com/documentation/custom-checkout">Customized Checkout</a> */
case class CreatePaymentRequest(amount: Double,
                                reason: String,
                                currency_id: String,
                                installments: Int,
                                payment_method_id: Option[String] = None,
                                card_token_id: String,
                                payer_email: String,
                                external_reference: String)
