package com.example.grocerlypartners.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerlypartners.ConnectivityObserver
import com.example.grocerlypartners.R
import com.example.grocerlypartners.model.BusinessUiState
import com.example.grocerlypartners.model.DateRangePair
import com.example.grocerlypartners.model.SalesComparisonResult
import com.example.grocerlypartners.model.SalesStatus
import com.example.grocerlypartners.preferences.GrocerlyDataStore
import com.example.grocerlypartners.repository.local.BusinessLocalRepoImpl
import com.example.grocerlypartners.repository.remote.BusinessRepoImpl
import com.example.grocerlypartners.room.entity.BusinessUiStateEntity
import com.example.grocerlypartners.utils.DashboardFilter
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject


@HiltViewModel
class BusinessViewModel @Inject constructor(application: Application,private val businessRepoImpl: BusinessRepoImpl,private val grocerlyDataStore: GrocerlyDataStore,private val businessLocalRepoImpl: BusinessLocalRepoImpl,private val connectivityObserver: ConnectivityObserver)  : AndroidViewModel(application) {

    val filterState: StateFlow<Pair<DashboardFilter, Int>> = grocerlyDataStore.getBusinessFilterState().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000),Pair(DashboardFilter.Today, R.id.todaybtn))

    val networkState: StateFlow<Boolean> = connectivityObserver.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),true)

    private val _businessUiState = MutableStateFlow<BusinessUiState?>(null)
    val businessUiState get() = _businessUiState.asStateFlow()


    private val totalActiveOrderSize = MutableStateFlow<NetworkResult<Int>>(NetworkResult.UnSpecified())
    private val totalActiveOrderAmount = MutableStateFlow<NetworkResult<Long>>(NetworkResult.UnSpecified())
    private val cancelledOrderSize = MutableStateFlow<NetworkResult<Int>>(NetworkResult.UnSpecified())
    private val cancelledOrderAmount = MutableStateFlow<NetworkResult<Long>>(NetworkResult.UnSpecified())

    private val totalSalesComparison = MutableStateFlow<NetworkResult<SalesComparisonResult>>(NetworkResult.UnSpecified())
    private val totalSalesComparisonSize = MutableStateFlow<NetworkResult<SalesComparisonResult>>(NetworkResult.UnSpecified())
    private val totalCancelledSales = MutableStateFlow<NetworkResult<SalesComparisonResult>>(NetworkResult.UnSpecified())
    private val totalCancelledSalesSize = MutableStateFlow<NetworkResult<SalesComparisonResult>>(NetworkResult.UnSpecified())



    init {
        merge(totalActiveOrderSize.map { BusinessUiState.TotalOrderSize(it) },totalActiveOrderAmount.map { BusinessUiState.TotalActiveOrderAmount(it) },cancelledOrderSize.map { BusinessUiState.CancelledOrderSize(it) },cancelledOrderAmount.map { BusinessUiState.CancelledOrderAmount(it) },totalSalesComparison.map { BusinessUiState.SalesComparison(it) },totalSalesComparisonSize.map { BusinessUiState.SalesComparisonSize(it) },totalCancelledSales.map { BusinessUiState.SalesComparisonCancelled(it) },totalCancelledSalesSize.map { BusinessUiState.SalesComparisonCancelledSize(it) }).onEach {
            _businessUiState.value = it
        }.launchIn(viewModelScope)
    }


    fun saveBusinessFilter(filter: DashboardFilter,buttonId: Int){
        viewModelScope.launch {
            grocerlyDataStore.setBusinessFilterState(filter,buttonId)
        }
    }

    fun resetBusinessFilter(){
        viewModelScope.launch {
            grocerlyDataStore.resetBusinessFilter()
        }
    }




    fun loadDataWithFilter(filter: DashboardFilter){
        viewModelScope.launch {
            loadDataForFilter(filter)
        }
    }

    private suspend fun loadDataForFilter(filter: DashboardFilter) {
        businessRepoImpl.loadDataForFilter(filter).collect { state ->
            when (state) {
                is BusinessUiState.TotalOrderSize -> totalActiveOrderSize.emit(state.totalSize)
                is BusinessUiState.TotalActiveOrderAmount -> totalActiveOrderAmount.emit(state.totalAmount)
                is BusinessUiState.CancelledOrderSize -> cancelledOrderSize.emit(state.totalSize)
                is BusinessUiState.CancelledOrderAmount -> cancelledOrderAmount.emit(state.totalAmount)
                is BusinessUiState.SalesComparison -> totalSalesComparison.emit(state.comparisonResult)
                is BusinessUiState.SalesComparisonSize -> totalSalesComparisonSize.emit(state.comparisonResult)
                is BusinessUiState.SalesComparisonCancelled -> totalCancelledSales.emit(state.comparisonResult)
                is BusinessUiState.SalesComparisonCancelledSize -> totalCancelledSalesSize.emit(state.comparisonResult)
            }
        }
    }


    suspend fun fetchTotalActiveOrderSize(startTime:Long?,endTime:Long?){
      if (NetworkUtils.isNetworkAvailable(getApplication())){
          val result =  businessRepoImpl.fetchTotalOrdersSize(startTime?: 0L,endTime?: 0L)
          totalActiveOrderSize.emit(result)
      }else{
          totalActiveOrderSize.emit(NetworkResult.Error("Enable Wifi or Mobile data"))
      }
    }

    suspend fun fetchTotalActiveOrderAmount(startTime:Long?,endTime:Long?){
        if (NetworkUtils.isNetworkAvailable(getApplication())){
            val result =  businessRepoImpl.fetchTotalAmountActiveOrders(startTime?: 0L,endTime?: 0L)
            totalActiveOrderAmount.emit(result)
        }
    }

    suspend fun fetchTotalCancelledOrderSize(startTime:Long?,endTime:Long?){
        if (NetworkUtils.isNetworkAvailable(getApplication())){
            val result =  businessRepoImpl.fetchCancelledOrdersSize(startTime?: 0L,endTime?: 0L)
            cancelledOrderSize.emit(result)
        }
    }

    suspend fun fetchTotalCancelledOrderAmount(startTime:Long?,endTime:Long?){
        if (NetworkUtils.isNetworkAvailable(getApplication())){
            val result =  businessRepoImpl.fetchTotalOrderLoss(startTime?: 0L,endTime?: 0L)
            cancelledOrderAmount.emit(result)
        }
    }



    suspend fun fetchTotalSalesComparison(
        currentStartTime: Long?,
        currentEndTime: Long?,
        previousStartTime: Long?,
        previousEndTime: Long?,
        filterType: DashboardFilter
    ) {
        if (NetworkUtils.isNetworkAvailable(getApplication())){
            val comparisonResult = businessRepoImpl.compareSalesAmount(currentStartTime?: 0L,currentEndTime?: 0L,previousStartTime?: 0L,previousEndTime?: 0L,filterType)
            totalSalesComparison.emit(comparisonResult)
        }else{
            totalSalesComparison.emit(NetworkResult.Error("Enable Wifi or Mobile data"))
        }
    }

    suspend fun compareTotalOrderAmountSize(startTime:Long?,endTime:Long?,previousStartTime: Long?,previousEndTime: Long?,filterType: DashboardFilter){
        if (NetworkUtils.isNetworkAvailable(getApplication())){
            val result =  businessRepoImpl.compareOrderSize(startTime?: 0L,endTime?: 0L,previousStartTime?: 0L,previousEndTime?: 0L,filterType)
            totalSalesComparisonSize.emit(result)
        }
    }


    suspend fun compareTotalCancelledOrderAmount(startTime:Long?,endTime:Long?,previousStartTime: Long?,previousEndTime: Long?,filterType: DashboardFilter){
        if (NetworkUtils.isNetworkAvailable(getApplication())){
            val result =  businessRepoImpl.compareCancelledOrders(startTime?: 0L,endTime?: 0L,previousStartTime?: 0L,previousEndTime?: 0L,filterType)
            totalCancelledSales.emit(result)
        }
    }

    suspend fun compareTotalCancelledOrderSize(startTime:Long?,endTime:Long?,previousStartTime: Long?,previousEndTime: Long?,filterType: DashboardFilter){
        if (NetworkUtils.isNetworkAvailable(getApplication())){
            val result =  businessRepoImpl.compareCancelledOrderSize(startTime?: 0L,endTime?: 0L,previousStartTime?: 0L,previousEndTime?: 0L,filterType)
            totalCancelledSalesSize.emit(result)
        }
    }


}