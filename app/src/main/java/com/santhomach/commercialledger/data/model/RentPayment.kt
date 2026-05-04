package com.santhomach.commercialledger.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rent_payments",
    foreignKeys = [
        ForeignKey(
            entity = Tenancy::class,
            parentColumns = ["id"],
            childColumns = ["tenancyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tenancyId"), Index("year", "month")]
)
data class RentPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tenancyId: Long,
    val paymentDate: String,
    val amountPaid: Long,
    val month: Int,
    val year: Int,
    val paymentMode: String = "Cash",
    val notes: String = "",
    val createdAt: String = java.time.LocalDateTime.now().toString()
)
