package com.santhomach.commercialledger.data.dao

import androidx.room.*
import com.santhomach.commercialledger.data.model.Tenant
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants WHERE id = :id")
    suspend fun getById(id: Long): Tenant?

    @Query("SELECT * FROM tenants ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Tenant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tenant: Tenant): Long

    @Update
    suspend fun update(tenant: Tenant)
}
