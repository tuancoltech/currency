package com.crypto.demo.ui

import com.crypto.demo.MainDispatcherRule
import com.crypto.demo.data.local.SampleCurrencyData
import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.domain.repository.CurrencyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DemoViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository = FakeCurrencyRepository()

    @Test
    fun initSeedsDatabaseWhenEmpty() = runTest {
        repository.isEmptyReturn = true

        val viewModel = DemoViewModel(repository)
        advanceUntilIdle()

        assertEquals(SampleCurrencyData.allPurchasable.size, repository.lastSeed?.size)
        assertEquals("Initial dataset loaded", viewModel.uiState.value.message?.text)
    }

    @Test
    fun onSeedDatabasePopulatesDataAndShowsMessage() = runTest {
        repository.isEmptyReturn = false
        val viewModel = DemoViewModel(repository)
        advanceUntilIdle()

        viewModel.onSeedDatabase()
        advanceUntilIdle()

        val message = requireNotNull(viewModel.uiState.value.message)
        assertEquals(SampleCurrencyData.allPurchasable.size, repository.lastSeed?.size)
        assertTrue(message.text.contains("Demo data"))
        assertTrue(!viewModel.uiState.value.isProcessing)
    }

    @Test
    fun onClearDatabaseClearsDataAndTogglesLoader() = runTest {
        repository.isEmptyReturn = false
        val viewModel = DemoViewModel(repository)
        advanceUntilIdle()

        viewModel.onClearDatabase()
        advanceUntilIdle()

        val message = requireNotNull(viewModel.uiState.value.message)
        assertEquals(1, repository.clearCount)
        assertTrue(message.text.contains("cleared"))
        assertTrue(!viewModel.uiState.value.isProcessing)
    }

    @Test
    fun datasetSelectionUpdatesState() = runTest {
        repository.isEmptyReturn = false
        val viewModel = DemoViewModel(repository)
        advanceUntilIdle()

        viewModel.onDatasetSelected(CurrencyListType.ALL)
        advanceUntilIdle()

        assertEquals(CurrencyListType.ALL, viewModel.uiState.value.selectedListType)
    }

    @Test
    fun messageCanBeConsumed() = runTest {
        repository.isEmptyReturn = false
        val viewModel = DemoViewModel(repository)
        advanceUntilIdle()

        viewModel.onSeedDatabase()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.message != null)
        viewModel.onMessageConsumed()
        assertNull(viewModel.uiState.value.message)
    }

    private class FakeCurrencyRepository : CurrencyRepository {
        private val items = MutableStateFlow<List<CurrencyInfo>>(emptyList())
        var lastSeed: List<CurrencyInfo>? = null
        var clearCount = 0
        var isEmptyReturn: Boolean = true

        override fun observeCurrencies(
            listTypes: List<CurrencyListType>,
            searchTerm: String
        ): Flow<List<CurrencyInfo>> = items

        override suspend fun seedCurrencies(data: List<CurrencyInfo>) {
            lastSeed = data
        }

        override suspend fun clearAll() {
            clearCount += 1
            items.value = emptyList()
        }

        override suspend fun isEmpty(): Boolean = isEmptyReturn
    }
}
