package com.example.grocerlypartners

import com.example.grocerlypartners.model.Product

interface HomeAdaptorListener {
    fun isProductEnabled(product: Product,isEnabled: Boolean)
}