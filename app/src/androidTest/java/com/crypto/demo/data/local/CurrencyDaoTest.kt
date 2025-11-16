package com.crypto.demo.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crypto.demo.domain.model.CurrencyListType
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyDaoTest {

    private lateinit var database: CurrencyDatabase
    private lateinit var dao: CurrencyDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CurrencyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.currencyDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeCurrenciesAppliesQueryRules() = runTest {
        val entities = listOf(
            CurrencyInfoEntity("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO),
            CurrencyInfoEntity("ETC", "Ethereum Classic", "ETC", null, CurrencyListType.CRYPTO),
            CurrencyInfoEntity("BTC", "Bitcoin", "BTC", null, CurrencyListType.CRYPTO)
        )
        dao.insertCurrencies(entities)

        val result = dao.observeCurrencies(
            listTypes = listOf(CurrencyListType.CRYPTO.name),
            searchTerm = "Classic"
        ).first()

        assertEquals(listOf("ETC"), result.map { it.id })

        val symbolMatches = dao.observeCurrencies(
            listTypes = listOf(CurrencyListType.CRYPTO.name),
            searchTerm = "BT"
        ).first()
        assertEquals(listOf("BTC"), symbolMatches.map { it.id })
    }

    @Test
    fun clearAllRemovesData() = runTest {
        dao.insertCurrencies(
            listOf(
                CurrencyInfoEntity("USD", "Dollar", "$", "USD", CurrencyListType.FIAT)
            )
        )
        assertEquals(1, dao.count())
        dao.clearAll()
        assertEquals(0, dao.count())
    }

    @Test
    fun emptySearchTermReturnsAllItemsSortedByName() = runTest {
        val items = listOf(
            CurrencyInfoEntity("ETH", "Ethereum", "ETH", null, CurrencyListType.CRYPTO),
            CurrencyInfoEntity("BTC", "Bitcoin", "BTC", null, CurrencyListType.CRYPTO),
            CurrencyInfoEntity("ADA", "Cardano", "ADA", null, CurrencyListType.CRYPTO)
        )
        dao.insertCurrencies(items)

        val result = dao.observeCurrencies(
            listOf(CurrencyListType.CRYPTO.name),
            ""
        ).first()

        assertEquals(listOf("Bitcoin", "Cardano", "Ethereum"), result.map { it.name })
    }
}
