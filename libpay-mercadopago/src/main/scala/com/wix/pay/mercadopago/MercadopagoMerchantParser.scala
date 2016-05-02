package com.wix.pay.mercadopago

trait MercadopagoMerchantParser {
  def parse(merchantKey: String): MercadopagoMerchant
  def stringify(merchant: MercadopagoMerchant): String
}
