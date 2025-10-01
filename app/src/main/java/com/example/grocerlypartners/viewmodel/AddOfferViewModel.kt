package com.example.grocerlypartners.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.grocerlypartners.model.OfferItem
import com.example.grocerlypartners.repository.remote.AddOfferRepoImpl
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.utils.OfferState
import com.example.grocerlypartners.utils.OfferValidation
import com.example.grocerlypartners.utils.validateOffer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddOfferViewModel @Inject constructor(application:Application,private val addOfferRepoImpl: AddOfferRepoImpl):AndroidViewModel(application) {

    private val _offerAdded = MutableSharedFlow<NetworkResult<String>>()
    val offerAdded:LiveData<NetworkResult<String>> get() = _offerAdded.asLiveData()

    private var _offerState = Channel<OfferState>()
     val offerstate:Flow<OfferState> get() = _offerState.receiveAsFlow()



    fun insertOfferIntoIntoFirebase(offerItem: OfferItem){
        viewModelScope.launch {
          validateAndInsertOffer(offerItem)
        }
    }

    private suspend fun validateAndInsertOffer(offerItem: OfferItem) {
        if (isOffersAreValidated(offerItem)){
            _offerAdded.emit(NetworkResult.Loading())
            val result = addOfferRepoImpl.addOfferToFirebase(offerItem)
            _offerAdded.emit(result)
        }else{
            emitOfferValidationErrors(offerItem)
        }
    }


    private suspend fun emitOfferValidationErrors(offerItem: OfferItem) {
        val offerState = OfferState(validateOffer(offerItem))
        _offerState.send(offerState)
    }


    private fun isOffersAreValidated(offerItem: OfferItem): Boolean {
        val isOfferValidated = validateOffer(offerItem)
        return isOfferValidated is OfferValidation.Success
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        _offerState.close()
    }

}