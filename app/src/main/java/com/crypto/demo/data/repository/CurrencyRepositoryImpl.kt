package com.crypto.demo.data.repository

import com.crypto.demo.data.local.CurrencyDao
import com.crypto.demo.data.local.toDomain
import com.crypto.demo.data.local.toEntity
import com.crypto.demo.di.IoDispatcher
import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.domain.repository.CurrencyRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val dao: CurrencyDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CurrencyRepository {

    override fun observeCurrencies(
        listTypes: List<CurrencyListType>,
        searchTerm: String
    ): Flow<List<CurrencyInfo>> {
        val persistedTypes = resolvedListTypes(listTypes)
        return dao.observeCurrencies(persistedTypes, searchTerm.trim())
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getCurrencies(
        listTypes: List<CurrencyListType>
    ): List<CurrencyInfo> = withContext(ioDispatcher) {
        val persistedTypes = resolvedListTypes(listTypes)
        dao.getCurrencies(persistedTypes).map { it.toDomain() }
    }

    override suspend fun seedCurrencies(data: List<CurrencyInfo>) = withContext(ioDispatcher) {
        dao.insertCurrencies(data.map { it.toEntity() })
    }

    override suspend fun clearAll() = withContext(ioDispatcher) {
        dao.clearAll()
    }

    override suspend fun isEmpty(): Boolean = withContext(ioDispatcher) {
        dao.count() == 0
    }

    private fun resolvedListTypes(
        requestedTypes: List<CurrencyListType>
    ): List<String> {
        val sanitized = requestedTypes.filter { it != CurrencyListType.ALL }
        val targetTypes = if (sanitized.isEmpty()) {
            CurrencyListType.entries.filter { it != CurrencyListType.ALL }
        } else {
            sanitized
        }
        return targetTypes.map { it.name }
    }
}
