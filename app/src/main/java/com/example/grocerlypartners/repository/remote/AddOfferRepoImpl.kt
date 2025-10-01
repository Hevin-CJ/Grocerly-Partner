package com.example.grocerlypartners.repository.remote

import com.example.grocerlypartners.model.OfferItem
import com.example.grocerlypartners.utils.Constants.OFFERS
import com.example.grocerlypartners.utils.Constants.PARTNERS
import com.example.grocerlypartners.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AddOfferRepoImpl @Inject constructor (private val db:FirebaseFirestore, private val auth:FirebaseAuth) {

     suspend fun addOfferToFirebase(offerItem: OfferItem):NetworkResult<String> {
        return try {

            val userId = auth.currentUser?.uid.toString()

            db.collection(PARTNERS)
                .document(userId)
                .collection(OFFERS)
                .document(offerItem.offerId)
                .set(offerItem)
                .await()

           NetworkResult.Success("Added Offer")

        }catch (e:Exception){
            NetworkResult.Error(e.message)
        }
    }

}