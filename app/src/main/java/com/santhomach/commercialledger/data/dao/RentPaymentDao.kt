package com.santhomach.commercialledger.data.dao

import androidx.room.*
import com.santhomach.commercialledger.data.model.RentPayment
import kotlinx.coroutines.flow.Flow

/** Total paid towards one tenancy for a given rent month. */
data class TenancyPaidTotal(val tenancyId: Long, val total: Long)

@Dao
interface RentPaymentDao {
    @Query("SELECT * FROM rent_payments WHERE tenancyId = :tenancyId ORDER BY year DESC, month DESC, paymentDate DESC")
    fun getByTenancyFlow(tenancyId: Long): Flow<List<RentPayment>>

    @Query("""
        SELECT p.* FROM rent_payments p
        INNER JOIN tenancies t ON p.tenancyId = t.id
        WHERE t.roomId = :roomId
        ORDER BY p.year DESC, p.month DESC, p.paymentDate DESC
    """)
    fun getByRoomFlow(roomId: Long): Flow<List<RentPayment>>

    @Query("SELECT * FROM rent_payments WHERE year = :year AND month = :month ORDER BY paymentDate DESC")
    fun getByMonthFlow(month: Int, year: Int): Flow<List<RentPayment>>

    @Query("SELECT * FROM rent_payments WHERE year = :year ORDER BY month ASC, paymentDate ASC")
    fun getByYearFlow(year: Int): Flow<List<RentPayment>>

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM rent_payments")
    fun getTotalAllTimeFlow(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM rent_payments WHERE year = :year")
    fun getTotalForYearFlow(year: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM rent_payments WHERE year = :year AND month = :month")
    fun getTotalForMonthFlow(month: Int, year: Int): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(p.amountPaid), 0) FROM rent_payments p
        INNER JOIN tenancies t ON p.tenancyId = t.id
        INNER JOIN room_units r ON t.roomId = r.id
        WHERE p.year = :year AND r.complexId = :complexId
    """)
    fun getTotalForYearByComplexFlow(complexId: Long, year: Int): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(p.amountPaid), 0) FROM rent_payments p
        INNER JOIN tenancies t ON p.tenancyId = t.id
        INNER JOIN room_units r ON t.roomId = r.id
        WHERE r.complexId = :complexId
    """)
    fun getTotalAllTimeByComplexFlow(complexId: Long): Flow<Long>

    @Query("""
        SELECT p.tenancyId AS tenancyId, SUM(p.amountPaid) AS total FROM rent_payments p
        INNER JOIN tenancies t ON p.tenancyId = t.id
        INNER JOIN room_units r ON t.roomId = r.id
        WHERE p.month = :month AND p.year = :year AND r.complexId = :complexId
        GROUP BY p.tenancyId
    """)
    fun getPaidTotalsForComplex(complexId: Long, month: Int, year: Int): Flow<List<TenancyPaidTotal>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(payment: RentPayment): Long

    @Update
    suspend fun update(payment: RentPayment)

    @Delete
    suspend fun delete(payment: RentPayment)

    @Query("SELECT * FROM rent_payments")
    suspend fun getAll(): List<RentPayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RentPayment>)

    @Query("DELETE FROM rent_payments")
    suspend fun deleteAll()
}
