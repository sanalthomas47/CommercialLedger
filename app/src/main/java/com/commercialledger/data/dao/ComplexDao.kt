package com.commercialledger.data.dao

import androidx.room.*
import com.commercialledger.data.model.Complex
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplexDao {
    @Query("SELECT * FROM complexes ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Complex>>

    @Query("SELECT * FROM complexes WHERE id = :id")
    suspend fun getById(id: Long): Complex?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(complex: Complex): Long

    @Update
    suspend fun update(complex: Complex)

    @Delete
    suspend fun delete(complex: Complex)
}
