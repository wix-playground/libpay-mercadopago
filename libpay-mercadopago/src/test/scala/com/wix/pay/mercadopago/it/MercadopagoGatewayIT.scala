package com.wix.pay.mercadopago.it

import com.google.api.client.http.javanet.NetHttpTransport
import com.wix.pay.creditcard.{CreditCard, CreditCardOptionalFields, YearMonth}
import com.wix.pay.mercadopago._
import com.wix.pay.mercadopago.testkit.{MercadopagoOauthDriver, MercadopagoPaymentsDriver, MercadopagoTokenizationDriver}
import com.wix.pay.model.{CurrencyAmount, Customer, Deal, Payment}
import com.wix.pay.{PaymentErrorException, PaymentGateway, PaymentRejectedException}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class MercadopagoGatewayIT extends SpecWithJUnit {
  val mercadopagoOauthPort = 10015
  val mercadopagoTokenizationPort = 10016
  val mercadopagoPaymentsPort = 10017

  val requestFactory = new NetHttpTransport().createRequestFactory()
  val oauthDriver = new MercadopagoOauthDriver(port = mercadopagoOauthPort)
  val tokenizationDriver = new MercadopagoTokenizationDriver(port = mercadopagoTokenizationPort)
  val paymentsDriver = new MercadopagoPaymentsDriver(port = mercadopagoPaymentsPort)

  step {
    oauthDriver.start()
    tokenizationDriver.start()
    paymentsDriver.start()
  }

  sequential

  trait Ctx extends Scope {
    val merchantParser = new JsonMercadopagoMerchantParser()
    val authorizationParser = new JsonMercadopagoAuthorizationParser()

    val someCountryCode = "BR"
    val someMerchant = MercadopagoMerchant(
      clientId = "some client ID",
      clientSecret = "some client secret",
      countryCode = someCountryCode
    )
    val merchantKey = merchantParser.stringify(someMerchant)

    val someCreditCard = CreditCard(
      number = "4012888818888",
      expiration = YearMonth(
        year = 2020,
        month = 1
      ),
      additionalFields = Some(CreditCardOptionalFields.withFields(
        csc = Some("123"),
        holderName = Some("Johninio Smitharez"),
          holderId = Some("12345678")

      ))
    )

    val someCurrencyAmount = CurrencyAmount(
      currency = "BRL",
      amount = 33.3
    )

    val somePayment = Payment(someCurrencyAmount, 1)

    val someDeal = Deal(
      id = System.currentTimeMillis().toString,
      title = Some("some deal title"),
      description = Some("some deal description")
    )

    val someCustomer = Customer(
      email = Some("example@example.org")
    )

    val someAccessToken = "some access token"
    val someCardTokenId = "some token ID"
    val someTransactionId = "some transaction ID"

    val mercadopago: PaymentGateway = new MercadopagoGateway(
      requestFactory = requestFactory,
      oauthEndpointUrl = s"http://localhost:$mercadopagoOauthPort/",
      tokenizationEndpointUrl = s"http://localhost:$mercadopagoTokenizationPort/",
      paymentsEndpointUrl = s"http://localhost:$mercadopagoPaymentsPort/",
      merchantParser = merchantParser,
      authorizationParser = authorizationParser)

    oauthDriver.reset()
    tokenizationDriver.reset()
    paymentsDriver.reset()
  }

  "sale request via MercadoPago gateway" should {
    "gracefully fail on invalid merchant" in new Ctx {
      val errorMessage = "some error message"
      oauthDriver.aRequestFor(
        clientId = someMerchant.clientId,
        clientSecret = someMerchant.clientSecret
      ) failsWith errorMessage

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must beEqualTo(errorMessage)
      }
    }

    "gracefully fail when tokenization fails" in new Ctx {
      oauthDriver.aRequestFor(
        clientId = someMerchant.clientId,
        clientSecret = someMerchant.clientSecret
      ) returns someAccessToken
      val errorMessage = "some error message"
      tokenizationDriver.aTokenizeFor(
        card = someCreditCard,
        countryCode = someCountryCode
      ) failsWith errorMessage

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must beEqualTo(errorMessage)
      }
    }

    "gracefully fail when creating a payment fails" in new Ctx {
      oauthDriver.aRequestFor(
        clientId = someMerchant.clientId,
        clientSecret = someMerchant.clientSecret
      ) returns someAccessToken
      tokenizationDriver.aTokenizeFor(
        card = someCreditCard,
        countryCode = someCountryCode
      ) returns someCardTokenId
      val errorMessage = "some error message"
      paymentsDriver.aCreatePaymentFor(
        card = someCreditCard,
        cardTokenId = someCardTokenId,
        currencyAmount = someCurrencyAmount,
        customer = someCustomer,
        deal = someDeal
      ) failsWith errorMessage

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must beEqualTo(errorMessage)
      }
    }

    "gracefully fail when the user is not authorized" in new Ctx {
      oauthDriver.aRequestFor(
        clientId = someMerchant.clientId,
        clientSecret = someMerchant.clientSecret
      ) returns someAccessToken
      tokenizationDriver.aTokenizeFor(
        card = someCreditCard,
        countryCode = someCountryCode
      ) returns someCardTokenId
      paymentsDriver.aCreatePaymentFor(
        card = someCreditCard,
        cardTokenId = someCardTokenId,
        currencyAmount = someCurrencyAmount,
        customer = someCustomer,
        deal = someDeal
      ) failsUnauthorized()

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must contain("401")
      }
    }

    "gracefully fail when a payment is rejected" in new Ctx {
      oauthDriver.aRequestFor(
        clientId = someMerchant.clientId,
        clientSecret = someMerchant.clientSecret
      ) returns someAccessToken
      tokenizationDriver.aTokenizeFor(
        card = someCreditCard,
        countryCode = someCountryCode
      ) returns someCardTokenId
      val errorMessage = "some error message"
      paymentsDriver.aCreatePaymentFor(
        card = someCreditCard,
        cardTokenId = someCardTokenId,
        currencyAmount = someCurrencyAmount,
        customer = someCustomer,
        deal = someDeal
      ) isRejected (
        transactionId = someTransactionId,
        message = errorMessage
      )

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must beEqualTo(errorMessage)
      }
    }

    "gracefully fail when a payment is rejected due solely to invalid card number length" in new Ctx {
      oauthDriver.aRequestFor(
        clientId = someMerchant.clientId,
        clientSecret = someMerchant.clientSecret
      ) returns someAccessToken
      tokenizationDriver.aTokenizeFor(
        card = someCreditCard,
        countryCode = someCountryCode
      ) returns someCardTokenId
      paymentsDriver.aCreatePaymentFor(
        card = someCreditCard,
        cardTokenId = someCardTokenId,
        currencyAmount = someCurrencyAmount,
        customer = someCustomer,
        deal = someDeal
      ) failsOnInvalidCardNumberLength()

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry(
        check = beAnInstanceOf[PaymentRejectedException]
      )
    }

    "successfully yield a transaction ID on valid request" in new Ctx {
      oauthDriver.aRequestFor(
        clientId = someMerchant.clientId,
        clientSecret = someMerchant.clientSecret
      ) returns someAccessToken
      tokenizationDriver.aTokenizeFor(
        card = someCreditCard,
        countryCode = someCountryCode
      ) returns someCardTokenId
      paymentsDriver.aCreatePaymentFor(
        card = someCreditCard,
        cardTokenId = someCardTokenId,
        currencyAmount = someCurrencyAmount,
        customer = someCustomer,
        deal = someDeal
      ) returns someTransactionId

      mercadopago.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beASuccessfulTry(
        check = ===(someTransactionId)
      )
    }
  }

  step {
    oauthDriver.stop()
    tokenizationDriver.stop()
    paymentsDriver.stop()
  }
}
