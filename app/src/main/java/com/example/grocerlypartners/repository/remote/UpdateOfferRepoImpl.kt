package com.example.grocerlypartners.repository.remote

import com.example.grocerlypartners.model.OfferItem
import com.example.grocerlypartners.utils.Constants.OFFERS
import com.example.grocerlypartners.utils.Constants.PARTNERS
import com.example.grocerlypartners.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UpdateOfferRepoImpl @Inject constructor(private val db:FirebaseFirestore,private val auth: FirebaseAuth) {


    suspend fun  updateOfferIntoDatabase(offerItem: OfferItem):NetworkResult<String>{
        return try {

            val userId = auth.currentUser?.uid.toString()

            db.collection(PARTNERS)
                .document(userId)
                .collection(OFFERS)
                .document(offerItem.offerId)
                .update(offerItem.toHashMap())
                .await()

            NetworkResult.Success("Updated Offer")
        }catch (e:Exception){
            NetworkResult.Error(e.message)
        }
    }

    private fun OfferItem.toHashMap():HashMap<String,Any>{
        return hashMapOf(
            "offerId" to offerId,
            "offerImage" to offerImage,
            "offerBgColor" to offerBgColor,
            "buttonText" to buttonText,
            "buttonBgColor" to buttonBgColor,
            "buttonTxtColor" to buttonTxtColor,
            "descriptionText" to descriptionText,
            "descriptionTextColor" to descriptionTextColor
        )
    }

}