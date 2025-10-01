package com.example.grocerlypartners.repository.remote

import android.util.Log
import com.example.grocerlypartners.ConnectivityObserver
import com.example.grocerlypartners.model.BusinessUiState
import com.example.grocerlypartners.model.DateRangePair
import com.example.grocerlypartners.model.SalesComparisonResult
import com.example.grocerlypartners.model.SalesStatus
import com.example.grocerlypartners.repository.local.BusinessLocalRepoImpl
import com.example.grocerlypartners.room.entity.BusinessUiStateEntity
import com.example.grocerlypartners.utils.Constants.ORDERS
import com.example.grocerlypartners.utils.Constants.PARTNERS
import com.example.grocerlypartners.utils.DashboardFilter
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@ActivityRetainedScoped
class BusinessRepoImpl @Inject constructor(private val auth: FirebaseAuth,private val db: FirebaseFirestore,private val ordersRepoImpl: OrdersRepoImpl,private val businessLocalRepoImpl: BusinessLocalRepoImpl,private val connectivityObserver: ConnectivityObserver) {

    private val userId = auth.currentUser?.uid.toString()
    private val orderRef = db.collection(PARTNERS).document(userId).collection( ORDERS)

    suspend fun fetchCancelledOrdersSize(
        startTime: Long = 0L,
        endTime: Long = 0L
    ): NetworkResult<Int> {
        try {
            val zoneId = ZoneId.of("Asia/Kolkata")
            val todayStartTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()

            val effectiveStartTime = if (startTime == 0L) todayStartTime else startTime
            val effectiveEndTime = if (endTime == 0L) System.currentTimeMillis() else endTime


            val networkResult = ordersRepoImpl.fetchCancelledItems().filter { it !is NetworkResult.Loading }.first()
            Log.d("cancelledorderssize",networkResult.data?.values.toString())

            return when (networkResult) {
                is NetworkResult.Success -> {
                    if (networkResult.data.isNullOrEmpty()) {
                        return NetworkResult.Success(0)
                    }

                    val size = networkResult.data.values
                        .flatten()
                        .count { (it.cancellationInfo.cancelledAt) in effectiveStartTime..effectiveEndTime }

                    NetworkResult.Success(size)
                }

                is NetworkResult.Error -> NetworkResult.Error(
                    networkResult.message ?: "Failed to fetch cancelled orders"
                )

                is NetworkResult.Loading -> NetworkResult.Loading()
                is NetworkResult.UnSpecified -> NetworkResult.UnSpecified()
            }


        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Failed to fetch cancelled orders")
        }
    }



    suspend fun fetchTotalOrderLoss(startTime: Long = 0L,endTime: Long = 0L): NetworkResult<Long> {
        try {


            val zoneId = ZoneId.of("Asia/Kolkata")
            val todayStartTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()


            val effectiveStartTime = if (startTime == 0L) todayStartTime else startTime
            val effectiveEndTime = if (endTime == 0L) System.currentTimeMillis() else endTime

            val networkResult = ordersRepoImpl.fetchCancelledItems().filter { it !is NetworkResult.Loading }.first()
            Log.d("totalorderloss",networkResult.data.toString())



            return when (networkResult) {
                is NetworkResult.Error -> NetworkResult.Error(
                    networkResult.message ?: "Unable to get total order loss"
                )

                is NetworkResult.Loading -> NetworkResult.Loading()
                is NetworkResult.UnSpecified -> NetworkResult.UnSpecified()
                is NetworkResult.Success -> {

                    if (networkResult.data.isNullOrEmpty()) {
                        return NetworkResult.Success(0L)
                    }

                    val totalAmount: Long = networkResult.data.values.flatten()
                        .filter { (it.cancellationInfo.cancelledAt) in effectiveStartTime..effectiveEndTime }
                        .sumOf { cartProduct ->
                            (cartProduct.product.itemPrice ?: 0L).toLong() * cartProduct.quantity
                        }

                    NetworkResult.Success(totalAmount)
                }
            }

        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Unable to get total order loss")
        }
    }


