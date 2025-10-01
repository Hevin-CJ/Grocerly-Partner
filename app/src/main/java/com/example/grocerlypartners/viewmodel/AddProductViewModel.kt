package com.example.grocerlypartners.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.model.Product
import com.example.grocerlypartners.repository.remote.AddProductRepoImpl
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
class AddProductViewModel @Inject constructor(private val addProductRepoImpl: AddProductRepoImpl,application: Application):AndroidViewModel(application) {

    private val _uploadProduct = MutableSharedFlow<NetworkResult<String>>()
    val uploadProduct:LiveData<NetworkResult<String>> get() = _uploadProduct.asLiveData()

    private val _productValState = Channel<productValidationState>()
    val productValState:Flow<productValidationState> get() = _productValState.receiveAsFlow()

    private val _uploadImageState = MutableStateFlow<NetworkResult<String>>(NetworkResult.UnSpecified())
    val uploadImageState: StateFlow<NetworkResult<String>> get() = _uploadImageState.asStateFlow()



    fun uploadProductToFirebase(product: Product){
        viewModelScope.launch {
           insertDataIntoDb(product)
        }
    }

    fun uploadImageToFirebase(uri:Uri){
        viewModelScope.launch {
            insertImageToFirebase(uri)
        }
    }

    private suspend fun insertImageToFirebase(uri:Uri) {
        _uploadImageState.emit(NetworkResult.Loading())
        addProductRepoImpl.uploadImageToFirebase(uri).collectLatest {
            _uploadImageState.emit(it)

        }
    }


    private suspend fun insertDataIntoDb(product: Product) {
        if (isProductValidated(product)){
            _uploadProduct.emit(NetworkResult.Loading())
            val uploaded = addProductRepoImpl.uploadDataToDb(product)
            _uploadProduct.emit(uploaded)
        }else{
            emitProductionValidationErrors(product)
        }
    }


    fun parseProductIntoString(category: ProductCategory):String {
        return when(category){
            ProductCategory.selectcatgory -> "Select a Category"
            ProductCategory.FruitsandVegies -> "Fruits & Vegies"
            ProductCategory.FrozenFoods -> "Frozen Foods"
            ProductCategory.BreadandBakery -> "Bread & Bakery"
            ProductCategory.PersonalCare -> "Personal Care"
            ProductCategory.Households -> "House Holds"
            ProductCategory.HealthCare -> "Health Care"
            ProductCategory.Meat -> "Meat"
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

    private suspend fun emitProductionValidationErrors(product: Product) {
        val productValState = productValidationState(validateProduct(product))
        _productValState.send(productValState)
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

    private fun isProductValidated(product: Product): Boolean {
        val isvalidated = validateProduct(product)
        return isvalidated is ProductValidation.success
    }


    override fun onCleared() {
        super.onCleared()
        _productValState.close()
    }


}