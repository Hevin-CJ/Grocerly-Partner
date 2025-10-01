package com.example.grocerlypartners.room.convertor

import androidx.room.TypeConverter
import com.example.grocerlypartners.model.SalesComparisonResult
import com.example.grocerlypartners.utils.DashboardFilter
import kotlinx.serialization.json.Json

class BusinessClassTypeConvertor {

    @TypeConverter
    fun fromSalesComparisonResult(result: SalesComparisonResult?): String? {
        return result?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toSalesComparisonResult(json: String?): SalesComparisonResult? {
        return json?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromDashboardFilter(filter: DashboardFilter?): String? {
        return filter?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toDashboardFilter(json: String?): DashboardFilter? {
        return json?.let { Json.decodeFromString(it) }
    }

}