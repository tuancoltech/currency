package com.crypto.demo.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crypto.demo.R
import com.crypto.demo.databinding.ActivityDemoBinding
import com.crypto.demo.domain.model.CurrencyListType
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DemoActivity : AppCompatActivity(), CurrencyListFragment.SearchFocusListener {

    private val viewModel: DemoViewModel by viewModels()
    private var currentListType: CurrencyListType? = null
    private var isSearchFocused: Boolean = false
    private var suppressDatasetCallback = false
    private var lastRenderedVersion: Long = -1L
    private var showListJob: Job? = null
    private lateinit var binding: ActivityDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        binding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyRootInsets()
        setupControlPanel()
        observeUiState()
    }

    private fun setupControlPanel() = with(binding) {
        clearButton.setOnClickListener { viewModel.onClearDatabase() }
        seedButton.setOnClickListener { viewModel.onSeedDatabase() }
        datasetToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || suppressDatasetCallback) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.buttonShowCrypto -> viewModel.onDatasetSelected(CurrencyListType.CRYPTO)
                R.id.buttonShowFiat -> viewModel.onDatasetSelected(CurrencyListType.FIAT)
                R.id.buttonShowAll -> viewModel.onDatasetSelected(CurrencyListType.ALL)
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderControlPanel(state)
                    val requiresRefresh =
                        currentListType != state.selectedListType ||
                            lastRenderedVersion != state.datasetVersion
                    if (requiresRefresh) {
                        lastRenderedVersion = state.datasetVersion
                        showList(state.selectedListType)
                    }
                }
            }
        }
    }

    private fun renderControlPanel(state: DemoUiState) = with(binding) {
        clearButton.isEnabled = !state.isProcessing
        seedButton.isEnabled = !state.isProcessing
        demoProgress.isVisible = state.isProcessing

        val selectedButtonId = when (state.selectedListType) {
            CurrencyListType.CRYPTO -> R.id.buttonShowCrypto
            CurrencyListType.FIAT -> R.id.buttonShowFiat
            CurrencyListType.ALL -> R.id.buttonShowAll
        }
        if (datasetToggleGroup.checkedButtonId != selectedButtonId) {
            suppressDatasetCallback = true
            datasetToggleGroup.check(selectedButtonId)
            suppressDatasetCallback = false
        }

        state.message?.let { message ->
            Snackbar.make(
                controlPanelCard,
                messageText(message.type),
                Snackbar.LENGTH_SHORT
            ).show()
            viewModel.onMessageConsumed()
        }
    }

    private fun showList(type: CurrencyListType) {
        showListJob?.cancel()
        showListJob = lifecycleScope.launch {
            val currencies = viewModel.loadCurrencies(type)
            val fragment = CurrencyListFragment.newInstance(ArrayList(currencies))
            fragment.arguments = (fragment.arguments ?: Bundle()).apply {
                putString(CurrencyListFragment.ARG_SELECTED_DATASET, type.name)
            }
            currentListType = type
            supportFragmentManager.commit {
                replace(
                    R.id.currencyListContainer,
                    fragment,
                    CurrencyListFragment.FRAGMENT_TAG
                )
            }
        }
    }

    private fun applyRootInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.demoRoot) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            binding.demoContainer.setPadding(0, systemBars.top, 0, imeInsets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(binding.demoRoot)
    }

    override fun onCurrencySearchFocusChanged(hasFocus: Boolean) {
        if (isSearchFocused == hasFocus) return
        isSearchFocused = hasFocus
        binding.controlPanelCard.isGone = hasFocus
    }

    private fun messageText(type: UiMessageType): String {
        val stringRes = when (type) {
            UiMessageType.INITIAL_SEED -> R.string.message_initial_seed
            UiMessageType.SEED -> R.string.message_seed_success
            UiMessageType.CLEARED -> R.string.message_clear_success
        }
        return getString(stringRes)
    }
}
