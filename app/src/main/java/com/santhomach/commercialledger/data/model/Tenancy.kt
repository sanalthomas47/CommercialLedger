package com.santhomach.commercialledger.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TenancyStatus { ACTIVE, CLOSED }

@Entity(
    tableName = "tenancies",
    foreignKeys = [
        ForeignKey(
            entity = RoomUnit::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("roomId"), Index("tenantId"), Index("status")]
)
data class Tenancy(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val tenantId: Long,
    val startDate: String,
    val endDate: String? = null,
    val monthlyRent: Long,
    val securityDeposit: Long = 0,
    val taxAmount: Long = 0,
    val agreementDocPath: String? = null,
    val status: TenancyStatus = TenancyStatus.ACTIVE,
    val closureNotes: String? = null,
    val refundAmount: Long? = null,
    val createdAt: String = java.time.LocalDateTime.now().toString(),
    val updatedAt: String = java.time.LocalDateTime.now().toString()
)
