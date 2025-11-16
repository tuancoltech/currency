package com.crypto.demo.ui

import androidx.lifecycle.SavedStateHandle
import com.crypto.demo.MainDispatcherRule
import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.domain.repository.CurrencyRepository
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeCurrencyRepository

    @Before
    fun setup() {
        repository = FakeCurrencyRepository()
    }

    @Test
    fun searchMatchesBeginningOfCoinName() = runTest {
        repository.setData(
            listOf(
                CurrencyInfo("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO),
                CurrencyInfo("ETC", "Ethereum Classic", "ETC", null, CurrencyListType.CRYPTO)
            )
        )
        val viewModel = viewModel(CurrencyListType.CRYPTO)

        viewModel.uiState.test {
            awaitItem() // initial
            viewModel.onSearchQueryChanged("Eth")
            val result = awaitItem()
            assertEquals(2, result.currencies.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchMatchesWordsWithPrecedingSpace() = runTest {
        repository.setData(
            listOf(
                CurrencyInfo("ETC", "Ethereum Classic", "ETC", null, CurrencyListType.CRYPTO),
                CurrencyInfo("TRX", "Tronclassic", "TRX", null, CurrencyListType.CRYPTO)
            )
        )
        val viewModel = viewModel(CurrencyListType.CRYPTO)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onSearchQueryChanged("Classic")
            val state = awaitItem()
            assertEquals(1, state.currencies.size)
            assertEquals("ETC", state.currencies.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchMatchesSymbolPrefix() = runTest {
        repository.setData(
            listOf(
                CurrencyInfo("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO),
                CurrencyInfo("BET", "Bet Coin", "BET", null, CurrencyListType.CRYPTO)
            )
        )
        val viewModel = viewModel(CurrencyListType.CRYPTO)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onSearchQueryChanged("ET")
            val state = awaitItem()
            val ids = state.currencies.map { it.id }
            assertTrue("ETH should match", ids.contains("ETH"))
            assertTrue("BET should not match", !ids.contains("BET"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun titleDefaultsToCryptoWhenDatasetMissingOrInvalid() = runTest {
        val viewModel = viewModelWithRawArg("INVALID")
        assertEquals("Crypto Currency", viewModel.uiState.value.title)
    }

    @Test
    fun fiatDatasetExposesFiatTitle() = runTest {
        val viewModel = viewModel(CurrencyListType.FIAT)
        assertEquals("Fiat Currency", viewModel.uiState.value.title)
    }

    @Test
    fun missingDatasetDefaultsToCryptoList() = runTest {
        repository.setData(
            listOf(
                CurrencyInfo("BTC", "Bitcoin", "BTC", null, CurrencyListType.CRYPTO)
            )
        )
        val viewModel = viewModelWithRawArg(null)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(CurrencyListType.CRYPTO, initial.selectedDataset)
            this@runTest.advanceUntilIdle()
            assertEquals(listOf(CurrencyListType.CRYPTO), repository.lastRequestedTypes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun allDatasetObservesBothListTypes() = runTest {
        repository.setData(
            listOf(
                CurrencyInfo("USD", "Dollar", "$", "USD", CurrencyListType.FIAT)
            )
        )
        val viewModel = viewModel(CurrencyListType.ALL)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals("Purchasable Currency", initial.title)
            this@runTest.advanceUntilIdle()
            assertEquals(
                listOf(CurrencyListType.CRYPTO, CurrencyListType.FIAT),
                repository.lastRequestedTypes
            )
            val state = awaitItem()
            assertEquals(1, state.currencies.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun subtitleUsesCodeOrFallsBackToListType() = runTest {
        repository.setData(
            listOf(
                CurrencyInfo("USD", "Dollar", "$", "USD", CurrencyListType.FIAT),
                CurrencyInfo("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO),
                CurrencyInfo("BBD", "Blank Code", "$", "", CurrencyListType.FIAT)
            )
        )
        val viewModel = viewModel(CurrencyListType.ALL)

        viewModel.uiState.test {
            awaitItem()
            val state = awaitItem()
            val usd = state.currencies.first { it.id == "USD" }
            val eth = state.currencies.first { it.id == "ETH" }
            val bbd = state.currencies.first { it.id == "BBD" }
            assertEquals("USD", usd.subtitle)
            assertEquals(CurrencyListType.CRYPTO.name, eth.subtitle)
            assertEquals(CurrencyListType.FIAT.name, bbd.subtitle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchActivationAndCloseUpdateFlags() = runTest {
        val viewModel = viewModel(CurrencyListType.CRYPTO)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.isSearchActive)
            viewModel.onSearchActivated()
            assertTrue(awaitItem().isSearchActive)
            viewModel.onSearchQueryChanged("BTC")
            awaitItem()
            viewModel.onCloseSearch()
            val cleared = awaitItem()
            assertEquals("", cleared.searchQuery)
            assertFalse(cleared.isSearchActive)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel(type: CurrencyListType): CurrencyListViewModel {
        return viewModelWithRawArg(type.name)
    }

    private fun viewModelWithRawArg(raw: String?): CurrencyListViewModel {
        val savedStateHandle = raw?.let {
            SavedStateHandle(mapOf(CurrencyListFragment.ARG_DATASET to it))
        } ?: SavedStateHandle()
        return CurrencyListViewModel(savedStateHandle, repository)
    }

    private class FakeCurrencyRepository : CurrencyRepository {
        private val items = MutableStateFlow<List<CurrencyInfo>>(emptyList())
        var lastRequestedTypes: List<CurrencyListType> = emptyList()
        var lastSearchTerm: String = ""

        fun setData(list: List<CurrencyInfo>) {
            items.value = list
        }

        override fun observeCurrencies(
            listTypes: List<CurrencyListType>,
            searchTerm: String
        ): Flow<List<CurrencyInfo>> {
            lastRequestedTypes = listTypes
            lastSearchTerm = searchTerm
            val lowerQuery = searchTerm.lowercase()
            return items.map { list ->
                list.filter { currency ->
                    currency.listType in listTypes &&
                        matches(currency, lowerQuery)
                }
            }
        }

        private fun matches(currency: CurrencyInfo, lowerQuery: String): Boolean {
            if (lowerQuery.isBlank()) return true
            val name = currency.name.lowercase()
            val symbol = currency.symbol.lowercase()
            return name.startsWith(lowerQuery) ||
                name.contains(" $lowerQuery") ||
                symbol.startsWith(lowerQuery)
        }

        override suspend fun seedCurrencies(data: List<CurrencyInfo>) {
            items.value = data
        }

        override suspend fun clearAll() {
            items.value = emptyList()
        }

        override suspend fun isEmpty(): Boolean = items.value.isEmpty()
    }
}
