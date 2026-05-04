package com.commercialledger.data.dao

import androidx.room.*
import com.commercialledger.data.model.Tenancy
import kotlinx.coroutines.flow.Flow

@Dao
interface TenancyDao {
    @Query("SELECT * FROM tenancies WHERE roomId = :roomId ORDER BY startDate DESC")
    fun getByRoomFlow(roomId: Long): Flow<List<Tenancy>>

    @Query("SELECT * FROM tenancies WHERE roomId = :roomId AND status = 'ACTIVE' LIMIT 1")
    fun getActiveTenancyForRoomFlow(roomId: Long): Flow<Tenancy?>

    @Query("SELECT * FROM tenancies WHERE id = :id")
    suspend fun getById(id: Long): Tenancy?

    @Query("SELECT * FROM tenancies WHERE status = 'ACTIVE'")
    fun getAllActiveTenanciesFlow(): Flow<List<Tenancy>>

    @Query("""
        SELECT t.* FROM tenancies t
        INNER JOIN room_units r ON t.roomId = r.id
        WHERE r.complexId = :complexId
        ORDER BY t.startDate DESC
    """)
    fun getByComplexFlow(complexId: Long): Flow<List<Tenancy>>

    @Query("""
        SELECT COALESCE(SUM(t.monthlyRent), 0) FROM tenancies t
        INNER JOIN room_units r ON t.roomId = r.id
        WHERE r.complexId = :complexId AND t.status = 'ACTIVE'
    """)
    fun getExpectedMonthlyRentForComplex(complexId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(p.amountPaid), 0) FROM rent_payments p
        INNER JOIN tenancies t ON p.tenancyId = t.id
        INNER JOIN room_units r ON t.roomId = r.id
        WHERE p.month = :month AND p.year = :year AND r.complexId = :complexId
    """)
    fun getCollectedRentForComplex(complexId: Long, month: Int, year: Int): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(p.amountPaid), 0) FROM rent_payments p
        WHERE p.month = :month AND p.year = :year
    """)
    fun getCollectedRentForMonth(month: Int, year: Int): Flow<Long>

    @Query("SELECT * FROM tenancies WHERE roomId = :roomId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveTenancyForRoom(roomId: Long): Tenancy?

    @Query("SELECT COUNT(*) FROM tenancies WHERE roomId = :roomId AND status = 'ACTIVE'")
    suspend fun countActiveTenanciesForRoom(roomId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tenancy: Tenancy): Long

    @Update
    suspend fun update(tenancy: Tenancy)
}
