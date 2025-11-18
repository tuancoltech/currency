package com.crypto.demo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.crypto.demo.R
import com.crypto.demo.databinding.FragmentCurrencyListBinding
import com.crypto.demo.domain.model.CurrencyInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max
import kotlinx.coroutines.launch
import java.util.ArrayList

@AndroidEntryPoint
class CurrencyListFragment : Fragment() {

    private val viewModel: CurrencyListViewModel by viewModels()
    private var searchFocusListener: SearchFocusListener? = null
    private var _binding: FragmentCurrencyListBinding? = null
    private val binding get() = _binding!!
    private lateinit var backCallback: OnBackPressedCallback
    private val currencyAdapter = CurrencyListAdapter()
    private var lastReportedSearchFocus: Boolean? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        searchFocusListener = when {
            parentFragment is SearchFocusListener -> parentFragment as SearchFocusListener
            context is SearchFocusListener -> context
            else -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val currentBinding = _binding
                if (currentBinding != null) {
                    hideKeyboard(currentBinding.root)
                    currentBinding.searchEditText.clearFocus()
                }
                viewModel.onCloseSearch()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrencyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.currencyRecyclerView.apply {
            adapter = currencyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.onSearchActivated()
            }
        }
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.onSearchQueryChanged(text?.toString().orEmpty())
        }
        binding.searchInputLayout.setEndIconOnClickListener {
            hideKeyboard(binding.root)
            binding.searchEditText.clearFocus()
            viewModel.onCloseSearch()
        }
        applyInsets()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchFocusListener?.onCurrencySearchFocusChanged(false)
        lastReportedSearchFocus = null
        _binding = null
    }

    override fun onDetach() {
        searchFocusListener = null
        super.onDetach()
    }

    private fun render(state: CurrencyListUiState) = with(binding) {
        val titleText = getString(state.titleRes)
        if (!currencyTitle.text.contentEquals(titleText)) {
            currencyTitle.text = titleText
        }

        val currentQuery = searchEditText.text?.toString().orEmpty()
        if (currentQuery != state.searchQuery) {
            searchEditText.setText(state.searchQuery)
            searchEditText.setSelection(state.searchQuery.length)
        }

        currencyAdapter.submitList(state.currencies)
        currencyRecyclerView.isVisible = !state.isEmpty
        emptyStateText.isVisible = state.isEmpty

        val endIconRes =
            if (state.isSearchActive || state.searchQuery.isNotBlank()) R.drawable.ic_close else 0
        searchInputLayout.setEndIconDrawable(endIconRes)

        val shouldHandleBack = state.isSearchActive || state.searchQuery.isNotBlank()
        backCallback.isEnabled = shouldHandleBack

        if (lastReportedSearchFocus != state.isSearchActive) {
            lastReportedSearchFocus = state.isSearchActive
            searchFocusListener?.onCurrencySearchFocusChanged(state.isSearchActive)
        }
    }

    private fun applyInsets() {
        val root = binding.currencyListRoot
        val initialStart = root.paddingStart
        val initialTop = root.paddingTop
        val initialEnd = root.paddingEnd
        val initialBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(
                androidx.core.view.WindowInsetsCompat.Type.systemBars()
            )
            val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())
            ViewCompat.setPaddingRelative(
                view,
                initialStart,
                initialTop + systemBars.top,
                initialEnd,
                initialBottom + max(systemBars.bottom, ime.bottom)
            )
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun hideKeyboard(target: View? = _binding?.root) {
        val view = target ?: return
        val imm = context?.getSystemService<InputMethodManager>() ?: return
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    interface SearchFocusListener {
        fun onCurrencySearchFocusChanged(hasFocus: Boolean)
    }

    companion object {
        const val ARG_CURRENCIES = "arg_currency_items"
        const val ARG_SELECTED_DATASET = "arg_currency_dataset"
        const val FRAGMENT_TAG = "CurrencyListFragment"

        fun newInstance(currencies: ArrayList<CurrencyInfo>): CurrencyListFragment {
            return CurrencyListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_CURRENCIES, currencies)
                }
            }
        }
    }
}
