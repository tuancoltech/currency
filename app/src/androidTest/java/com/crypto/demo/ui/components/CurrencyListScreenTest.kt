package com.crypto.demo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.crypto.demo.domain.model.CurrencyListType
import com.crypto.demo.ui.CurrencyListUiState
import com.crypto.demo.ui.CurrencyRowItem
import com.crypto.demo.ui.theme.CurrencyDemoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CurrencyListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersEmptyAndPopulatedStatesAndForwardsQuery() {
        val state = mutableStateOf(
            CurrencyListUiState(
                title = "Crypto Currency",
                isEmpty = true
            )
        )
        val queries = mutableListOf<String>()
        var searchActivated = false

        composeRule.setContent {
            CurrencyDemoTheme {
                CurrencyListScreen(
                    state = state.value,
                    onQueryChange = { queries += it },
                    onActivateSearch = { searchActivated = true },
                    onCloseSearch = { },
                    onSearchFocusChanged = {}
                )
            }
        }

        composeRule.onNodeWithTag("currencyEmptyState").assertExists()
        composeRule.onNode(hasSetTextAction()).performTextInput("B")
        composeRule.waitUntil { queries.contains("B") }
        assertTrue(searchActivated)

        state.value = state.value.copy(
            searchQuery = queries.last(),
            isSearchActive = true,
            isEmpty = false,
            currencies = listOf(
                CurrencyRowItem("BTC", "Bitcoin", "BTC", "₿")
            )
        )

        composeRule.onNodeWithTag("currencyList").assertExists()
    }

    @Test
    fun backHandlerClosesSearchWhenActive() {
        val state = mutableStateOf(
            CurrencyListUiState(
                title = "Crypto Currency",
                searchQuery = "BTC",
                isSearchActive = true,
                currencies = listOf(
                    CurrencyRowItem("BTC", "Bitcoin", CurrencyListType.CRYPTO.name, "₿")
                )
            )
        )
        var closeCount = 0

        composeRule.setContent {
            CurrencyDemoTheme {
                CurrencyListScreen(
                    state = state.value,
                    onQueryChange = {},
                    onActivateSearch = {},
                    onCloseSearch = { closeCount++ },
                    onSearchFocusChanged = {}
                )
            }
        }

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitUntil { closeCount == 1 }

        composeRule.runOnIdle {
            state.value = state.value.copy(isSearchActive = false, searchQuery = "")
        }
        assertEquals(1, closeCount)
    }
}
