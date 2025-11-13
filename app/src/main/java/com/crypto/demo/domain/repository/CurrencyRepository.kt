package com.crypto.demo.domain.repository

import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun observeCurrencies(
        listTypes: List<CurrencyListType>,
        searchTerm: String
    ): Flow<List<CurrencyInfo>>

    suspend fun seedCurrencies(data: List<CurrencyInfo>)

    suspend fun clearAll()

    suspend fun isEmpty(): Boolean
}
