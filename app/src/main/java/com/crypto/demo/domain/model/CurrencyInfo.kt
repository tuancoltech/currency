package com.crypto.demo.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class CurrencyListType {
    CRYPTO,
    FIAT,
    ALL
}

@Parcelize
data class CurrencyInfo(
    val id: String,
    val name: String,
    val symbol: String,
    val code: String?,
    val listType: CurrencyListType
) : Parcelable
