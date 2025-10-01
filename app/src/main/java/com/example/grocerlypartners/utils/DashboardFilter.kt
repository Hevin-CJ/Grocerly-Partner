package com.example.grocerlypartners.utils

import kotlinx.serialization.Serializable

@Serializable
sealed class DashboardFilter {
    @Serializable
    data object Today : DashboardFilter()
    @Serializable
    data object Yesterday : DashboardFilter()
    @Serializable
    data object Week: DashboardFilter()
    @Serializable
    data object Month: DashboardFilter()
    @Serializable
    data class Custom(val startTime: Long, val endTime: Long) : DashboardFilter()
}