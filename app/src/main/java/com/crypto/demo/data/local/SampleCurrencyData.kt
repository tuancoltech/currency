package com.crypto.demo.data.local

import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType

object SampleCurrencyData {
    val crypto: List<CurrencyInfo> = listOf(
        CurrencyInfo("BTC", "Bitcoin", "BTC", null, CurrencyListType.CRYPTO),
        CurrencyInfo("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO),
        CurrencyInfo("XRP", "XRP", "XRP", null, CurrencyListType.CRYPTO),
        CurrencyInfo("BCH", "Bitcoin Cash", "BCH", null, CurrencyListType.CRYPTO),
        CurrencyInfo("LTC", "Litecoin", "LTC", null, CurrencyListType.CRYPTO),
        CurrencyInfo("EOS", "EOS", "EOS", null, CurrencyListType.CRYPTO),
        CurrencyInfo("BNB", "Binance Coin", "BNB", null, CurrencyListType.CRYPTO),
        CurrencyInfo("LINK", "Chainlink", "LINK", null, CurrencyListType.CRYPTO),
        CurrencyInfo("NEO", "NEO", "NEO", null, CurrencyListType.CRYPTO),
        CurrencyInfo("ETC", "Ethereum Classic", "ETC", null, CurrencyListType.CRYPTO),
        CurrencyInfo("ONT", "Ontology", "ONT", null, CurrencyListType.CRYPTO),
        CurrencyInfo("CRO", "Crypto.com Chain", "CRO", null, CurrencyListType.CRYPTO),
        CurrencyInfo("CUC", "Cucumber", "CUC", null, CurrencyListType.CRYPTO),
        CurrencyInfo("USDC", "USD Coin", "USDC", null, CurrencyListType.CRYPTO)
    )

    val fiat: List<CurrencyInfo> = listOf(
        CurrencyInfo("SGD", "Singapore Dollar", "$", "SGD", CurrencyListType.FIAT),
        CurrencyInfo("EUR", "Euro", "€", "EUR", CurrencyListType.FIAT),
        CurrencyInfo("GBP", "British Pound", "£", "GBP", CurrencyListType.FIAT),
        CurrencyInfo("HKD", "Hong Kong Dollar", "$", "HKD", CurrencyListType.FIAT),
        CurrencyInfo("JPY", "Japanese Yen", "¥", "JPY", CurrencyListType.FIAT),
        CurrencyInfo("AUD", "Australian Dollar", "$", "AUD", CurrencyListType.FIAT),
        CurrencyInfo("USD", "United States Dollar", "$", "USD", CurrencyListType.FIAT)
    )

    val allPurchasable: List<CurrencyInfo> = crypto + fiat
}
