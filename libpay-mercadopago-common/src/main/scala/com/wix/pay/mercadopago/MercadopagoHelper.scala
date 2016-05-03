package com.wix.pay.mercadopago

import com.wix.pay.creditcard.networks.Networks
import com.wix.pay.creditcard.{CreditCard, PublicCreditCard}
import com.wix.pay.mercadopago.model._
import com.wix.pay.model.CurrencyAmount

object MercadopagoHelper {
  def createOauthRequest(clientId: String, clientSecret: String): Map[String, String] = {
    Map(
      OauthFields.grantType -> GrantTypes.clientCredentials,
      OauthFields.clientId -> clientId,
      OauthFields.clientSecret -> clientSecret
    )
  }

  def createTokenizeRequest(creditCard: CreditCard, countryCode: String): TokenizeRequest = {
    MercadopagoHelper.countryIdentificationTypes.get(countryCode).map { identificationType =>
      TokenizeRequest(
        card_number = creditCard.number,
        security_code = creditCard.csc,
        expiration_month = creditCard.expiration.month,
        expiration_year = creditCard.expiration.year,
        cardholder = CardHolder(
          name = creditCard.holderName.get,
          identification = Identification(
            `type` = identificationType,
            number = creditCard.holderId.get
          )
        )
      )
    }.getOrElse {
      throw new IllegalArgumentException(s"Unsupported countryCode: $countryCode")
    }
  }

  def createCreatePaymentRequest(currencyAmount: CurrencyAmount,
                                 dealId: String,
                                 dealTitle: String,
                                 cardTokenId: String,
                                 paymentMethodId: Option[String],
                                 customerEmail: String): CreatePaymentRequest = {
    CreatePaymentRequest(
      amount = currencyAmount.amount,
      reason = dealTitle,
      currency_id = currencyAmount.currency,
      installments = 1,
      payment_method_id = paymentMethodId,
      card_token_id = cardTokenId,
      payer_email = customerEmail,
      external_reference = dealId
    )
  }

  def toPaymentMethodId(creditCard: CreditCard): Option[String] = {
    PublicCreditCard(creditCard).network match {
      case Some(network) => toPaymentMethodId(network)
      case None => None
    }
  }

  private def toPaymentMethodId(network: String): Option[String] = {
    network match {
      case Networks.visa => Some(PaymentMethodIds.visa)
      case Networks.masterCard => Some(PaymentMethodIds.mastercard)
      case _ => None
    }
  }

  private val countryIdentificationTypes = Map(
    "AR" -> IdentificationTypes.dni,
    "BR" -> IdentificationTypes.cpf,
    "CO" -> IdentificationTypes.cc,
    "MX" -> IdentificationTypes.rfc,
    "VE" -> IdentificationTypes.ci
  )
}