    suspend fun fetchTotalOrdersSize(startTime: Long = 0L,endTime: Long = 0L):NetworkResult<Int>{
        try {

            val zoneId = ZoneId.of("Asia/Kolkata")
            val todayStartTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()

            val effectiveStartTime = if (startTime == 0L) todayStartTime else startTime
            val effectiveEndTime = if (endTime == 0L) System.currentTimeMillis() else endTime

            val networkResult =  ordersRepoImpl.fetchAllActiveOrders().filter { it !is NetworkResult.Loading }.first()

            return when (networkResult) {
                is NetworkResult.Success -> {

                    if (networkResult.data.isNullOrEmpty()) {
                        return NetworkResult.Success(0)
                    }

                    val size = networkResult.data.count { it.timestamp in effectiveStartTime .. effectiveEndTime }
                    NetworkResult.Success(size)

                }
                is NetworkResult.Error -> NetworkResult.Error(networkResult.message ?: "Failed to fetch cancelled orders")
                is NetworkResult.Loading -> NetworkResult.Loading()
                is NetworkResult.UnSpecified -> NetworkResult.UnSpecified()
            }

        }catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Failed to fetch cancelled orders")
        }
    }

    suspend fun fetchTotalAmountActiveOrders(startTime: Long = 0L,endTime: Long = 0L): NetworkResult<Long> {
       return try {
           val zoneId = ZoneId.of("Asia/Kolkata")
            val todayStartTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()

            val effectiveStartTime = if (startTime == 0L) todayStartTime else startTime
            val effectiveEndTime = if (endTime == 0L) System.currentTimeMillis() else endTime


            when (val networkResult = ordersRepoImpl.fetchAllActiveOrders().filter { it !is NetworkResult.Loading }.first()) {
                is NetworkResult.Success -> {
                    val orders = networkResult.data.orEmpty()
                    if (orders.isEmpty()) {
                        NetworkResult.Success(0L)
                    } else {
                        val totalAmount = orders
                            .filter { it.timestamp in effectiveStartTime..effectiveEndTime }
                            .flatMap { it.items }
                            .sumOf { (it.product.itemPrice ?: 0L).toLong() * it.quantity }
                        NetworkResult.Success(totalAmount)

                    }
                }
                is NetworkResult.Error -> NetworkResult.Error(
                    networkResult.message ?: "Unable to get total order loss"
                )
                is NetworkResult.Loading -> NetworkResult.Loading()
                is NetworkResult.UnSpecified -> NetworkResult.UnSpecified()
            }

        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Unable to get total Amount")
        }
    }


    suspend fun compareSalesAmount(currentStartTime: Long = 0L, currentEndTime: Long = 0L, previousStartTime: Long = 0L, previousEndTime: Long = 0L,filterType: DashboardFilter): NetworkResult<SalesComparisonResult>{
        return try {

            val zoneId = ZoneId.of("Asia/Kolkata")
            val todayStartTime = LocalDate.now(zoneId)
            val yesterday = LocalDate.now(zoneId).minusDays(1)

            val effectiveCurrentStart = if (currentStartTime == 0L) {
                todayStartTime.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else currentStartTime

            val effectiveCurrentEnd = if (currentEndTime == 0L) {
                System.currentTimeMillis()
            } else currentEndTime


            val effectivePreviousStart = if (previousStartTime == 0L) {
                yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else previousStartTime

            val effectivePreviousEnd = if (previousEndTime == 0L) {
                todayStartTime.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1 // End of yesterday
            } else previousEndTime

            val (currentResult, previousResult) = coroutineScope {
                val currentDeferred = async { fetchTotalAmountActiveOrders(effectiveCurrentStart, effectiveCurrentEnd) }
                val previousDeferred = async { fetchTotalAmountActiveOrders(effectivePreviousStart, effectivePreviousEnd) }
                currentDeferred.await() to previousDeferred.await()
            }

            if (currentResult is NetworkResult.Error) return NetworkResult.Error("Unable to get current Sales data")
            if (previousResult is NetworkResult.Error) return NetworkResult.Error("Unable to get previous Sales data")

            val currentSales = (currentResult as NetworkResult.Success).data ?: 0
            val previousSales = (previousResult as NetworkResult.Success).data ?: 0

            val (percentage, status) = when {
                previousSales == 0L -> {
                    if (currentSales > 0) {
                        100.0 to SalesStatus.INCREASE
                    } else {
                        0.0 to SalesStatus.NO_PRIOR_DATA
                    }
                }

                else -> {
                    val change = ((currentSales.toDouble() - previousSales) / previousSales) * 100
                    val newStatus = when{
                        change > 0 -> SalesStatus.INCREASE
                        change < 0  -> SalesStatus.DECREASE
                        else -> SalesStatus.NO_CHANGE
                    }
                    change to newStatus
                }
            }

            Log.d("SalesDebug102", "Comparing Current: $currentSales vs Previous: $previousSales")

            NetworkResult.Success(
                SalesComparisonResult(
                    percentageChange = percentage,
                    status = status,
                    filterType
                )
            )

        }catch (e: Exception){
           return NetworkResult.Error(e.message ?: "Failed to get Sales data")
        }
    }

    suspend fun compareOrderSize(currentStartTime: Long = 0L, currentEndTime: Long = 0L, previousStartTime: Long = 0L, previousEndTime: Long = 0L,filterType: DashboardFilter): NetworkResult<SalesComparisonResult>{
        return try {

            val zoneId = ZoneId.of("Asia/Kolkata")
            val todayStartTime = LocalDate.now(zoneId)
            val yesterday = LocalDate.now(zoneId).minusDays(1)

            val effectiveCurrentStart = if (currentStartTime == 0L) {
                todayStartTime.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else currentStartTime

            val effectiveCurrentEnd = if (currentEndTime == 0L) {
                System.currentTimeMillis()
            } else currentEndTime


            val effectivePreviousStart = if (previousStartTime == 0L) {
                yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else previousStartTime

            val effectivePreviousEnd = if (previousEndTime == 0L) {
                todayStartTime.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
            } else previousEndTime

            val (currentResult, previousResult) = coroutineScope {
                val currentDeferred = async { fetchTotalOrdersSize(effectiveCurrentStart, effectiveCurrentEnd) }
                val previousDeferred = async { fetchTotalOrdersSize(effectivePreviousStart, effectivePreviousEnd) }
                currentDeferred.await() to previousDeferred.await()
            }

            if (currentResult is NetworkResult.Error) return NetworkResult.Error("Unable to get current Sales data")
            if (previousResult is NetworkResult.Error) return NetworkResult.Error("Unable to get previous Sales data")

            val currentSales = (currentResult as NetworkResult.Success).data ?: 0
            val previousSales = (previousResult as NetworkResult.Success).data ?: 0
            Log.d("SalesDebug101", "Comparing Current: $currentSales vs Previous: $previousSales")


            val (percentage, status) = when {
                previousSales == 0 -> {
                    if (currentSales > 0) {
                        100.0 to SalesStatus.INCREASE
                    } else {
                        0.0 to SalesStatus.NO_PRIOR_DATA
                    }
                }

                else -> {
                    val change = ((currentSales.toDouble() - previousSales) / previousSales) * 100
                    val newStatus = when{
                        change > 0 -> SalesStatus.INCREASE
                        change < 0  -> SalesStatus.DECREASE
                        else -> SalesStatus.NO_CHANGE
                    }
                    change to newStatus
                }
            }

            Log.d("salesration","percentage ratio $percentage to status $status")

            NetworkResult.Success(
                SalesComparisonResult(
                    percentageChange = percentage,
                    status = status,
                    filterType
                )
            )

        }catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Failed to get Sales data")
        }
    }


    suspend fun compareCancelledOrderSize(currentStartTime: Long = 0L, currentEndTime: Long = 0L, previousStartTime: Long = 0L, previousEndTime: Long = 0L,filterType: DashboardFilter): NetworkResult<SalesComparisonResult>{
        return try {

            val zoneId = ZoneId.of("Asia/Kolkata")
            val todayStartTime = LocalDate.now(zoneId)
            val yesterday = LocalDate.now(zoneId).minusDays(1)

            val effectiveCurrentStart = if (currentStartTime == 0L) {
                todayStartTime.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else currentStartTime

            val effectiveCurrentEnd = if (currentEndTime == 0L) {
                System.currentTimeMillis()
            } else currentEndTime


            val effectivePreviousStart = if (previousStartTime == 0L) {
                yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else previousStartTime

            val effectivePreviousEnd = if (previousEndTime == 0L) {
                todayStartTime.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1 // End of yesterday
            } else previousEndTime

            val (currentResult, previousResult) = coroutineScope {
                val currentDeferred = async { fetchCancelledOrdersSize(effectiveCurrentStart, effectiveCurrentEnd) }
                val previousDeferred = async { fetchCancelledOrdersSize(effectivePreviousStart, effectivePreviousEnd) }
                currentDeferred.await() to previousDeferred.await()
            }

            if (currentResult is NetworkResult.Error) return NetworkResult.Error("Unable to get current Sales data")
            if (previousResult is NetworkResult.Error) return NetworkResult.Error("Unable to get previous Sales data")

            val currentSales = (currentResult as NetworkResult.Success).data ?: 0
            val previousSales = (previousResult as NetworkResult.Success).data ?: 0

            val (percentage, status) = when {
                previousSales == 0 -> {
                    if (currentSales > 0) {
                        100.0 to SalesStatus.INCREASE
                    } else {
                        0.0 to SalesStatus.NO_PRIOR_DATA
                    }
                }

                else -> {
                    val change = ((currentSales.toDouble() - previousSales) / previousSales) * 100
                    val newStatus = when{
                        change > 0 -> SalesStatus.INCREASE
                        change < 0  -> SalesStatus.DECREASE
                        else -> SalesStatus.NO_CHANGE
                    }
                    change to newStatus
                }
            }

            NetworkResult.Success(
                SalesComparisonResult(
                    percentageChange = percentage,
                    status = status,
                    filterType
                )
            )

        }catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Failed to get Sales data")
        }
    }

    suspend fun compareCancelledOrders(currentStartTime: Long = 0L, currentEndTime: Long = 0L, previousStartTime: Long = 0L, previousEndTime: Long = 0L,filterType: DashboardFilter): NetworkResult<SalesComparisonResult>{
        return try {

            val zoneId = ZoneId.of("Asia/Kolkata")
            val todayStartTime = LocalDate.now(zoneId)
            val yesterday = LocalDate.now(zoneId).minusDays(1)

            val effectiveCurrentStart = if (currentStartTime == 0L) {
                todayStartTime.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else currentStartTime

            val effectiveCurrentEnd = if (currentEndTime == 0L) {
                System.currentTimeMillis()
            } else currentEndTime


            val effectivePreviousStart = if (previousStartTime == 0L) {
                yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else previousStartTime

            val effectivePreviousEnd = if (previousEndTime == 0L) {
                todayStartTime.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1 // End of yesterday
            } else previousEndTime

            val (currentResult, previousResult) = coroutineScope {
                val currentDeferred = async { fetchTotalOrderLoss(effectiveCurrentStart, effectiveCurrentEnd) }
                val previousDeferred = async { fetchTotalOrderLoss(effectivePreviousStart, effectivePreviousEnd) }
                currentDeferred.await() to previousDeferred.await()
            }

            if (currentResult is NetworkResult.Error) return NetworkResult.Error("Unable to get current Sales data")
            if (previousResult is NetworkResult.Error) return NetworkResult.Error("Unable to get previous Sales data")

            val currentSales = (currentResult as NetworkResult.Success).data ?: 0
            val previousSales = (previousResult as NetworkResult.Success).data ?: 0

            val (percentage, status) = when {
                previousSales == 0L -> {
                    if (currentSales > 0) {
                        100.0 to SalesStatus.INCREASE
                    } else {
                        0.0 to SalesStatus.NO_PRIOR_DATA
                    }
                }

                else -> {
                    val change = ((currentSales.toDouble() - previousSales) / previousSales) * 100
                    val newStatus = when{
                        change > 0 -> SalesStatus.INCREASE
                        change < 0  -> SalesStatus.DECREASE
                        else -> SalesStatus.NO_CHANGE
                    }
                    change to newStatus
                }
            }
            Log.d("SalesDebug", "Comparing Current: $currentSales vs Previous: $previousSales")
            Log.d("cancelledorderloss",percentage.toString())

            NetworkResult.Success(
                SalesComparisonResult(
                    percentageChange = percentage,
                    status = status,
                    filterType
                )
            )


        }catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Failed to get Sales data")
        }
    }


          fun loadDataForFilter(filter: DashboardFilter): Flow<BusinessUiState> = flow {

            val filterId = getFilterId(filter)

            val cachedData = businessLocalRepoImpl.getBusinessDataById(filterId)

            emitAllStates(cachedData)

            if (connectivityObserver.observe().first()) {
                val ranges = calculateDateRanges(filter)
                val (currentStart, currentEnd, prevStart, prevEnd, filter) = ranges

                coroutineScope {

                      val totalOrderSize =   async { fetchTotalOrdersSize(currentStart, currentEnd) }
                       val totalAmount =  async { fetchTotalAmountActiveOrders(currentStart, currentEnd) }
                     val  cancelledAmount=  async { fetchTotalOrderLoss(currentStart, currentEnd) }
                    val  cancelledOrderSize=    async { fetchCancelledOrdersSize(currentStart, currentEnd) }

                    val compareTotalOrderAmount =   async {
                        compareSalesAmount(
                            currentStart, currentEnd, prevStart, prevEnd, filter
                        )
                    }
                    val compareTotalOrderSize =   async {
                        compareOrderSize(
                            currentStart, currentEnd, prevStart, prevEnd, filter
                        )
                    }
                    val  compareCancelledAmount=  async {
                        compareCancelledOrders(
                            currentStart, currentEnd, prevStart, prevEnd, filter
                        )
                    }
                    val  compareCancelledOrderSize=   async {
                        compareCancelledOrderSize(
                            currentStart, currentEnd, prevStart, prevEnd, filter
                        )
                    }

                    val actualTotalAmount = totalAmount.await().data ?: 0
                    val actualTotalOrderSize = totalOrderSize.await().data ?: 0
                    val actualCancelledAmount = cancelledAmount.await().data?: 0
                    val actualCancelledOrderSize = cancelledOrderSize.await().data?: 0
                    val actualCompareTotalOrderAmount = compareTotalOrderAmount.await().data?: SalesComparisonResult()
                    val actualCompareTotalOrderSize = compareTotalOrderSize.await().data?: SalesComparisonResult()
                    val actualCompareCancelledAmount = compareCancelledAmount.await().data?: SalesComparisonResult()
                    val actualCompareCancelledOrderSize = compareCancelledOrderSize.await().data?: SalesComparisonResult()

                    val entity = BusinessUiStateEntity(
                        filterId = filterId,
                        filterType = filter,
                        actualTotalAmount,
                        actualTotalOrderSize,
                        actualCancelledAmount,
                        actualCancelledOrderSize,
                        actualCompareTotalOrderAmount,
                        actualCompareTotalOrderSize,
                        actualCompareCancelledAmount,
                        actualCompareCancelledOrderSize

                    )

                    businessLocalRepoImpl.upsertBusinessEntity(entity)
                    emitAllStates(entity)

                }
            } else {

                if (cachedData==null && connectivityObserver.observe().first()==false){

                    val lastSavedData = businessLocalRepoImpl.getAllBusinessData()

                  emitAllStates(lastSavedData)

                }


            }





        }

    private suspend fun FlowCollector<BusinessUiState>.emitAllStates(data: BusinessUiStateEntity?) {
        emit(BusinessUiState.TotalOrderSize(NetworkResult.Success(data?.totalOrderSize ?: 0)))
        emit(BusinessUiState.TotalActiveOrderAmount(NetworkResult.Success(data?.totalActiveOrderAmount ?: 0L)))
        emit(BusinessUiState.CancelledOrderAmount(NetworkResult.Success(data?.cancelledOrderAmount ?: 0L)))
        emit(BusinessUiState.CancelledOrderSize(NetworkResult.Success(data?.cancelledOrderSize ?: 0)))
        emit(BusinessUiState.SalesComparison(NetworkResult.Success(data?.salesComparisonPercentage ?: SalesComparisonResult())))
        emit(BusinessUiState.SalesComparisonSize(NetworkResult.Success(data?.salesComparisonSizePercentage ?: SalesComparisonResult())))
        emit(BusinessUiState.SalesComparisonCancelled(NetworkResult.Success(data?.cancelledComparisonPercentage ?: SalesComparisonResult())))
        emit(BusinessUiState.SalesComparisonCancelledSize(NetworkResult.Success(data?.cancelledSizeComparisonPercentage ?: SalesComparisonResult())))
    }

    private fun calculateDateRanges(filter: DashboardFilter): DateRangePair {
        val zoneId = ZoneId.of("Asia/Kolkata")
        val today = LocalDate.now(zoneId)
        return when (filter) {
            is DashboardFilter.Today -> {
                DateRangePair(0, 0, 0, 0, DashboardFilter.Today)
            }

            is DashboardFilter.Yesterday -> {


                val yesterday = today.minusDays(1)
                val dayBefore = yesterday.minusDays(1)

                val yesterdayStart = yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val yesterdayEnd = today.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

                val dayBeforeStart = dayBefore.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val dayBeforeEnd = yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1


                DateRangePair(yesterdayStart, yesterdayEnd, dayBeforeStart, dayBeforeEnd,DashboardFilter.Yesterday)
            }

            is DashboardFilter.Custom -> {

                val currentStartTime = filter.startTime
                val currentEndTime = filter.endTime

                val duration = currentEndTime - currentStartTime
                val previousEndTime = currentStartTime - 1
                val previousStartTime = previousEndTime - duration


                DateRangePair(filter.startTime, filter.endTime, previousStartTime, previousEndTime, DashboardFilter.Custom(filter.startTime,filter.endTime))

            }

            DashboardFilter.Month -> {

                val monthStartDate = today.withDayOfMonth(1)
                val monthStartTime = monthStartDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val monthEndTime = System.currentTimeMillis()

                val previousMonthStartDate = monthStartDate.minusMonths(1)
                val previousMonthStartTime = previousMonthStartDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val previousMonthEndTime = monthStartTime-1


                DateRangePair(monthStartTime, monthEndTime, previousMonthStartTime, previousMonthEndTime,DashboardFilter.Month)
            }
            DashboardFilter.Week -> {

                val weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weekStart = weekStartDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val weekEnd = System.currentTimeMillis()

                val prevWeekStartDate = weekStartDate.minusWeeks(1)
                val prevWeekStart = prevWeekStartDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val prevWeekEnd = weekStart - 1


                DateRangePair(weekStart, weekEnd, prevWeekStart, prevWeekEnd,DashboardFilter.Week)

            }
        }
    }


    private fun getFilterId(filter: DashboardFilter): String {
        return when (filter) {
            DashboardFilter.Today -> "TODAY"
            DashboardFilter.Yesterday -> "YESTERDAY"
            DashboardFilter.Week -> "WEEK"
            DashboardFilter.Month -> "MONTH"
            is DashboardFilter.Custom -> "CUSTOM_${filter.startTime}_${filter.endTime}"
        }
    }

}