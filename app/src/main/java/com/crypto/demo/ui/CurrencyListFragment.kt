package com.crypto.demo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.os.bundleOf
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.ui.components.CurrencyListScreen
import com.crypto.demo.ui.theme.CurrencyDemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyListFragment : Fragment() {

    private val viewModel: CurrencyListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            CurrencyDemoTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CurrencyListScreen(
                    state = state,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onActivateSearch = viewModel::onSearchActivated,
                    onCloseSearch = viewModel::onCloseSearch
                )
            }
        }
    }

    companion object {
        const val ARG_DATASET = "arg_currency_dataset"
        const val FRAGMENT_TAG = "CurrencyListFragment"

        fun newInstance(type: CurrencyListType): CurrencyListFragment {
            return CurrencyListFragment().apply {
                arguments = bundleOf(ARG_DATASET to type.name)
            }
        }
    }
}
