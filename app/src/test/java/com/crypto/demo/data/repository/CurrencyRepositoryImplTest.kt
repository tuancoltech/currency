package com.crypto.demo.data.repository

import com.crypto.demo.data.local.CurrencyDao
import com.crypto.demo.data.local.CurrencyInfoEntity
import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyRepositoryImplTest {

    @Test
    fun observeCurrenciesDefaultsToNonAllTypesAndTrimsQuery() = runTest {
        val (dao, repository) = createSubject()
        val targetEntity = CurrencyInfoEntity(
            id = "BTC",
            name = "Bitcoin",
            symbol = "BTC",
            code = null,
            listType = CurrencyListType.CRYPTO
        )
        dao.emit(listOf(targetEntity))

        val result = repository.observeCurrencies(emptyList(), "   btc ").first()

        assertEquals(listOf(CurrencyListType.CRYPTO.name, CurrencyListType.FIAT.name), dao.lastListTypes)
        assertEquals("btc", dao.lastSearchTerm)
        assertEquals("BTC", result.single().id)
    }

    @Test
    fun observeCurrenciesRemovesAllFlag() = runTest {
        val (dao, repository) = createSubject()
        repository.observeCurrencies(
            listOf(CurrencyListType.ALL, CurrencyListType.FIAT),
            "usd"
        ).first()

        assertEquals(listOf(CurrencyListType.FIAT.name), dao.lastListTypes)
    }

    @Test
    fun observeCurrenciesKeepsProvidedListWhenNonEmpty() = runTest {
        val (dao, repository) = createSubject()
        val entity = CurrencyInfoEntity(
            id = "USD",
            name = "Dollar",
            symbol = "USD",
            code = "USD",
            listType = CurrencyListType.FIAT
        )
        dao.emit(listOf(entity))

        val result = repository.observeCurrencies(
            listOf(CurrencyListType.FIAT),
            "  usd"
        ).first()

        assertEquals(listOf(CurrencyListType.FIAT.name), dao.lastListTypes)
        assertEquals("USD", result.single().id)
    }

    @Test
    fun observeCurrenciesFallsBackWhenOnlyAllRequested() = runTest {
        val (dao, repository) = createSubject()
        repository.observeCurrencies(
            listOf(CurrencyListType.ALL),
            "   "
        ).first()

        assertEquals(
            listOf(CurrencyListType.CRYPTO.name, CurrencyListType.FIAT.name),
            dao.lastListTypes
        )
    }

    @Test
    fun observeCurrenciesSupportsSuspendingCollectors() = runTest {
        val (dao, repository) = createSubject()
        val entity = CurrencyInfoEntity(
            id = "CAD",
            name = "Canadian Dollar",
            symbol = "CAD",
            code = "CAD",
            listType = CurrencyListType.FIAT
        )
        dao.emit(listOf(entity))

        val emissions = mutableListOf<List<CurrencyInfo>>()
        repository.observeCurrencies(listOf(CurrencyListType.FIAT), "")
            .take(1)
            .collect { values ->
                delay(1)
                emissions += values
            }

        assertEquals("CAD", emissions.single().single().id)
    }

    @Test
    fun observeCurrenciesCompletesFiniteDaoFlowWithoutSuspension() = runTest {
        val entity = CurrencyInfoEntity(
            id = "BRL",
            name = "Brazilian Real",
            symbol = "R$",
            code = "BRL",
            listType = CurrencyListType.FIAT
        )
        val dao = FiniteCurrencyDao(listOf(listOf(entity)))
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = CurrencyRepositoryImpl(dao, dispatcher)
        val emissions = mutableListOf<List<CurrencyInfo>>()

        repository.observeCurrencies(emptyList(), "")
            .toList(emissions)

        assertEquals("BRL", emissions.single().single().id)
    }

    @Test
    fun seedCurrenciesSavesMappedEntities() = runTest {
        val (dao, repository) = createSubject()
        val input = listOf(
            CurrencyInfo("USD", "Dollar", "$", "USD", CurrencyListType.FIAT),
            CurrencyInfo("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO)
        )

        repository.seedCurrencies(input)

        assertEquals(2, dao.insertedEntities.single().size)
        assertTrue(dao.insertedEntities.single().any { it.id == "USD" && it.code == "USD" })
    }

    @Test
    fun seedCurrenciesUpdatesObservedData() = runTest {
        val (dao, repository) = createSubject()
        val fiat = CurrencyInfo("SGD", "Singapore Dollar", "$", "SGD", CurrencyListType.FIAT)

        repository.seedCurrencies(listOf(fiat))

        val observed = repository.observeCurrencies(listOf(CurrencyListType.FIAT), "").first()
        assertEquals("SGD", observed.single().id)
    }

    @Test
    fun seedCurrenciesSwitchesToIoDispatcher() = runTest {
        val dispatcher = RecordingDispatcher()
        val (dao, repository) = createSubject(dispatcher)
        val fiat = CurrencyInfo("JPY", "Yen", "¥", "JPY", CurrencyListType.FIAT)

        repository.seedCurrencies(listOf(fiat))

        assertTrue("Expected dispatcher to handle seedCurrencies context switch", dispatcher.dispatchCount > 0)
        assertEquals(1, dao.insertedEntities.last().size)
    }

    @Test
    fun clearAllDelegatesToDao() = runTest {
        val (dao, repository) = createSubject()
        repository.clearAll()
        assertEquals(1, dao.clearCount)
    }

    @Test
    fun clearAllSwitchesToIoDispatcher() = runTest {
        val dispatcher = RecordingDispatcher()
        val (_, repository) = createSubject(dispatcher)

        repository.clearAll()

        assertTrue("Expected dispatcher to handle clearAll context switch", dispatcher.dispatchCount > 0)
    }

    @Test
    fun isEmptyReflectsDaoCount() = runTest {
        val (dao, repository) = createSubject()
        dao.countValue = 0
        assertTrue(repository.isEmpty())

        dao.countValue = 5
        assertTrue(!repository.isEmpty())
    }

    @Test
    fun isEmptySwitchesToIoDispatcher() = runTest {
        val dispatcher = RecordingDispatcher()
        val (dao, repository) = createSubject(dispatcher)
        dao.countValue = 0

        repository.isEmpty()

        assertTrue("Expected dispatcher to handle isEmpty context switch", dispatcher.dispatchCount > 0)
    }

    private fun TestScope.createSubject(
        dispatcher: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
    ): Pair<RecordingCurrencyDao, CurrencyRepositoryImpl> {
        val dao = RecordingCurrencyDao()
        val repository = CurrencyRepositoryImpl(dao, dispatcher)
        return dao to repository
    }

    private class RecordingCurrencyDao : CurrencyDao {
        private val flow = MutableStateFlow<List<CurrencyInfoEntity>>(emptyList())
        var lastListTypes: List<String> = emptyList()
        var lastSearchTerm: String = ""
        val insertedEntities = mutableListOf<List<CurrencyInfoEntity>>()
        var clearCount = 0
        var countValue = 0

        fun emit(list: List<CurrencyInfoEntity>) {
            flow.value = list
        }

        override fun observeCurrencies(
            listTypes: List<String>,
            searchTerm: String
        ): Flow<List<CurrencyInfoEntity>> {
            lastListTypes = listTypes
            lastSearchTerm = searchTerm
            return flow
        }

        override suspend fun insertCurrencies(items: List<CurrencyInfoEntity>) {
            insertedEntities += items
            flow.value = items
        }

        override suspend fun clearAll() {
            clearCount += 1
            flow.value = emptyList()
        }

        override suspend fun count(): Int = countValue
    }

    private class FiniteCurrencyDao(
        private val emissions: List<List<CurrencyInfoEntity>>
    ) : CurrencyDao {
        override fun observeCurrencies(
            listTypes: List<String>,
            searchTerm: String
        ): Flow<List<CurrencyInfoEntity>> = flowOf(*emissions.toTypedArray())

        override suspend fun insertCurrencies(items: List<CurrencyInfoEntity>) = Unit

        override suspend fun clearAll() = Unit

        override suspend fun count(): Int = emissions.sumOf { it.size }
    }

    private class RecordingDispatcher : CoroutineDispatcher() {
        var dispatchCount = 0

        override fun isDispatchNeeded(context: CoroutineContext): Boolean = true

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            dispatchCount += 1
            block.run()
        }
    }
}
