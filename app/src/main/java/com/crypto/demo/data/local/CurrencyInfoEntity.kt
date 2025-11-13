package com.crypto.demo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crypto.demo.domain.model.CurrencyListType

@Entity(tableName = "currencies")
data class CurrencyInfoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "symbol")
    val symbol: String,
    @ColumnInfo(name = "code")
    val code: String?,
    @ColumnInfo(name = "list_type")
    val listType: CurrencyListType
)
