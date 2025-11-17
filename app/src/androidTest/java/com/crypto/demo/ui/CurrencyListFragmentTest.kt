package com.crypto.demo.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crypto.demo.domain.model.CurrencyInfo
import com.crypto.demo.domain.model.CurrencyListType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyListFragmentTest {

    @Test
    fun newInstanceStoresCurrencyArguments() {
        val fiat = CurrencyInfo("USD", "Dollar", "$", "USD", CurrencyListType.FIAT)
        val currencies = arrayListOf<CurrencyInfo>(fiat)
        val instance = CurrencyListFragment.newInstance(currencies)
        assertEquals(
            currencies,
            instance.arguments?.getParcelableArrayList<CurrencyInfo>(CurrencyListFragment.ARG_CURRENCIES)
        )
    }
}
