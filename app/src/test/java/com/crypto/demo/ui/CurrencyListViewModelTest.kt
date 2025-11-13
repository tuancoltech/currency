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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository = FakeCurrencyRepository()

    @Test
    fun `search matches beginning of coin name`() = runTest {
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
    fun `search matches words with preceding space`() = runTest {
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
    fun `search matches symbol prefix`() = runTest {
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

    private fun viewModel(type: CurrencyListType): CurrencyListViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(CurrencyListFragment.ARG_DATASET to type.name))
        return CurrencyListViewModel(savedStateHandle, repository)
    }

    private class FakeCurrencyRepository : CurrencyRepository {
        private val items = MutableStateFlow<List<CurrencyInfo>>(emptyList())

        fun setData(list: List<CurrencyInfo>) {
            items.value = list
        }

        override fun observeCurrencies(
            listTypes: List<CurrencyListType>,
            searchTerm: String
        ): Flow<List<CurrencyInfo>> {
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
