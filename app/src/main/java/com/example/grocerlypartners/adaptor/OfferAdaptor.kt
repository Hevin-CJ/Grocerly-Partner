package com.example.grocerlypartners.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.grocerlypartners.databinding.OffersRcLayoutBinding
import com.example.grocerlypartners.fragments.OffersDirections
import com.example.grocerlypartners.model.OfferItem

class OfferAdaptor:RecyclerView.Adapter<OfferAdaptor.OfferViewHolder>(){

    private var offerItems:List<OfferItem> = emptyList()

    inner class OfferViewHolder(private val binding:OffersRcLayoutBinding):ViewHolder(binding.root){

        fun bindOffer(offerItem: OfferItem){
            binding.apply {
                txtviewofferText.text = offerItem.descriptionText
                txtviewofferText.setTextColor(offerItem.descriptionTextColor.toColorInt())
                shopnowbtn.setText(offerItem.buttonText)
                shopnowbtn.setBackgroundColor(offerItem.buttonBgColor.toColorInt())
                shopnowbtn.setTextColor(offerItem.buttonTxtColor.toColorInt())
                crdviewpreview.setCardBackgroundColor(offerItem.offerBgColor.toColorInt())
                Glide.with(binding.root.context).load(offerItem.offerImage).into(imgviewItem)
            }
        }

        fun actionToUpdateOffer(offerItem: OfferItem){
            binding.apply {
               crdviewpreview.setOnClickListener {
                   val action = OffersDirections.actionOffersToUpdateOffer(offerItem)
                   it.findNavController().navigate(action)
               }
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
       return OfferViewHolder(OffersRcLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return offerItems.size
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
       val currentOffer = offerItems[position]
        holder.bindOffer(currentOffer)
        holder.actionToUpdateOffer(currentOffer)
    }

    fun setOffers(offers:List<OfferItem>){
        this.offerItems = offers
        notifyDataSetChanged()
    }

    fun getOffer(position: Int):OfferItem{
        return offerItems[position]
    }

    fun getPosition(offer: OfferItem):Int{
        return offerItems.indexOf(offer)
    }


    fun removeOffer(postion:Int){
        if (postion in offerItems.indices){
            offerItems = offerItems.toMutableList().apply { removeAt(postion) }
            notifyItemRemoved(postion)
            notifyItemRangeChanged(postion,offerItems.size)
        }
    }


    fun addDeletedOffer(offer: OfferItem){
        val position = offerItems.size
        offerItems = offerItems.toMutableList().apply { add(position,offer) }
        notifyItemInserted(position)
    }

}