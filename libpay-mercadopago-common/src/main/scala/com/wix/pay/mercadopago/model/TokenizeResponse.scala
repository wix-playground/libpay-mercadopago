package com.wix.pay.mercadopago.model

case class TokenizeResponse(public_key: String,
                            card_id: String,
                            luhn_validation: Boolean,
                            status: String,
                            used_date: String,
                            live_mode: Boolean,
                            card_number_length: Int,
                            id: String,
                            creation_date: String,
                            trunc_card_number: String,
                            security_code_length: Option[Int],
                            expiration_year: Int,
                            expiration_month: Int,
                            last_modified_date: String,
                            cardholder: CardHolder,
                            due_date: String)