package com.example.grocerlypartners.model

import com.example.grocerlypartners.utils.DashboardFilter
import kotlinx.serialization.Serializable

@Serializable
data class SalesComparisonResult(
    val percentageChange: Double = 0.0,
    val status: SalesStatus = SalesStatus.NO_PRIOR_DATA,
    val filter: DashboardFilter = DashboardFilter.Today
)

@Serializable
enum class SalesStatus {
    INCREASE,
    DECREASE,
    NO_CHANGE,
    NO_PRIOR_DATA
}