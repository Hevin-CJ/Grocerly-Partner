package com.example.grocerlypartners.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.grocerlypartners.model.OfferItem
import com.example.grocerlypartners.repository.remote.UpdateOfferRepoImpl
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
class UpdateOfferViewModel @Inject constructor(private val updateOfferRepoImpl:UpdateOfferRepoImpl ):ViewModel() {

    private val _updateOffer=MutableSharedFlow<NetworkResult<String>>()
    val updateOffer:LiveData<NetworkResult<String>> get() = _updateOffer.asLiveData()

    private val _isOfferValidated = Channel<OfferState>()
    val isOfferValidated:Flow<OfferState> get() = _isOfferValidated.receiveAsFlow()

    fun updateOfferIntoFb(offerItem: OfferItem){
        viewModelScope.launch { handleNetworkResultUpdateIntoFirebase(offerItem) }
    }

    private suspend fun handleNetworkResultUpdateIntoFirebase(offerItem: OfferItem) {
        if (isOfferValidated(offerItem)){
            _updateOffer.emit(NetworkResult.Loading())
            val updated = updateOfferRepoImpl.updateOfferIntoDatabase(offerItem)
            _updateOffer.emit(updated)
        }else{
            emitValidationErrors(offerItem)
        }
    }

    private suspend fun emitValidationErrors(offerItem: OfferItem) {
        val validationError = OfferState(validateOffer(offerItem))
        _isOfferValidated.send(validationError)
    }


    private fun isOfferValidated(offerItem: OfferItem):Boolean{
        val validation = validateOffer(offerItem)
        return validation is OfferValidation.Success
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        _isOfferValidated.close()
    }

}