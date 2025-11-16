package com.crypto.demo.data.local

import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyMappersTest {

    @Test
    fun entityToDomainCopiesEachProperty() {
        val entity = CurrencyInfoEntity(
            id = "BTC",
            name = "Bitcoin",
            symbol = "₿",
            code = "BTC",
            listType = CurrencyListType.CRYPTO
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.symbol, domain.symbol)
        assertEquals(entity.code, domain.code)
        assertEquals(entity.listType, domain.listType)
    }

    @Test
    fun domainToEntityCopiesEachProperty() {
        val model = CurrencyInfo(
            id = "USD",
            name = "United States Dollar",
            symbol = "$",
            code = "USD",
            listType = CurrencyListType.FIAT
        )

        val entity = model.toEntity()

        assertEquals(model.id, entity.id)
        assertEquals(model.name, entity.name)
        assertEquals(model.symbol, entity.symbol)
        assertEquals(model.code, entity.code)
        assertEquals(model.listType, entity.listType)
    }
}
