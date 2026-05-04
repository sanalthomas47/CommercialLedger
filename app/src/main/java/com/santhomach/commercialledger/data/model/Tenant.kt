package com.santhomach.commercialledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val idProofType: String = "",
    val idProofNumber: String = "",
    val address: String = "",
    val createdAt: String = java.time.LocalDateTime.now().toString()
)
