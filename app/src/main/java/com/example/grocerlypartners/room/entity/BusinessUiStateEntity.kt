package com.example.grocerlypartners.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.grocerlypartners.model.SalesComparisonResult
import com.example.grocerlypartners.utils.Constants.BUSINESS_TABLE
import com.example.grocerlypartners.utils.DashboardFilter


@Entity(tableName = BUSINESS_TABLE)
data class BusinessUiStateEntity (
    @PrimaryKey
    val filterId: String = "",
    val filterType: DashboardFilter,
    val totalActiveOrderAmount: Long,
    val totalOrderSize: Int,
    val cancelledOrderAmount: Long,
    val cancelledOrderSize: Int,
    val salesComparisonPercentage: SalesComparisonResult,
    val salesComparisonSizePercentage: SalesComparisonResult,
    val cancelledComparisonPercentage: SalesComparisonResult,
    val cancelledSizeComparisonPercentage: SalesComparisonResult,
    val lastUpdated: Long = System.currentTimeMillis()

)
