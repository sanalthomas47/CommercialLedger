package com.santhomach.commercialledger.data.dao

import androidx.room.*
import com.santhomach.commercialledger.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE complexId = :complexId ORDER BY date DESC")
    fun getByComplexFlow(complexId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE roomId = :roomId ORDER BY date DESC")
    fun getByRoomFlow(roomId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE complexId = :complexId AND year = :year")
    fun getTotalForComplexYearFlow(complexId: Long, year: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE complexId = :complexId AND year = :year AND month = :month")
    fun getTotalForComplexMonthFlow(complexId: Long, year: Int, month: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE year = :year AND month = :month")
    fun getTotalForMonthFlow(year: Int, month: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE year = :year")
    fun getTotalForYearFlow(year: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses")
    fun getTotalAllTimeFlow(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}
