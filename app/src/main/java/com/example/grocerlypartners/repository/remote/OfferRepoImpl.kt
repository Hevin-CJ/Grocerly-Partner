package com.example.grocerlypartners.repository.remote

import com.example.grocerlypartners.model.OfferItem
import com.example.grocerlypartners.utils.Constants.OFFERS
import com.example.grocerlypartners.utils.Constants.PARTNERS
import com.example.grocerlypartners.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@ActivityRetainedScoped
class OfferRepoImpl @Inject constructor(private val db:FirebaseFirestore,private val auth:FirebaseAuth) {

  private  val userId  = auth.currentUser?.uid.toString()

   private val offerRef = db.collection(PARTNERS).document(userId).collection(OFFERS)

    fun retrieveOfferFromFirebase(): Flow<NetworkResult<List<OfferItem>>> = callbackFlow {

        trySend(NetworkResult.Loading())
        val listener = offerRef.addSnapshotListener { snapshot, exception ->

            if (exception != null) {
                trySend(NetworkResult.Error(exception.message ?: "Unable to fetch offers"))
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                trySend(NetworkResult.Success(emptyList()))
                return@addSnapshotListener
            }


            val offerList = snapshot.documents.mapNotNull { it.toObject(OfferItem::class.java) }
            trySend(NetworkResult.Success(offerList))


        }
        awaitClose {
            listener.remove()
        }
    }

    suspend fun deleteOfferFromFirebase(offerItem: OfferItem): NetworkResult<OfferItem> {
        return try {
            offerRef
                .document(offerItem.offerId)
                .delete()
                .await()

            NetworkResult.Success(offerItem)


        } catch (e: Exception) {
            NetworkResult.Error(e.message)
        }
    }

}