package com.example.grocerlypartners.model

import android.os.Parcelable
import com.example.grocerlypartners.utils.PackUp
import com.example.grocerlypartners.utils.ProductCategory
import com.example.grocerlypartners.utils.QuantityType
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val productId:String="",
    val partnerId:String="",
    val image:String?="",
    val itemName:String="",
    val itemPrice:Int?=0,
    val itemOriginalPrice: Int?=0,
    val category: ProductCategory = ProductCategory.selectcatgory,
    val itemRating:Double ?= 5.0,
    val totalRating:Int ?= 0,

    @get:PropertyName("isEnabled")
    var isEnabled: Boolean=true,
    val maxQuantity: Int?=1,
    val quantityType: QuantityType = QuantityType.selectQuantity,
    val packUpTime: PackUp = PackUp.selectTime
) : Parcelable
