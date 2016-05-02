package com.wix.pay.mercadopago.model

// Sometimes there's also an optional 'status' parameter which we don't really need
case class ErrorResponse(error: String,
                         message: String,
                         cause: List[Error]) {
  override def toString: String = {
    if (cause.isEmpty) {
      message
    } else {
      message + ": " + cause.map(error => error.description).mkString(", ")
    }
  }
}
