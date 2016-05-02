package com.wix.pay.mercadopago

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class JsonMercadopagoMerchantParser() extends MercadopagoMerchantParser {
  private implicit val formats = DefaultFormats

  override def parse(merchantKey: String): MercadopagoMerchant = {
    Serialization.read[MercadopagoMerchant](merchantKey)
  }

  override def stringify(merchant: MercadopagoMerchant): String = {
    Serialization.write(merchant)
  }
}
