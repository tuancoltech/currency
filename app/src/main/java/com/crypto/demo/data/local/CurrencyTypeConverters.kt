package com.crypto.demo.data.local

import androidx.room.TypeConverter
import com.crypto.demo.domain.model.CurrencyListType

class CurrencyTypeConverters {
    @TypeConverter
    fun fromListType(type: CurrencyListType): String = type.name

    @TypeConverter
    fun toListType(value: String): CurrencyListType = CurrencyListType.valueOf(value)
}
