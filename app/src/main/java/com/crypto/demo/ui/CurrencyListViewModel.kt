package com.crypto.demo.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.ArrayList
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialCurrencies =
        savedStateHandle.get<ArrayList<CurrencyInfo>>(CurrencyListFragment.ARG_CURRENCIES)
            ?.toList()
            ?: emptyList()

    private val requestedDataset =
        savedStateHandle.get<String>(CurrencyListFragment.ARG_SELECTED_DATASET)
            ?.let { runCatching { CurrencyListType.valueOf(it) }.getOrNull() }

    private val baseCurrencies = initialCurrencies.sortedBy { it.name }
    private val selectedDataset: CurrencyListType =
        requestedDataset ?: inferDataset(baseCurrencies)

    private var currentQuery: String = ""
    private var currentSearchActive: Boolean = false

    private val _uiState = MutableStateFlow(
        CurrencyListUiState(
            title = titleFor(selectedDataset),
            currencies = baseCurrencies.map { it.toUiModel() },
            isEmpty = baseCurrencies.isEmpty(),
            selectedDataset = selectedDataset
        )
    )
    val uiState: StateFlow<CurrencyListUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(value: String) {
        currentQuery = value
        currentSearchActive = value.isNotBlank()
        publishState()
    }

    fun onSearchActivated() {
        currentSearchActive = true
        publishState()
    }

    fun onCloseSearch() {
        currentQuery = ""
        currentSearchActive = false
        publishState()
    }

    private fun titleFor(type: CurrencyListType): String = when (type) {
        CurrencyListType.CRYPTO -> "Crypto Currency"
        CurrencyListType.FIAT -> "Fiat Currency"
        CurrencyListType.ALL -> "Purchasable Currency"
    }

    private fun CurrencyInfo.toUiModel(): CurrencyRowItem = CurrencyRowItem(
        id = id,
        title = name,
        subtitle = subtitleFor(this),
        symbol = symbol
    )

    private fun subtitleFor(item: CurrencyInfo): String {
        return item.code?.takeIf { it.isNotBlank() } ?: item.listType.name
    }

    private fun inferDataset(items: List<CurrencyInfo>): CurrencyListType {
        val distinctTypes = items.map { it.listType }.distinct()
        return when {
            distinctTypes.isEmpty() -> CurrencyListType.CRYPTO
            distinctTypes.size == 1 -> distinctTypes.first()
            else -> CurrencyListType.ALL
        }
    }

    private fun filterCurrencies(
        items: List<CurrencyInfo>,
        query: String
    ): List<CurrencyInfo> {
        if (query.isBlank()) return items
        val lowered = query.lowercase()
        return items.filter { currency ->
            val name = currency.name.lowercase()
            val symbol = currency.symbol.lowercase()
            name.startsWith(lowered) ||
                name.contains(" $lowered") ||
                symbol.startsWith(lowered)
        }
    }

    private fun publishState() {
        val filtered = filterCurrencies(baseCurrencies, currentQuery)
        _uiState.value = CurrencyListUiState(
            title = titleFor(selectedDataset),
            currencies = filtered.map { it.toUiModel() },
            searchQuery = currentQuery,
            isSearchActive = currentSearchActive,
            isEmpty = filtered.isEmpty(),
            selectedDataset = selectedDataset
        )
    }
}

data class CurrencyListUiState(
    val title: String = "",
    val currencies: List<CurrencyRowItem> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isEmpty: Boolean = false,
    val selectedDataset: CurrencyListType = CurrencyListType.CRYPTO
)

data class CurrencyRowItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val symbol: String
)
