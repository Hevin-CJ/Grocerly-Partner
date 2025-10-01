package com.example.grocerlypartners.utils

import com.example.grocerlypartners.model.Product

fun validateProduct(product: Product):ProductValidation{

    if (product.image.isNullOrEmpty()) {return ProductValidation.failure("Please upload an  image")}

    if (product.itemName.isEmpty()) return ProductValidation.failure("Product Name is Mandatory")

    if (product.itemPrice ==null || product.itemPrice<= 0) return ProductValidation.failure("Product Price is Mandatory")

    if (product.maxQuantity==null || product.maxQuantity <= 0) return ProductValidation.failure("Required Maximum Quantity Per Order")

    if (product.maxQuantity > 25) return ProductValidation.failure("Maximum Quantity Can Only Be 25")

    if (product.category == ProductCategory.selectcatgory) return ProductValidation.failure("Please select a category")

    if (product.quantityType == QuantityType.selectQuantity) return ProductValidation.failure("Please select a type of quantity")

    if (product.packUpTime == PackUp.selectTime) return ProductValidation.failure("Please choose a valid packing time")

    return ProductValidation.success

}