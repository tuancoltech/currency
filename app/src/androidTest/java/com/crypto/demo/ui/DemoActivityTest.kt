package com.crypto.demo.ui

import android.os.SystemClock
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crypto.demo.R
import com.crypto.demo.domain.model.CurrencyListType
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DemoActivity::class.java)

    @Test
    fun showListReplacesFragmentContainer() {
        activityRule.scenario.onActivity { activity ->
            val method =
                DemoActivity::class.java.getDeclaredMethod("showList", CurrencyListType::class.java)
            method.isAccessible = true
            method.invoke(activity, CurrencyListType.FIAT)
        }
        waitForDataset(CurrencyListType.FIAT)
    }

    @Test
    fun datasetButtonsSwapFragmentsForEachListType() {
        waitForDataset(CurrencyListType.CRYPTO)

        onView(withId(R.id.buttonShowFiat)).perform(click())
        waitForDataset(CurrencyListType.FIAT)

        onView(withId(R.id.buttonShowAll)).perform(click())
        waitForDataset(CurrencyListType.ALL)

        onView(withId(R.id.buttonShowCrypto)).perform(click())
        waitForDataset(CurrencyListType.CRYPTO)
    }

    private fun waitForDataset(expected: CurrencyListType) {
        val timeout = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < timeout) {
            val current = currentDataset()
            if (current == expected) {
                return
            }
            SystemClock.sleep(100)
        }
        fail("Timed out waiting for dataset $expected")
    }

    private fun currentDataset(): CurrencyListType? {
        var dataset: CurrencyListType? = null
        activityRule.scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag(
                CurrencyListFragment.FRAGMENT_TAG
            )
            dataset = fragment?.arguments
                ?.getString(CurrencyListFragment.ARG_SELECTED_DATASET)
                ?.let { runCatching { CurrencyListType.valueOf(it) }.getOrNull() }
        }
        return dataset
    }
}
