package com.santhomach.commercialledger.data.db

import androidx.room.TypeConverter
import com.santhomach.commercialledger.data.model.ExpenseType
import com.santhomach.commercialledger.data.model.TenancyStatus

class Converters {
    @TypeConverter
    fun fromTenancyStatus(value: TenancyStatus): String = value.name

    @TypeConverter
    fun toTenancyStatus(value: String): TenancyStatus =
        runCatching { TenancyStatus.valueOf(value) }.getOrDefault(TenancyStatus.ACTIVE)

    @TypeConverter
    fun fromExpenseType(value: ExpenseType): String = value.name

    @TypeConverter
    fun toExpenseType(value: String): ExpenseType =
        runCatching { ExpenseType.valueOf(value) }.getOrDefault(ExpenseType.COMPLEX)
}
