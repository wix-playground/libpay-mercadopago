package com.wix.pay.mercadopago.model

/**
 * @see <a href="https://developers.mercadopago.com/documentation/custom-checkout">Customized Checkout</a>
 */
case class CreatePaymentResponse(payment_id: String,
                                 status: String,
                                 status_detail: String,
                                 status_code: Option[String],
                                 amount: Double,
                                 installments: Int,
                                 total_paid_amount: Double,
                                 finance_charge: Double,
                                 marketplace_fee: Option[Double],
                                 currency_id: String,
                                 payment_method_id: String,
                                 card_issuer_id: Int,
                                 payment_type: String,
                                 operation_type: String,
                                 external_reference: String,
                                 payer_email: String,
                                 reason: String,
                                 card_token_id: String,
                                 statement_descriptor: Option[String])
