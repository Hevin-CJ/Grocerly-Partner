package com.example.grocerlypartners.model

import com.example.grocerlypartners.utils.DashboardFilter

data class DateRangePair(
    val currentStart: Long=0L,
    val currentEnd: Long=0L,
    val previousStart: Long=0L,
    val previousEnd: Long=0L,
    val filterType: DashboardFilter
)