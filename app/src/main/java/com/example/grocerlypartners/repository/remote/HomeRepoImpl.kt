package com.example.grocerlypartners.repository.remote

import android.util.Log
import com.example.grocerlypartners.model.Product
import com.example.grocerlypartners.utils.Constants.PARTNERS
import com.example.grocerlypartners.utils.Constants.PRODUCTS
import com.example.grocerlypartners.utils.Mappers.toMap
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
class HomeRepoImpl @Inject constructor(private val db:FirebaseFirestore,private val auth:FirebaseAuth) {
    private val userId = auth.currentUser?.uid.toString()
    private val partnerRef =  db.collection(PARTNERS).document(userId).collection(PRODUCTS)
    private val FIRESTORE_TIMEOUT_MILLIS = 10000L

    fun fetchDataFromFirebaseToHome(): Flow<NetworkResult<List<Product>>> = callbackFlow {

        trySend(NetworkResult.Loading())

        val listener = partnerRef.addSnapshotListener { snapshot, error ->


            if (error != null) {
                trySend(NetworkResult.Error(error.message ?: "Unable to fetch Products, Please try later.."))
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                trySend(NetworkResult.Success(emptyList()))
                return@addSnapshotListener
            }

                val productList = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
                Log.d("productList", productList.toString())
                trySend(NetworkResult.Success(productList))

        }
        awaitClose {
            listener.remove()
        }
    }

    suspend fun setProductEnableFirebase(product: Product,isEnabled: Boolean): NetworkResult<Unit>{
        return try {
            Log.d("isenabledgot",isEnabled.toString())
            val updatedProduct = product.copy(isEnabled = isEnabled)
            partnerRef.document(product.productId).update(updatedProduct.toMap()).await()
            NetworkResult.Success(Unit)
        }catch (e: Exception){
            NetworkResult.Error(e.message ?: "Unable to update product, Please try later..")
        }
    }


    suspend fun deleteDataFromFirebase(product: Product): NetworkResult<Product> {
        return try {

            partnerRef.document(product.productId).delete().await()

            NetworkResult.Success(product)

        } catch (e: Exception) {

            NetworkResult.Error(e.message)
        }

    }


}