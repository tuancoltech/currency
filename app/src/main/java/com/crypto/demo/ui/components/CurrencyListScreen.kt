package com.crypto.demo.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crypto.demo.R
import com.crypto.demo.ui.CurrencyListUiState
import com.crypto.demo.ui.CurrencyRowItem

@Composable
fun CurrencyListScreen(
    state: CurrencyListUiState,
    onQueryChange: (String) -> Unit,
    onActivateSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onSearchFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = state.isSearchActive || state.searchQuery.isNotBlank()) {
        onCloseSearch()
    }

    LaunchedEffect(state.isSearchActive) {
        onSearchFocusChanged(state.isSearchActive)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("currencyListColumn")
        ) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CurrencySearchField(
                value = state.searchQuery,
                active = state.isSearchActive,
                onQueryChanged = onQueryChange,
                onActivate = onActivateSearch,
                onClose = onCloseSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("currencySearchField")
            )
            if (state.isEmpty) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                )
            } else {
                CurrencyList(
                    items = state.currencies,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CurrencySearchField(
    value: String,
    active: Boolean,
    onQueryChanged: (String) -> Unit,
    onActivate: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = {
            if (!active) {
                onActivate()
            }
            onQueryChanged(it)
        },
        modifier = modifier.onFocusChanged { focusState ->
            if (!active && focusState.isFocused) {
                onActivate()
            }
        },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            val icon = if (active || value.isNotBlank()) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack
            IconButton(onClick = onClose) {
                Icon(imageVector = icon, contentDescription = null)
            }
        },
        placeholder = { Text(text = stringResource(id = R.string.search_hint)) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun CurrencyList(
    items: List<CurrencyRowItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("currencyList"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(items, key = { it.id }) { item ->
            CurrencyRow(item = item)
        }
    }
}

@Composable
private fun CurrencyRow(item: CurrencyRowItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(text = item.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "${item.symbol} - ${item.subtitle}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier.testTag("currencyEmptyState")) {
        Text(
            text = stringResource(id = R.string.empty_state_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
