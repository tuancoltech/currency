package com.crypto.demo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CurrencyDemoThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appliesLightAndDarkColorPalettes() {
        val recordedColors = mutableListOf<Color>()

        composeRule.setContent {
            CurrencyDemoTheme(darkTheme = false) {
                recordedColors += MaterialTheme.colorScheme.primary
            }
            CurrencyDemoTheme(darkTheme = true) {
                recordedColors += MaterialTheme.colorScheme.primary
            }
        }
        composeRule.waitForIdle()

        assertEquals(Purple40, recordedColors[0])
        assertEquals(Purple80, recordedColors[1])
    }
}
