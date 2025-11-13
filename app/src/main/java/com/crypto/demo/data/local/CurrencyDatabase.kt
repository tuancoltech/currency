package com.crypto.demo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CurrencyInfoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(CurrencyTypeConverters::class)
abstract class CurrencyDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
}
