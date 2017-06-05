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
                                 customerEmail: String,
                                 sponsorId: Int): CreatePaymentRequest = {
    CreatePaymentRequest(
      amount = currencyAmount.amount,
      reason = dealTitle,
      currency_id = currencyAmount.currency,
      installments = 1,
      payment_method_id = paymentMethodId,
      card_token_id = cardTokenId,
      payer_email = customerEmail,
      external_reference = dealId,
      sponsor_id = sponsorId
    )
  }

  def toPaymentMethodId(creditCard: CreditCard): Option[String] = {
    PublicCreditCard(creditCard).network.flatMap { networkPaymentMethodIds.get }
  }

  private val networkPaymentMethodIds = Map(
    Networks.visa -> PaymentMethodIds.visa,
    Networks.masterCard -> PaymentMethodIds.mastercard
  )

  private val countryIdentificationTypes = Map(
    "AR" -> IdentificationTypes.dni,
    "BR" -> IdentificationTypes.cpf,
    "CL" -> IdentificationTypes.rut,
    "CO" -> IdentificationTypes.cc,
    "MX" -> IdentificationTypes.rfc,
    "VE" -> IdentificationTypes.ci
  )

  private val argentinaCode = 249880066
  private val brazilCode = 249882304
  private val mexicoCode = 249886588
  private val colombiaCode = 249886998
  private val chileCode = 249886882
  private val peruCode = 249887185
  private val uruguayCode = 249883989
  private val venezuelaCode = 249887375

  private val countryCodeToSponsorCode = Map(
    "AR" -> argentinaCode,
    "BR" -> brazilCode,
    "MX" -> mexicoCode,
    "CO" -> colombiaCode,
    "CL" -> chileCode,
    "PE" -> peruCode,
    "UY" -> uruguayCode,
    "VE" -> venezuelaCode)

  def getSponsorId(countryCode: String) = {
    countryCodeToSponsorCode.get(countryCode).get
  }

}
