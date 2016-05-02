package com.wix.pay.mercadopago.model

/**
 * @param subtype
 * @param number
 * @param `type`  @see IdentificationTypes
 */
case class Identification(subtype: String = null, number: String, `type`: String)