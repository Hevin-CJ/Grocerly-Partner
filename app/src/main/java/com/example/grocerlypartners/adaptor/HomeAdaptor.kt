package com.example.grocerlypartners.adaptor

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.grocerlypartners.HomeAdaptorListener
import com.example.grocerlypartners.R
import com.example.grocerlypartners.databinding.ProductHomeRclayoutBinding
import com.example.grocerlypartners.model.OfferItem
import com.example.grocerlypartners.model.Product

class HomeAdaptor(private val homeAdaptorListener: HomeAdaptorListener): ListAdapter<Product, HomeAdaptor.HomeViewHolder>(ProductDiffCallback) {


    object ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    inner class HomeViewHolder(private val binding: ProductHomeRclayoutBinding):ViewHolder(binding.root){

        fun bindProduct(product: Product){

            binding.switch2.setOnCheckedChangeListener(null)

            binding.product = product
            binding.executePendingBindings()

            val isEnabled = product.isEnabled
            binding.switch2.isChecked = isEnabled

            applySwitchColors(product.isEnabled)

            binding.switch2.setOnCheckedChangeListener { _, isChecked ->
                homeAdaptorListener.isProductEnabled(product, isChecked)
            }

        }

        private fun applySwitchColors(enabled: Boolean) {
            when(enabled){
                true ->{


                    val greenColor = ContextCompat.getColor(binding.root.context, R.color.green)
                    val lightGreenColor = ContextCompat.getColor(binding.root.context, R.color.slight_green)


                    binding.switch2.thumbTintList = ColorStateList.valueOf(greenColor)
                    binding.switch2.trackTintList = ColorStateList.valueOf(lightGreenColor)


                }
                false -> {
                    val greyColor = ContextCompat.getColor(binding.root.context, R.color.grey)
                    val lightGreyColor = ContextCompat.getColor(binding.root.context, R.color.lightgrey)

                    binding.switch2.thumbTintList = ColorStateList.valueOf(greyColor)
                    binding.switch2.trackTintList = ColorStateList.valueOf(lightGreyColor)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return  HomeViewHolder(ProductHomeRclayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }


    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
       val currentProduct= getItem(position)
        holder.bindProduct(currentProduct)
    }

    fun getProduct(position: Int): Product {
        return getItem(position)
    }

}