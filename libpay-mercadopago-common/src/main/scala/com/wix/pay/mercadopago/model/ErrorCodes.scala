package com.wix.pay.mercadopago.model

object ErrorCodes {
  val invalidCardNumberLength = "3033"

  /**
    * Cannot pay this amount with this payment_method_id
    * Example: 100.0 COP cannot be charged
    */
  val cannotPayThisAmountWithThisPaymentMethodId = "132"

  /**
    * Invalid parameter 'cardholder.identification.number'
    * Example: card holder ID is not a valid Chilean RUT
    */
  val invalidParameterCardHolderIdentificationNumer = "324"

  /** Error codes that should be treated as if the payment was rejected. */
  val paymentRejectedBadRequestErrorCodes = Set(
    invalidCardNumberLength,
    cannotPayThisAmountWithThisPaymentMethodId,
    invalidParameterCardHolderIdentificationNumer
  )
}
