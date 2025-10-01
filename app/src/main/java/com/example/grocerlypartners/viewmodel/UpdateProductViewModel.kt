package com.example.grocerlypartners.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.grocerlypartners.model.Product
import com.example.grocerlypartners.repository.remote.UpdateProductRepoImpl
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.utils.ProductCategory
import com.example.grocerlypartners.utils.ProductValidation
import com.example.grocerlypartners.utils.productValidationState
import com.example.grocerlypartners.utils.validateProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UpdateProductViewModel @Inject constructor(private val updateProductRepoImpl: UpdateProductRepoImpl):ViewModel() {

    private val _updateProduct = MutableSharedFlow<NetworkResult<String>>()
    val updateProduct:LiveData<NetworkResult<String>> get() = _updateProduct.asLiveData()

    private val _productValidationState = Channel<productValidationState>()
    val productValidationState:Flow<productValidationState> get() = _productValidationState.receiveAsFlow()

    private val _uploadImageState = MutableStateFlow<NetworkResult<String>>(NetworkResult.UnSpecified())
    val uploadImageState: StateFlow<NetworkResult<String>> get() = _uploadImageState.asStateFlow()


    fun updateDataIntoFirebase(product:Product) = viewModelScope.launch {
        updateDataIntoFirebaseSafeCall(product)
    }

    fun uploadImageToFirebase(uri:Uri){
        viewModelScope.launch {
            insertImageToFirebase(uri)
        }
    }


    private suspend fun updateDataIntoFirebaseSafeCall(product: Product) {
        if (isProductValidated(product)) {
            val result = updateProductRepoImpl.updateProduct(product)
            _updateProduct.emit(result)

        }else{
            emitProductValidationErrors(product)
        }
    }

    private suspend fun insertImageToFirebase(uri:Uri) {
        _uploadImageState.emit(NetworkResult.Loading())
        updateProductRepoImpl.uploadImageToFirebase(uri).collectLatest {
            _uploadImageState.emit(it)
        }
    }

    private suspend fun emitProductValidationErrors(product: Product) {
        val state = productValidationState(validateProduct(product))
        _productValidationState.send(state)
    }


    private fun isProductValidated(product: Product):Boolean {
        val isProductValidated = validateProduct(product)
        return isProductValidated is ProductValidation.success
    }

    fun parseStringIntoProduct(productCategory: String):ProductCategory{

        return when(productCategory){
            "Fruits & Vegies" -> ProductCategory.FruitsandVegies
            "Frozen Foods" -> ProductCategory.FrozenFoods
            "Bread & Bakery" ->   ProductCategory.BreadandBakery
            "Personal Care" -> ProductCategory.PersonalCare
            "House Holds" -> ProductCategory.Households
            "Health Care" -> ProductCategory.HealthCare
            "Meat" ->ProductCategory.Meat
            else ->{
                ProductCategory.selectcatgory
            }
        }
    }

    fun parseProductIntoInt(category: ProductCategory):Int {
        return when(category){
            ProductCategory.selectcatgory -> 0
            ProductCategory.FruitsandVegies -> 1
            ProductCategory.FrozenFoods -> 2
            ProductCategory.BreadandBakery -> 3
            ProductCategory.PersonalCare -> 4
            ProductCategory.Households -> 5
            ProductCategory.HealthCare -> 6
            ProductCategory.Meat -> 7
        }
    }

    override fun onCleared() {
        super.onCleared()
        _productValidationState.close()
    }

}