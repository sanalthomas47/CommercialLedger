package com.santhomach.commercialledger.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "room_units",
    foreignKeys = [
        ForeignKey(
            entity = Complex::class,
            parentColumns = ["id"],
            childColumns = ["complexId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("complexId")]
)
data class RoomUnit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val complexId: Long,
    val doorNumber: String,
    val description: String = "",
    val floor: String = "",
    val createdAt: String = java.time.LocalDateTime.now().toString()
)
