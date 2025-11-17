package com.crypto.demo.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleCurrencyDataTest {

    @Test
    fun allPurchasableContainsEveryCryptoAndFiatEntry() {
        val expected = SampleCurrencyData.crypto + SampleCurrencyData.fiat

        assertEquals(expected, SampleCurrencyData.allPurchasable)
    }

    @Test
    fun currencyIdentifiersAreUniqueAcrossDatasets() {
        val allIds = SampleCurrencyData.allPurchasable.map { it.id }

        assertEquals(allIds.size, allIds.toSet().size)
        assertTrue(allIds.isNotEmpty())
    }
}
