package com.santhomach.commercialledger.data.repository

import androidx.room.withTransaction
import com.google.gson.GsonBuilder
import com.santhomach.commercialledger.data.dao.*
import com.santhomach.commercialledger.data.db.AppDatabase
import com.santhomach.commercialledger.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class LedgerRepository(
    private val database: AppDatabase,
    private val complexDao: ComplexDao,
    private val roomUnitDao: RoomUnitDao,
    private val tenantDao: TenantDao,
    private val tenancyDao: TenancyDao,
    private val rentPaymentDao: RentPaymentDao,
    private val expenseDao: ExpenseDao,
) {
    // Complex
    fun getAllComplexesFlow(): Flow<List<Complex>> = complexDao.getAllFlow()
    suspend fun insertComplex(complex: Complex): Long = complexDao.insert(complex)
    suspend fun updateComplex(complex: Complex) = complexDao.update(complex)
    suspend fun deleteComplex(complex: Complex) = complexDao.delete(complex)
    suspend fun getComplexById(id: Long): Complex? = complexDao.getById(id)

    // RoomUnit
    fun getRoomsForComplexFlow(complexId: Long): Flow<List<RoomUnit>> =
        roomUnitDao.getByComplexFlow(complexId)
    suspend fun insertRoomUnit(room: RoomUnit): Long = roomUnitDao.insert(room)
    suspend fun updateRoomUnit(room: RoomUnit) = roomUnitDao.update(room)
    suspend fun deleteRoomUnit(room: RoomUnit) = roomUnitDao.delete(room)
    suspend fun getRoomUnitById(id: Long): RoomUnit? = roomUnitDao.getById(id)

    // Tenant
    suspend fun insertTenant(tenant: Tenant): Long = tenantDao.insert(tenant)
    suspend fun updateTenant(tenant: Tenant) = tenantDao.update(tenant)
    suspend fun getTenantById(id: Long): Tenant? = tenantDao.getById(id)
    fun getAllTenantsFlow(): Flow<List<Tenant>> = tenantDao.getAllFlow()

    // Tenancy — atomic insert of tenant + tenancy together
    suspend fun insertTenantAndTenancy(
        tenant: Tenant,
        tenancy: Tenancy
    ): Result<Long> = runCatching {
        database.withTransaction {
            val existingActive = tenancyDao.countActiveTenanciesForRoom(tenancy.roomId)
            if (existingActive > 0) error("This door already has an active tenant")
            val tenantId = tenantDao.insert(tenant)
            tenancyDao.insert(tenancy.copy(tenantId = tenantId))
        }
    }

    fun getTenancyHistoryForRoomFlow(roomId: Long): Flow<List<Tenancy>> =
        tenancyDao.getByRoomFlow(roomId)
    fun getActiveTenancyForRoomFlow(roomId: Long): Flow<Tenancy?> =
        tenancyDao.getActiveTenancyForRoomFlow(roomId)
    suspend fun updateTenancy(tenancy: Tenancy) = tenancyDao.update(
        tenancy.copy(updatedAt = LocalDateTime.now().toString())
    )
    fun getAllActiveTenanciesFlow(): Flow<List<Tenancy>> = tenancyDao.getAllActiveTenanciesFlow()
    fun getExpectedMonthlyRentForComplex(complexId: Long): Flow<Long> =
        tenancyDao.getExpectedMonthlyRentForComplex(complexId)
    fun getCollectedRentForComplex(complexId: Long, month: Int, year: Int): Flow<Long> =
        tenancyDao.getCollectedRentForComplex(complexId, month, year)
    suspend fun getTenancyById(id: Long): Tenancy? = tenancyDao.getById(id)

    fun getActiveDoorNumbersForTenant(tenantId: Long): Flow<List<String>> =
        tenancyDao.getActiveDoorNumbersForTenant(tenantId)

    suspend fun insertTenancyForExistingTenant(tenancy: Tenancy): Result<Long> = runCatching {
        database.withTransaction {
            val existingActive = tenancyDao.countActiveTenanciesForRoom(tenancy.roomId)
            if (existingActive > 0) error("This door already has an active tenant")
            tenancyDao.insert(tenancy)
        }
    }

    suspend fun closeTenancy(
        tenancyId: Long,
        endDate: String,
        closureNotes: String,
        refundAmount: Long
    ): Result<Unit> = runCatching {
        database.withTransaction {
            val tenancy = tenancyDao.getById(tenancyId)
                ?: error("Tenancy not found")
            if (tenancy.status == TenancyStatus.CLOSED)
                error("Tenancy is already closed")
            tenancyDao.update(
                tenancy.copy(
                    status = TenancyStatus.CLOSED,
                    endDate = endDate,
                    closureNotes = closureNotes,
                    refundAmount = refundAmount,
                    updatedAt = LocalDateTime.now().toString()
                )
            )
        }
    }

    // RentPayment
    fun getPaymentsForTenancyFlow(tenancyId: Long): Flow<List<RentPayment>> =
        rentPaymentDao.getByTenancyFlow(tenancyId)
    fun getTotalAllTimeFlow(): Flow<Long> = rentPaymentDao.getTotalAllTimeFlow()
    fun getTotalForYearFlow(year: Int): Flow<Long> = rentPaymentDao.getTotalForYearFlow(year)
    fun getTotalForMonthFlow(month: Int, year: Int): Flow<Long> =
        rentPaymentDao.getTotalForMonthFlow(month, year)
    fun getTotalForYearByComplexFlow(complexId: Long, year: Int): Flow<Long> =
        rentPaymentDao.getTotalForYearByComplexFlow(complexId, year)
    fun getTotalAllTimeByComplexFlow(complexId: Long): Flow<Long> =
        rentPaymentDao.getTotalAllTimeByComplexFlow(complexId)

    fun getPaidTenancyIdsForComplex(complexId: Long, month: Int, year: Int): Flow<Set<Long>> =
        rentPaymentDao.getPaidTenancyIdsForComplex(complexId, month, year).map { it.toSet() }

    suspend fun insertPayment(payment: RentPayment): Result<Long> = runCatching {
        val existing = rentPaymentDao.countForMonth(payment.tenancyId, payment.month, payment.year)
        if (existing > 0) error("Payment for ${payment.month}/${payment.year} already recorded")
        rentPaymentDao.insert(payment)
    }

    suspend fun deletePayment(payment: RentPayment) = rentPaymentDao.delete(payment)

    // Expense
    fun getExpensesForComplexFlow(complexId: Long): Flow<List<Expense>> =
        expenseDao.getByComplexFlow(complexId)
    fun getExpensesForRoomFlow(roomId: Long): Flow<List<Expense>> =
        expenseDao.getByRoomFlow(roomId)
    fun getExpenseTotalForComplexYear(complexId: Long, year: Int): Flow<Long> =
        expenseDao.getTotalForComplexYearFlow(complexId, year)
    fun getExpenseTotalForComplexMonth(complexId: Long, year: Int, month: Int): Flow<Long> =
        expenseDao.getTotalForComplexMonthFlow(complexId, year, month)
    fun getExpenseTotalForMonth(year: Int, month: Int): Flow<Long> =
        expenseDao.getTotalForMonthFlow(year, month)
    fun getExpenseTotalForYear(year: Int): Flow<Long> =
        expenseDao.getTotalForYearFlow(year)
    fun getExpenseTotalAllTime(): Flow<Long> = expenseDao.getTotalAllTimeFlow()
    suspend fun insertExpense(expense: Expense): Long = expenseDao.insert(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)
    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getById(id)

    // Backup / Restore
    private val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()

    suspend fun exportBackup(): String {
        val data = BackupData(
            exportedAt = LocalDateTime.now().toString(),
            complexes = complexDao.getAll(),
            roomUnits = roomUnitDao.getAll(),
            tenants = tenantDao.getAll(),
            tenancies = tenancyDao.getAll(),
            rentPayments = rentPaymentDao.getAll(),
            expenses = expenseDao.getAll(),
        )
        return gson.toJson(data)
    }

    suspend fun importFromBackup(json: String): Result<Unit> = runCatching {
        val data = gson.fromJson(json, BackupData::class.java)
            ?: error("Invalid backup file")
        require(data.version == 1) { "Unsupported backup version ${data.version}" }
        database.withTransaction {
            // Delete children before parents to satisfy FK constraints
            rentPaymentDao.deleteAll()
            expenseDao.deleteAll()
            tenancyDao.deleteAll()
            roomUnitDao.deleteAll()
            tenantDao.deleteAll()
            complexDao.deleteAll()
            // Insert parents before children; orEmpty() guards against fields
            // missing in a hand-edited or incomplete backup file
            complexDao.insertAll(data.complexes.orEmpty())
            roomUnitDao.insertAll(data.roomUnits.orEmpty())
            tenantDao.insertAll(data.tenants.orEmpty())
            tenancyDao.insertAll(data.tenancies.orEmpty())
            rentPaymentDao.insertAll(data.rentPayments.orEmpty())
            expenseDao.insertAll(data.expenses.orEmpty())
        }
    }
}
