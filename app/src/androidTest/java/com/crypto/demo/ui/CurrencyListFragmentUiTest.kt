package com.crypto.demo.ui

import android.os.SystemClock
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crypto.demo.R
import com.crypto.demo.domain.model.CurrencyListType
import org.hamcrest.Matchers.allOf
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyListFragmentUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DemoActivity::class.java)

    @Test
    fun searchFiltersListAndShowsEmptyState() {
        waitForDataset(CurrencyListType.CRYPTO)
        waitForCurrencies()

        onView(withId(R.id.currencyRecyclerView)).check(matches(isDisplayed()))

        onView(withId(R.id.searchEditText)).perform(click(), typeText("zzzz"))
        closeSoftKeyboard()

        onView(withId(R.id.emptyStateText)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(com.google.android.material.R.id.text_input_end_icon),
                isDescendantOfA(withId(R.id.searchInputLayout))
            )
        ).perform(click())

        waitForCurrencies()
        onView(withId(R.id.currencyRecyclerView)).check(matches(isDisplayed()))
    }

    private fun waitForDataset(expected: CurrencyListType) {
        val timeout = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < timeout) {
            val current = currentDataset()
            if (current == expected) return
            SystemClock.sleep(100)
        }
        fail("Dataset did not change to $expected")
    }

    private fun waitForCurrencies() {
        val timeout = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < timeout) {
            val count = currentCurrencyCount()
            if (count > 0) return
            SystemClock.sleep(100)
        }
        fail("Currencies were not loaded")
    }

    private fun currentDataset(): CurrencyListType? {
        var dataset: CurrencyListType? = null
        activityRule.scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag(
                CurrencyListFragment.FRAGMENT_TAG
            )
            dataset = fragment?.arguments
                ?.getString(CurrencyListFragment.ARG_DATASET)
                ?.let { CurrencyListType.valueOf(it) }
        }
        return dataset
    }

    private fun currentCurrencyCount(): Int {
        var count = -1
        activityRule.scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag(
                CurrencyListFragment.FRAGMENT_TAG
            )
            val recyclerView =
                fragment?.view?.findViewById<RecyclerView>(R.id.currencyRecyclerView)
            count = recyclerView?.adapter?.itemCount ?: -1
        }
        return count
    }
}
