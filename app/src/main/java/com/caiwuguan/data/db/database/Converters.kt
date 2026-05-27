package com.caiwuguan.data.db.database

import androidx.room.TypeConverter
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    companion object {
        private val gson = Gson()
    }

    @TypeConverter
    fun fromCategory(value: Category): String = value.name

    @TypeConverter
    fun toCategory(value: String): Category = Category.valueOf(value)

    @TypeConverter
    fun fromBillType(value: BillType): String = value.name

    @TypeConverter
    fun toBillType(value: String): BillType = BillType.valueOf(value)

    @TypeConverter
    fun fromPaymentSource(value: PaymentSource): String = value.name

    @TypeConverter
    fun toPaymentSource(value: String): PaymentSource = PaymentSource.valueOf(value)

    @TypeConverter
    fun fromCategoryMap(map: Map<Category, Long>): String = gson.toJson(map)

    @TypeConverter
    fun toCategoryMap(json: String): Map<Category, Long> =
        gson.fromJson(json, object : TypeToken<Map<Category, Long>>() {}.type)
}
