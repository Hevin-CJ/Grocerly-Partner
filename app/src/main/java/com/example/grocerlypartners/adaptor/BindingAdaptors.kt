package com.example.grocerlypartners.adaptor

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.navigation.findNavController
import coil3.load
import coil3.request.crossfade
import com.bumptech.glide.Glide
import com.example.grocerlypartners.R
import com.example.grocerlypartners.fragments.HomeDirections
import com.example.grocerlypartners.fragments.UpdateProductDirections
import com.example.grocerlypartners.model.Product
import com.example.grocerlypartners.utils.ProductCategory
import com.example.grocerlypartners.utils.QuantityType
import com.google.android.material.materialswitch.MaterialSwitch
import java.io.File

object BindingAdaptors {


    @BindingAdapter("parseCategoryIntoString")
    @JvmStatic
    fun parseCategoryIntoString(view:TextView, category: ProductCategory){
        val categoryInString = convertCategoryIntoString(category)
        view.text = categoryInString
    }


   private fun convertCategoryIntoString(category: ProductCategory):String{
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

    @BindingAdapter("app:convertQuantityIntoString")
    @JvmStatic
     fun convertQuantityIntoString(textView: TextView,quantityType: QuantityType){
        val text =  when(quantityType){
            QuantityType.selectQuantity -> "/ Not Selected"
            QuantityType.perKilogram -> "/ Kilogram"
            QuantityType.perLiter -> "/ Liter"
            QuantityType.perPiece -> "/ Piece"
            QuantityType.perPacket -> "/ Packet"
        }
        textView.text = text
    }


    @BindingAdapter("setImageToView")
    @JvmStatic
    fun setImageToView(view:ImageView,src:String){

        try {
            Glide.with(view.context)
                .load(src)
                .into(view)

        }catch (e:Exception){

        }
    }


    @BindingAdapter("ActionToUpdateProduct")
    @JvmStatic
    fun ActionToUpdateProduct(view:CardView,product: Product){
        view.setOnClickListener {
            val action = HomeDirections.actionProductsToUpdateProduct(product)
            view.findNavController().navigate(action)
        }
    }



}