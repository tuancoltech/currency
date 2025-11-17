package com.crypto.demo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Query(
        """
        SELECT * FROM currencies
        WHERE list_type IN (:listTypes)
        AND (
            :searchTerm = '' OR
            name LIKE :searchTerm || '%' COLLATE NOCASE OR
            name LIKE '% ' || :searchTerm || '%' COLLATE NOCASE OR
            symbol LIKE :searchTerm || '%' COLLATE NOCASE
        )
        ORDER BY name
        """
    )
    fun observeCurrencies(
        listTypes: List<String>,
        searchTerm: String
    ): Flow<List<CurrencyInfoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(items: List<CurrencyInfoEntity>)

    @Query("DELETE FROM currencies")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM currencies")
    suspend fun count(): Int

    @Query(
        """
        SELECT * FROM currencies
        WHERE list_type IN (:listTypes)
        ORDER BY name
        """
    )
    suspend fun getCurrencies(
        listTypes: List<String>
    ): List<CurrencyInfoEntity>
}
