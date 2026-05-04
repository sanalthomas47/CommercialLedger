package com.santhomach.commercialledger.data.db

import androidx.room.TypeConverter
import com.santhomach.commercialledger.data.model.ExpenseType
import com.santhomach.commercialledger.data.model.TenancyStatus

class Converters {
    @TypeConverter
    fun fromTenancyStatus(value: TenancyStatus): String = value.name

    @TypeConverter
    fun toTenancyStatus(value: String): TenancyStatus = TenancyStatus.valueOf(value)

    @TypeConverter
    fun fromExpenseType(value: ExpenseType): String = value.name

    @TypeConverter
    fun toExpenseType(value: String): ExpenseType = ExpenseType.valueOf(value)
}
