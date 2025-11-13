package com.crypto.demo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crypto.demo.data.local.SampleCurrencyData
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

    init {
        viewModelScope.launch {
            if (repository.isEmpty()) {
                repository.seedCurrencies(SampleCurrencyData.allPurchasable)
                showMessage("Initial dataset loaded")
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
            showMessage("Local database cleared")
            setLoading(false)
        }
    }

    fun onSeedDatabase() {
        viewModelScope.launch {
            setLoading(true)
            repository.seedCurrencies(SampleCurrencyData.allPurchasable)
            showMessage("Demo data inserted")
            setLoading(false)
        }
    }

    fun onDatasetSelected(type: CurrencyListType) {
        _uiState.update { it.copy(selectedListType = type) }
    }

    fun onMessageConsumed() {
        _uiState.update { it.copy(message = null) }
    }

    private fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isProcessing = loading) }
    }

    private fun showMessage(text: String) {
        messageId += 1
        _uiState.update { it.copy(message = UiMessage(messageId, text)) }
    }
}

data class DemoUiState(
    val selectedListType: CurrencyListType = CurrencyListType.CRYPTO,
    val isProcessing: Boolean = false,
    val message: UiMessage? = null
)

data class UiMessage(
    val id: Long,
    val text: String
)
