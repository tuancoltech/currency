package com.crypto.demo.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CurrencyRepository
) : ViewModel() {

    private val requestedType = savedStateHandle.get<String>(CurrencyListFragment.ARG_DATASET)
        ?.let { runCatching { CurrencyListType.valueOf(it) }.getOrElse { CurrencyListType.CRYPTO } }
        ?: CurrencyListType.CRYPTO

    private val listTypes: List<CurrencyListType> = when (requestedType) {
        CurrencyListType.ALL -> listOf(CurrencyListType.CRYPTO, CurrencyListType.FIAT)
        else -> listOf(requestedType)
    }

    private val searchQuery = MutableStateFlow("")
    private val isSearchActive = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currencies = searchQuery
        .flatMapLatest { query ->
            repository.observeCurrencies(listTypes, query)
        }

    val uiState: StateFlow<CurrencyListUiState> = combine(
        currencies,
        searchQuery,
        isSearchActive
    ) { items, query, searching ->
        CurrencyListUiState(
            title = titleFor(requestedType),
            currencies = items.map { it.toUiModel() },
            searchQuery = query,
            isSearchActive = searching,
            isEmpty = items.isEmpty(),
            selectedDataset = requestedType
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CurrencyListUiState(
            title = titleFor(requestedType),
            selectedDataset = requestedType
        )
    )

    fun onSearchQueryChanged(value: String) {
        searchQuery.value = value
        isSearchActive.value = value.isNotBlank()
    }

    fun onSearchActivated() {
        isSearchActive.value = true
    }

    fun onCloseSearch() {
        searchQuery.value = ""
        isSearchActive.value = false
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
