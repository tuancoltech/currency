package com.crypto.demo.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crypto.demo.domain.model.CurrencyListType
import org.junit.Assert.assertNotNull
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
            val method = DemoActivity::class.java.getDeclaredMethod("showList", CurrencyListType::class.java)
            method.isAccessible = true
            method.invoke(activity, CurrencyListType.FIAT)
            val fragment = activity.supportFragmentManager.findFragmentByTag(
                CurrencyListFragment.FRAGMENT_TAG
            )
            assertNotNull(fragment)
        }
    }
}
