package com.sb.mycriptoanalisi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CriptoDao {
    @Query("SELECT * FROM cripto")
    fun getAll(): Flow<List<CriptoPosseduta>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cripto: CriptoPosseduta)

    @Update
    suspend fun update(cripto: CriptoPosseduta)

    @Delete
    suspend fun delete(cripto: CriptoPosseduta)

    @Query("DELETE FROM cripto")
    suspend fun deleteAll()
}
