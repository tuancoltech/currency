package com.crypto.demo.data.local

import com.crypto.demo.domain.model.CurrencyInfo

fun CurrencyInfoEntity.toDomain() = CurrencyInfo(
    id = id,
    name = name,
    symbol = symbol,
    code = code,
    listType = listType
)

fun CurrencyInfo.toEntity() = CurrencyInfoEntity(
    id = id,
    name = name,
    symbol = symbol,
    code = code,
    listType = listType
)
