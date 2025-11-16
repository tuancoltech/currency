package com.crypto.demo.data.local

import com.crypto.demo.domain.model.CurrencyListType
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyTypeConvertersTest {

    private val converters = CurrencyTypeConverters()

    @Test
    fun roundTripConversionKeepsEnumValue() {
        CurrencyListType.entries.forEach { type ->
            val serialized = converters.fromListType(type)
            val restored = converters.toListType(serialized)
            assertEquals(type, restored)
        }
    }
}
