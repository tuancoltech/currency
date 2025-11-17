package com.crypto.demo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.crypto.demo.R
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.ui.DemoUiState
import com.crypto.demo.ui.theme.CurrencyDemoTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DemoControlPanel(
    state: DemoUiState,
    onClear: () -> Unit,
    onSeed: () -> Unit,
    onShowCrypto: () -> Unit,
    onShowFiat: () -> Unit,
    onShowAll: () -> Unit,
    onMessageConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message?.id) {
        state.message?.let {
            onMessageConsumed()
            snackbarHostState.showSnackbar(it.text)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("demoControlPanel")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(id = R.string.control_panel_title),
                style = MaterialTheme.typography.titleMedium
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onClear, enabled = !state.isProcessing) {
                    Text(text = stringResource(id = R.string.button_clear))
                }
                Button(onClick = onSeed, enabled = !state.isProcessing) {
                    Text(text = stringResource(id = R.string.button_seed))
                }
                DatasetButton(
                    label = stringResource(id = R.string.button_crypto),
                    selected = state.selectedListType == CurrencyListType.CRYPTO,
                    onClick = onShowCrypto
                )
                DatasetButton(
                    label = stringResource(id = R.string.button_fiat),
                    selected = state.selectedListType == CurrencyListType.FIAT,
                    onClick = onShowFiat
                )
                DatasetButton(
                    label = stringResource(id = R.string.button_all),
                    selected = state.selectedListType == CurrencyListType.ALL,
                    onClick = onShowAll
                )
            }
            if (state.isProcessing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("demoProgress")
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag("demoSnackbarHost")
            )
        }
    }
}

@Composable
private fun DatasetButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    FilledTonalButton(onClick = onClick, colors = colors) {
        Text(text = label)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DemoControlPanelPreview() {
    CurrencyDemoTheme {
        DemoControlPanel(
            state = DemoUiState(selectedListType = CurrencyListType.ALL),
            onClear = {},
            onSeed = {},
            onShowCrypto = {},
            onShowFiat = {},
            onShowAll = {},
            onMessageConsumed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DatasetButtonPreview() {
    CurrencyDemoTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            DatasetButton(label = "Crypto", selected = true, onClick = {})
            DatasetButton(label = "Fiat", selected = false, onClick = {})
        }
    }
}
