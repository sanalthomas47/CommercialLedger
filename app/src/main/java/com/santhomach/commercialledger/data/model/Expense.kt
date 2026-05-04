package com.santhomach.commercialledger.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ExpenseType { COMPLEX, DOOR }

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Complex::class,
            parentColumns = ["id"],
            childColumns = ["complexId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("complexId"), Index("roomId"), Index("year", "month")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val complexId: Long,
    val roomId: Long? = null,
    val amount: Long,
    val date: String,
    val month: Int,
    val year: Int,
    val category: String = "",
    val description: String = "",
    val type: ExpenseType = ExpenseType.COMPLEX,
    val createdAt: String = java.time.LocalDateTime.now().toString()
)
