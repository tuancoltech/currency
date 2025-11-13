package com.crypto.demo.di

import android.content.Context
import androidx.room.Room
import com.crypto.demo.data.local.CurrencyDao
import com.crypto.demo.data.local.CurrencyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCurrencyDatabase(
        @ApplicationContext context: Context
    ): CurrencyDatabase = Room.databaseBuilder(
        context,
        CurrencyDatabase::class.java,
        "currency-db"
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideCurrencyDao(database: CurrencyDatabase): CurrencyDao = database.currencyDao()
}
