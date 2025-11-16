package com.crypto.demo.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.crypto.demo.domain.model.CurrencyListType
import org.junit.Rule
import org.junit.Test

class DemoActivityComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<DemoActivity>()

    @Test
    fun datasetButtonsSwapFragmentsForEachListType() {
        waitForDataset(CurrencyListType.CRYPTO)

        composeRule.onNodeWithText("Show Fiat").performClick()
        waitForDataset(CurrencyListType.FIAT)

        composeRule.onNodeWithText("Show All Purchasable").performClick()
        waitForDataset(CurrencyListType.ALL)

        composeRule.onNodeWithText("Show Crypto").performClick()
        waitForDataset(CurrencyListType.CRYPTO)
    }

    private fun waitForDataset(expected: CurrencyListType) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            currentDataset() == expected
        }
    }

    private fun currentDataset(): CurrencyListType? {
        var dataset: CurrencyListType? = null
        composeRule.activityRule.scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag(
                CurrencyListFragment.FRAGMENT_TAG
            )
            dataset = fragment?.arguments
                ?.getString(CurrencyListFragment.ARG_DATASET)
                ?.let { CurrencyListType.valueOf(it) }
        }
        return dataset
    }
}
