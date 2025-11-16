package com.crypto.demo.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseModuleTest {

    @Test
    fun provideCurrencyDatabaseReturnsRoomInstance() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = DatabaseModule.provideCurrencyDatabase(context)
        try {
            val dao = DatabaseModule.provideCurrencyDao(database)
            assertNotNull(dao)
        } finally {
            database.close()
        }
    }
}
