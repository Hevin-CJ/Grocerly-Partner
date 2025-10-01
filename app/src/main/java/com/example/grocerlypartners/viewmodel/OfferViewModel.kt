package com.example.grocerlypartners.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerlypartners.model.OfferItem
import com.example.grocerlypartners.repository.remote.OfferRepoImpl
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferViewModel @Inject constructor(private val offerRepoImpl: OfferRepoImpl,application: Application): AndroidViewModel(application) {

    private val _deletedOffer = Channel<NetworkResult<OfferItem>>()
    val deletedOffer: Flow<NetworkResult<OfferItem>> get() = _deletedOffer.receiveAsFlow()

    private val _offerItems = MutableStateFlow<NetworkResult<List<OfferItem>>>(NetworkResult.UnSpecified())
    val offerItems: StateFlow<NetworkResult<List<OfferItem>>> get() = _offerItems.asStateFlow()


    init {
        getOfferFromFirebase()
    }

    fun getOfferFromFirebase(){
        viewModelScope.launch {
            fetchOfferFromDb()
        }
    }


    fun deleteOfferFromFirebase(offerItem: OfferItem){
        viewModelScope.launch {
            handleNetworkResultForDeleteOffer(offerItem)
        }
    }

    private suspend fun handleNetworkResultForDeleteOffer(offerItem: OfferItem) {
       if (NetworkUtils.isNetworkAvailable(getApplication())){
           _deletedOffer.send(NetworkResult.Loading())
           val deletedOffer = offerRepoImpl.deleteOfferFromFirebase(offerItem)
           _deletedOffer.send(deletedOffer)
       }else{
           _deletedOffer.send(NetworkResult.Error("Enable Wifi or Mobile data"))
       }
    }

    private suspend fun fetchOfferFromDb() {
        offerRepoImpl.retrieveOfferFromFirebase().collectLatest {
            _offerItems.emit(it)
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }


}