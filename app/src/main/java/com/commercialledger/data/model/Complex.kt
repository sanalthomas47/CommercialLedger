package com.commercialledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complexes")
data class Complex(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String,
    val description: String = "",
    val createdAt: String = java.time.LocalDateTime.now().toString()
)
