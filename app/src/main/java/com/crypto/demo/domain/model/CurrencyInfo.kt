package com.crypto.demo.domain.model

enum class CurrencyListType {
    CRYPTO,
    FIAT,
    ALL
}

data class CurrencyInfo(
    val id: String,
    val name: String,
    val symbol: String,
    val code: String?,
    val listType: CurrencyListType
)
