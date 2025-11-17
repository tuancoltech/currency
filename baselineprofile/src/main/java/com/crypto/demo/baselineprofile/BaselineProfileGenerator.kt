package com.crypto.demo.baselineprofile

import android.os.Build
import androidx.benchmark.macro.junit4.BaselineProfileRule
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Assume.assumeNoException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @RequiresApi(Build.VERSION_CODES.P)
    @get:Rule
    val baselineRule = BaselineProfileRule()

    @RequiresApi(Build.VERSION_CODES.P)
    @Test
    fun generate() {
        try {
            baselineRule.collect(packageName = PACKAGE_NAME) {
                // Launch the default activity.
                pressHome()
                startActivityAndWait()

                // Wait for the dataset toggle buttons to be laid out.
                device.wait(
                    Until.hasObject(By.res(PACKAGE_NAME, "datasetToggleGroup")),
                    OBJECT_TIMEOUT_MS
                )

                // Exercise dataset switching and search interactions to warm important code paths.
                device.findObject(By.res(PACKAGE_NAME, "buttonShowFiat"))?.click()
                device.findObject(By.res(PACKAGE_NAME, "buttonShowAll"))?.click()

                val searchField = device.findObject(By.res(PACKAGE_NAME, "searchEditText"))
                searchField?.click()
                searchField?.text = "bit"
                device.waitForIdle()
                device.pressBack()
            }
        } catch (illegalState: IllegalStateException) {
            val message = illegalState.message.orEmpty()
            if (message.contains("pm dump-profiles")) {
                Log.w(TAG, "Skipping baseline profile generation on this device", illegalState)
                assumeNoException(
                    "pm dump-profiles output format is unsupported on this device",
                    illegalState
                )
            } else {
                throw illegalState
            }
        }
    }

    companion object {
        private const val TAG = "BaselineProfile"
        private const val PACKAGE_NAME = "com.crypto.demo"
        private const val OBJECT_TIMEOUT_MS = 5_000L
    }
}
