package com.crypto.demo.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crypto.demo.domain.model.CurrencyListType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyListFragmentTest {

    @Test
    fun newInstanceStoresDatasetArgumentAndComposes() {
        val instance = CurrencyListFragment.newInstance(CurrencyListType.ALL)
        assertEquals(
            CurrencyListType.ALL.name,
            instance.arguments?.getString(CurrencyListFragment.ARG_DATASET)
        )
    }
}
