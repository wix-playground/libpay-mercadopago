package com.wix.pay.mercadopago.model

/**
 * @see <a href="https://developers.mercadopago.com/documentation/custom-checkout-responses">Customized Checkout Responses</a>
 */
object Statuses {
  /** Success! The payment was approved. */
  val approved = "approved"
  /** We cannot finish your payment right now, but we are processing it and will notify the result to you as soon as possible. */
  val pending = "pending"
  /**
   * We need to make some additional checks before approving or rejecting this payment. The final result of it will be
   * informed to you soon.
   */
  val inProcess = "in_process"
  /** The payment was rejected. You need to review the entered data or either trying to pay with another payment method. */
  val rejected = "rejected"
}
