package com.crypto.demo.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crypto.demo.R
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.ui.components.DemoControlPanel
import com.crypto.demo.ui.theme.CurrencyDemoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DemoActivity : AppCompatActivity(), CurrencyListFragment.SearchFocusListener {

    private val viewModel: DemoViewModel by viewModels()
    private var currentListType: CurrencyListType? = null
    private lateinit var controlPanelComposeView: ComposeView
    private var isSearchFocused: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        enableEdgeToEdge()
        setContentView(R.layout.activity_demo)

        controlPanelComposeView = findViewById(R.id.controlPanelComposeView)

        controlPanelComposeView.setContent {
            CurrencyDemoTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                DemoControlPanel(
                    state = uiState,
                    onClear = viewModel::onClearDatabase,
                    onSeed = viewModel::onSeedDatabase,
                    onShowCrypto = { viewModel.onDatasetSelected(CurrencyListType.CRYPTO) },
                    onShowFiat = { viewModel.onDatasetSelected(CurrencyListType.FIAT) },
                    onShowAll = { viewModel.onDatasetSelected(CurrencyListType.ALL) },
                    onMessageConsumed = viewModel::onMessageConsumed
                )
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val target = when (state.selectedListType) {
                        CurrencyListType.CRYPTO -> CurrencyListType.CRYPTO
                        CurrencyListType.FIAT -> CurrencyListType.FIAT
                        CurrencyListType.ALL -> CurrencyListType.ALL
                    }
                    if (currentListType != target) {
                        showList(target)
                    }
                }
            }
        }
    }

    private fun showList(type: CurrencyListType) {
        currentListType = type
        supportFragmentManager.commit {
            replace(
                R.id.currencyListContainer,
                CurrencyListFragment.newInstance(type),
                CurrencyListFragment.FRAGMENT_TAG
            )
        }
    }

    override fun onCurrencySearchFocusChanged(hasFocus: Boolean) {
        if (isSearchFocused == hasFocus) return
        isSearchFocused = hasFocus
        controlPanelComposeView.isGone = hasFocus
    }
}
