package com.crypto.demo.ui

import androidx.lifecycle.SavedStateHandle
import com.crypto.demo.MainDispatcherRule
import com.crypto.demo.R
import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun searchMatchesBeginningOfCoinName() = runTest {
        val viewModel = viewModel(
            listOf(
                CurrencyInfo("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO),
                CurrencyInfo("ETC", "Ethereum Classic", "ETC", null, CurrencyListType.CRYPTO)
            )
        )

        viewModel.onSearchQueryChanged("Eth")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.currencies.size)
    }

    @Test
    fun searchMatchesWordsWithPrecedingSpace() = runTest {
        val viewModel = viewModel(
            listOf(
                CurrencyInfo("ETC", "Ethereum Classic", "ETC", null, CurrencyListType.CRYPTO),
                CurrencyInfo("TRX", "Tronclassic", "TRX", null, CurrencyListType.CRYPTO)
            )
        )

        viewModel.onSearchQueryChanged("Classic")
        advanceUntilIdle()

        val ids = viewModel.uiState.value.currencies.map { it.id }
        assertTrue(ids.contains("ETC"))
        assertFalse(ids.contains("TRX"))
    }

    @Test
    fun searchMatchesSymbolPrefix() = runTest {
        val viewModel = viewModel(
            listOf(
                CurrencyInfo("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO),
                CurrencyInfo("BET", "Bet Coin", "BET", null, CurrencyListType.CRYPTO)
            )
        )

        viewModel.onSearchQueryChanged("ET")
        advanceUntilIdle()

        val ids = viewModel.uiState.value.currencies.map { it.id }
        assertTrue(ids.contains("ETH"))
        assertFalse(ids.contains("BET"))
    }

    @Test
    fun datasetDefaultsToCryptoWhenMissing() {
        val viewModel = viewModel(emptyList())
        assertEquals(R.string.currency_title_crypto, viewModel.uiState.value.titleRes)
        assertEquals(CurrencyListType.CRYPTO, viewModel.uiState.value.selectedDataset)
    }

    @Test
    fun datasetMatchesProvidedType() {
        val viewModel = viewModel(
            currencies = emptyList(),
            selectedType = CurrencyListType.FIAT
        )
        assertEquals(R.string.currency_title_fiat, viewModel.uiState.value.titleRes)
        assertEquals(CurrencyListType.FIAT, viewModel.uiState.value.selectedDataset)
    }

    @Test
    fun mixedCurrenciesInferredAsAll() {
        val viewModel = viewModel(
            listOf(
                CurrencyInfo("USD", "Dollar", "$", "USD", CurrencyListType.FIAT),
                CurrencyInfo("BTC", "Bitcoin", "BTC", null, CurrencyListType.CRYPTO)
            )
        )

        assertEquals(CurrencyListType.ALL, viewModel.uiState.value.selectedDataset)
        assertEquals(R.string.currency_title_all, viewModel.uiState.value.titleRes)
    }

    @Test
    fun singleDatasetIsInferredFromCurrencies() {
        val viewModel = viewModel(
            listOf(
                CurrencyInfo("USD", "Dollar", "$", "USD", CurrencyListType.FIAT)
            )
        )

        assertEquals(CurrencyListType.FIAT, viewModel.uiState.value.selectedDataset)
        assertEquals(R.string.currency_title_fiat, viewModel.uiState.value.titleRes)
    }

    @Test
    fun subtitleUsesCodeOrFallsBackToType() {
        val viewModel = viewModel(
            listOf(
                CurrencyInfo("USD", "Dollar", "$", "USD", CurrencyListType.FIAT),
                CurrencyInfo("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO),
                CurrencyInfo("BBD", "Blank Code", "$", "", CurrencyListType.FIAT)
            )
        )

        val state = viewModel.uiState.value
        val usd = state.currencies.first { it.id == "USD" }
        val eth = state.currencies.first { it.id == "ETH" }
        val bbd = state.currencies.first { it.id == "BBD" }
        assertEquals("USD", usd.subtitle)
        assertEquals(CurrencyListType.CRYPTO.name, eth.subtitle)
        assertEquals(CurrencyListType.FIAT.name, bbd.subtitle)
    }

    @Test
    fun searchActivationAndCloseUpdateFlags() = runTest {
        val viewModel = viewModel(
            listOf(CurrencyInfo("BTC", "Bitcoin", "BTC", null, CurrencyListType.CRYPTO))
        )

        assertFalse(viewModel.uiState.value.isSearchActive)
        viewModel.onSearchActivated()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSearchActive)
        viewModel.onSearchQueryChanged("BTC")
        advanceUntilIdle()
        viewModel.onCloseSearch()
        advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertFalse(viewModel.uiState.value.isSearchActive)
    }

    @Test
    fun blankQueryAutomaticallyDeactivatesSearch() = runTest {
        val viewModel = viewModel(
            listOf(CurrencyInfo("BTC", "Bitcoin", "BTC", null, CurrencyListType.CRYPTO))
        )

        viewModel.onSearchQueryChanged("B")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSearchActive)
        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSearchActive)
    }

    @Test
    fun invalidDatasetArgumentDefaultsToCrypto() {
        val viewModel = viewModel(
            currencies = emptyList(),
            rawSelectedType = "INVALID"
        )

        assertEquals(CurrencyListType.CRYPTO, viewModel.uiState.value.selectedDataset)
        assertEquals(R.string.currency_title_crypto, viewModel.uiState.value.titleRes)
    }

    @Test
    fun missingCurrencyArgumentDefaultsToEmptyList() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = CurrencyListViewModel(savedStateHandle)

        assertTrue(viewModel.uiState.value.currencies.isEmpty())
        assertEquals(CurrencyListType.CRYPTO, viewModel.uiState.value.selectedDataset)
    }

    private fun viewModel(
        currencies: List<CurrencyInfo>,
        selectedType: CurrencyListType? = null,
        rawSelectedType: String? = null
    ): CurrencyListViewModel {
        val args = mutableMapOf<String, Any>(
            CurrencyListFragment.ARG_CURRENCIES to ArrayList(currencies)
        )
        val resolvedRaw = rawSelectedType ?: selectedType?.name
        if (resolvedRaw != null) {
            args[CurrencyListFragment.ARG_SELECTED_DATASET] = resolvedRaw
        }
        val savedStateHandle = SavedStateHandle(args)
        return CurrencyListViewModel(savedStateHandle)
    }
}
