package com.santhomach.commercialledger.data.dao

import androidx.room.*
import com.santhomach.commercialledger.data.model.RoomUnit
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomUnitDao {
    @Query("SELECT * FROM room_units WHERE complexId = :complexId ORDER BY doorNumber ASC")
    fun getByComplexFlow(complexId: Long): Flow<List<RoomUnit>>

    @Query("SELECT * FROM room_units WHERE id = :id")
    suspend fun getById(id: Long): RoomUnit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(roomUnit: RoomUnit): Long

    @Update
    suspend fun update(roomUnit: RoomUnit)

    @Delete
    suspend fun delete(roomUnit: RoomUnit)
}
