package com.example.grocerlypartners.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.grocerlypartners.ConnectivityObserver
import com.example.grocerlypartners.model.Product
import com.example.grocerlypartners.repository.remote.HomeRepoImpl
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor (connectivityObserver: ConnectivityObserver,private val homeRepoImpl: HomeRepoImpl, application: Application):AndroidViewModel(application) {

    private val _product = MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val product: StateFlow<NetworkResult<List<Product>>> get() = _product.asStateFlow()

    private val _deleteProduct = MutableSharedFlow<NetworkResult<Product>>()
    val deleteProduct:LiveData<NetworkResult<Product>> get() = _deleteProduct.asLiveData()

    private val _enableProduct = Channel<NetworkResult<Unit>>()
    val enableProduct: Flow<NetworkResult<Unit>> get() = _enableProduct.receiveAsFlow()

    val networkState:StateFlow<Boolean> = connectivityObserver.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L),true)

    init {
        fetchProductAddedByPartnerFromFirebase()
    }


    fun fetchProductAddedByPartnerFromFirebase(){
      viewModelScope.launch {
         fetchProductFromFirebase()
      }
    }

    fun setProductEnable(product: Product,isEnabled: Boolean){
        viewModelScope.launch {
            setProductEnableDisable(product,isEnabled)
        }
    }

    private suspend fun fetchProductFromFirebase() {
       homeRepoImpl.fetchDataFromFirebaseToHome().collectLatest {
           _product.emit(it)
       }
    }

    private suspend fun setProductEnableDisable(product: Product,isEnabled: Boolean){
      if (NetworkUtils.isNetworkAvailable(getApplication())){
          homeRepoImpl.setProductEnableFirebase(product,isEnabled)
      }else{
          _enableProduct.send(NetworkResult.Error("Enable Wifi or Mobile Data"))
      }
    }

    fun deleteProduct(offer: Product) {
        viewModelScope.launch {
            deleteProductFromFirebase(offer)
        }
    }

    private suspend fun deleteProductFromFirebase(product: Product) {
        _deleteProduct.emit(NetworkResult.Loading())
        val deleteProduct = homeRepoImpl.deleteDataFromFirebase(product)
        _deleteProduct.emit(deleteProduct)
    }


}