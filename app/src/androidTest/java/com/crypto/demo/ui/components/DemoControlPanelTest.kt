package com.crypto.demo.ui.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.ui.DemoUiState
import com.crypto.demo.ui.UiMessage
import com.crypto.demo.ui.theme.CurrencyDemoTheme
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DemoControlPanelTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsControlsDisablesButtonsWhileProcessingAndConsumesMessage() {
        val messageConsumed = AtomicInteger(0)
        val datasetSelection = mutableStateOf(CurrencyListType.CRYPTO)
        val state = mutableStateOf(
            DemoUiState(
                selectedListType = CurrencyListType.CRYPTO,
                isProcessing = true,
                message = UiMessage(1, "Hello")
            )
        )

        composeRule.setContent {
            CurrencyDemoTheme {
                DemoControlPanel(
                    state = state.value,
                    onClear = {},
                    onSeed = {},
                    onShowCrypto = { datasetSelection.value = CurrencyListType.CRYPTO },
                    onShowFiat = { datasetSelection.value = CurrencyListType.FIAT },
                    onShowAll = { datasetSelection.value = CurrencyListType.ALL },
                    onMessageConsumed = { messageConsumed.incrementAndGet() }
                )
            }
        }

        composeRule.onNodeWithTag("demoControlPanel").assertIsDisplayed()
        composeRule.onNodeWithText("Clear Database").assertIsNotEnabled()
        composeRule.onNodeWithText("Seed Demo Data").assertIsNotEnabled()
        composeRule.onNodeWithTag("demoProgress").assertIsDisplayed()
        composeRule.onNodeWithText("Hello").assertIsDisplayed()
        composeRule.waitUntil { messageConsumed.get() == 1 }

        composeRule.onNodeWithText("Show Fiat").performClick()
        assertEquals(CurrencyListType.FIAT, datasetSelection.value)

        state.value = state.value.copy(isProcessing = false, message = null)
        composeRule.onNodeWithText("Clear Database").assertIsDisplayed()
        composeRule.onNodeWithText("Show Crypto").assertIsDisplayed()
    }
}
