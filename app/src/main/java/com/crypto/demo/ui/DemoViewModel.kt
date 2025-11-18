package com.crypto.demo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crypto.demo.data.local.SampleCurrencyData
import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DemoViewModel @Inject constructor(
    private val repository: CurrencyRepository
) : ViewModel() {

    private var datasetVersion = 0L
    init {
        viewModelScope.launch {
            if (repository.isEmpty()) {
                repository.seedCurrencies(SampleCurrencyData.allPurchasable)
                showMessage(UiMessageType.INITIAL_SEED)
                updateDatasetVersion()
            }
        }
    }

    private val _uiState = MutableStateFlow(DemoUiState())
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    private var messageId = 0L

    fun onClearDatabase() {
        viewModelScope.launch {
            setLoading(true)
            repository.clearAll()
            showMessage(UiMessageType.CLEARED)
            setLoading(false)
            updateDatasetVersion()
        }
    }

    fun onSeedDatabase() {
        viewModelScope.launch {
            setLoading(true)
            repository.seedCurrencies(SampleCurrencyData.allPurchasable)
            showMessage(UiMessageType.SEED)
            setLoading(false)
            updateDatasetVersion()
        }
    }

    fun onDatasetSelected(type: CurrencyListType) {
        updateDatasetVersion(type)
    }

    suspend fun loadCurrencies(type: CurrencyListType): List<CurrencyInfo> {
        val listTypes = when (type) {
            CurrencyListType.ALL -> listOf(CurrencyListType.CRYPTO, CurrencyListType.FIAT)
            else -> listOf(type)
        }
        return repository.getCurrencies(listTypes)
    }

    fun onMessageConsumed() {
        _uiState.update { it.copy(message = null) }
    }

    private fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isProcessing = loading) }
    }

    private fun showMessage(type: UiMessageType) {
        messageId += 1
        _uiState.update { it.copy(message = UiMessage(messageId, type)) }
    }

    private fun updateDatasetVersion(newType: CurrencyListType? = null) {
        datasetVersion += 1
        _uiState.update { state ->
            state.copy(
                selectedListType = newType ?: state.selectedListType,
                datasetVersion = datasetVersion
            )
        }
    }
}

data class DemoUiState(
    val selectedListType: CurrencyListType = CurrencyListType.CRYPTO,
    val isProcessing: Boolean = false,
    val message: UiMessage? = null,
    val datasetVersion: Long = 0L
)

data class UiMessage(
    val id: Long,
    val type: UiMessageType
)

enum class UiMessageType {
    INITIAL_SEED,
    SEED,
    CLEARED
}
