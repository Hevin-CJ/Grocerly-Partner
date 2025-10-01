package com.example.grocerlypartners.utils

import com.example.grocerlypartners.model.Product

object Mappers{

    fun Product.toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "productId" to this.productId,
            "partnerId" to this.partnerId,
            "image" to this.image,
            "itemName" to this.itemName,
            "itemPrice" to this.itemPrice,
            "itemOriginalPrice" to this.itemOriginalPrice,
            "category" to this.category.name,
            "itemRating" to this.itemRating,
            "totalRating" to this.totalRating,
            "isEnabled" to this.isEnabled,
            "maxQuantity" to this.maxQuantity,
            "quantityType" to this.quantityType.name,
            "packUpTime" to this.packUpTime.name
        )
    }


}