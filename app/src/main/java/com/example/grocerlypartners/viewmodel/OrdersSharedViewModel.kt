package com.example.grocerlypartners.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerlypartners.model.CartProduct
import com.example.grocerlypartners.model.Order
import com.example.grocerlypartners.repository.remote.OrdersRepoImpl
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.utils.NetworkUtils
import com.example.grocerlypartners.utils.OrderStatus
import com.example.grocerlypartners.model.OrderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersSharedViewModel @Inject constructor(private val ordersRepoImpl: OrdersRepoImpl,application: Application)   : AndroidViewModel(application) {

    private val _orders = MutableStateFlow<OrderUiState?>(null)
    val orders get() = _orders.asStateFlow()



    private val _orderStatus = MutableStateFlow<NetworkResult<Unit>>(NetworkResult.UnSpecified())
    private val _pendingOrders = MutableStateFlow<NetworkResult<List<Order>>>(NetworkResult.UnSpecified())
    private val _acceptedOrders =MutableStateFlow<NetworkResult<List<Order>>>(NetworkResult.UnSpecified())
    private val _readyOrders = MutableStateFlow<NetworkResult<List<Order>>>(NetworkResult.UnSpecified())
    private val _shippedOrders = MutableStateFlow<NetworkResult<List<Order>>>(NetworkResult.UnSpecified())
    private val _cancelledOrders = MutableStateFlow<NetworkResult<Map<String, List<CartProduct>>>>(NetworkResult.UnSpecified())


    init {
        getPendingOrders()
        getAcceptedOrders()
        getShippedOrders()
        getReadyOrders()
        cleanCancelledOrders()
        getCancelledOrders()


        merge(_pendingOrders.map { OrderUiState.Pending(it) },_acceptedOrders.map { OrderUiState.Accepted(it) },_readyOrders.map { OrderUiState.Ready(it) },_shippedOrders.map { OrderUiState.Shipped(it) },_orderStatus.map { OrderUiState.OrderStatus(it) },_cancelledOrders.map { OrderUiState.Cancelled(it) }).onEach {it
            _orders.emit(it)
        }.launchIn(viewModelScope)
    }


    fun getPendingOrders(){
        viewModelScope.launch {
            ordersRepoImpl.fetchPendingOrders().collectLatest {
                _pendingOrders.emit(it)
            }
        }
    }


    fun getAcceptedOrders(){
        viewModelScope.launch {
            ordersRepoImpl.fetchAcceptedOrders().collectLatest {
                _acceptedOrders.emit(it)
            }
        }
    }

    fun cleanCancelledOrders(){
        viewModelScope.launch {
            ordersRepoImpl.cleanCancelledOrders()
        }
    }


    fun getReadyOrders(){
        viewModelScope.launch {
            ordersRepoImpl.fetchReadyOrders().collectLatest {
               _readyOrders.emit(it)
            }
        }
    }

    fun getShippedOrders(){
        viewModelScope.launch {
            ordersRepoImpl.fetchShippedOrders().collectLatest {
                _shippedOrders.emit(it)
            }
        }
    }

    fun getCancelledOrders(){
        viewModelScope.launch {
            ordersRepoImpl.fetchCancelledItems().collectLatest {
                _cancelledOrders.emit(it)
            }
        }
    }

    fun deleteOrder(order: Order){
        viewModelScope.launch {
            if (NetworkUtils.isNetworkAvailable(getApplication())){
                val deleteStatus = ordersRepoImpl.deleteOrderFromFirebase(order)
                _orderStatus.emit(deleteStatus)
            }else{
                _orderStatus.emit(NetworkResult.Error("Enable Wifi or Mobile Data"))
            }
        }
    }

    fun setOrderStatus(order: Order,orderStatus: OrderStatus){
        viewModelScope.launch {
            setOrderStatusInOrder(order,orderStatus)
        }
    }

    fun setCancellationStatus(order: Order,reason: String){
        viewModelScope.launch {
            setCancellationStatusOrder(order,reason)
        }
    }

    private suspend fun setOrderStatusInOrder(order: Order,orderStatus: OrderStatus){
        if (NetworkUtils.isNetworkAvailable(getApplication())){
            _orderStatus.emit(NetworkResult.Loading())
            val orderStatus = ordersRepoImpl.setOrderStateInOrder(order,orderStatus)
            _orderStatus.emit(orderStatus)
        }
    }

    private suspend fun setCancellationStatusOrder(order: Order,reason: String){
        if (NetworkUtils.isNetworkAvailable(getApplication())){
            val status = ordersRepoImpl.setCancellationStatus(order,reason)
            _orderStatus.emit(status)
        }else{
            _orderStatus.emit(NetworkResult.Error("Enable Wifi or Mobile Data"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        _orders.value = null
    }

}