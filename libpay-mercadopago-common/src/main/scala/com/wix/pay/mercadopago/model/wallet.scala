package com.wix.pay.direct.mercadopago.model

case class MercadoPreferenceItem(title: String,
                                 quantity: String,
                                 currency_id: String,
                                 unit_price: Double,
                                 id: Option[String] = None,
                                 description: Option[String] = None,
                                 picture_url: Option[String] = None,
                                 category_id: Option[String] = None)

case class MercadoBackUrls(success: Option[String] = None,
                           pending: Option[String] = None,
                           failure: Option[String] = None)

case class MercadoPreferenceRequest(items: Seq[MercadoPreferenceItem],
                                    back_urls: MercadoBackUrls = MercadoBackUrls.empty,
                                    notification_url: Option[String] = None,
                                    external_reference: Option[String] = None)

case object MercadoBackUrls {
  val empty = MercadoBackUrls()

  def fromSingleUrl(url: String) =
    MercadoBackUrls(Some(url), Some(url), Some(url))
}

case class MercadoReceiverAddress(zip_code: String,
                                  street_number: Int,
                                  street_name: String,
                                  floor: String,
                                  apartment: String)

case class MercadoShipment(receiver_address: MercadoReceiverAddress
                            )

case class MercadoExcludedPayment(id: String)

case class MercadoPagoPaymentMethods(excluded_payment_methods: Seq[MercadoExcludedPayment],
                                     excluded_payment_types: Seq[MercadoExcludedPayment],
                                     installments: Int,
                                     default_payment_method_id: String,
                                     default_installments: Int)

case class MercadoPayer(name: String,
                        surname: String,
                        email: String,
                        date_created: String,
                        phone: MercadoPhone,
                        identification: MercadoIdentification,
                        address: MercadoAddress)

case class MercadoPhone(area_code: String,
                        number: String)

case class MercadoIdentification(`type`: String,
                                 number: String)

case class MercadoAddress(street_name: String,
                          street_number: Int,
                          zip_code: String)


case class MercadoPreferenceResponse(collector_id: Int,
                                     operation_type: String,
                                     items: Seq[MercadoPreferenceItem],
                                     payer: MercadoPayer,
                                     back_urls: MercadoBackUrls,
                                     payment_methods: MercadoPagoPaymentMethods,
                                     client_id: Int,
                                     marketplace: String,
                                     marketplace_fee: Float,
                                     shipments: MercadoShipment,
                                     notification_url: String,
                                     expires: Boolean,
                                     expiration_date_from: String,
                                     expiration_date_to: String,
                                     date_created: String,
                                     id: String,
                                     init_point: String,
                                     sandbox_init_point: String,
                                     auto_return: Option[String] = None,
                                     external_reference: Option[String] = None,
                                     additional_info: Option[String] = None)




case class MercadoOrderRequest(merchantOrderId: String)

case class MercadoPayment(id: String, status: String)
case class MercadoOrderResponse(id: String,
                                payments: Seq[MercadoPayment])