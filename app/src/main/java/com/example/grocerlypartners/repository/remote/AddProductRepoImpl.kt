package com.example.grocerlypartners.repository.remote


import android.net.Uri
import com.example.grocerlypartners.model.Product
import com.example.grocerlypartners.utils.Constants.PARTNERS
import com.example.grocerlypartners.utils.Constants.PRODUCTS
import com.example.grocerlypartners.utils.Mappers.toMap
import com.example.grocerlypartners.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject


class AddProductRepoImpl @Inject constructor(private val db:FirebaseFirestore,private val auth:FirebaseAuth,private val storage: FirebaseStorage) {

    val userId = auth.currentUser?.uid.toString()
    private val productRef = db.collection(PARTNERS).document(userId).collection(PRODUCTS)

     suspend fun uploadDataToDb(product: Product):NetworkResult<String> {
         return try {

             if (userId.isEmpty()){
                 return NetworkResult.Error("User not found, Please try later...")
             }

             val updatedProduct = product.copy(partnerId = userId)

             productRef.document(product.productId)
                .set(updatedProduct.toMap(), SetOptions.merge())
                .await()

            NetworkResult.Success("Added ${product.itemName}")

        }catch (e:Exception){

           NetworkResult.Error(e.message)
        }
    }


    fun uploadImageToFirebase(uri: Uri): Flow<NetworkResult<String>> = callbackFlow {

        if (uri.path.isNullOrEmpty()){
             trySend(NetworkResult.Error("Image path not found\nPlease try again"))
             return@callbackFlow
        }

        if (userId.isEmpty()){
            trySend(NetworkResult.Error("User not found\nPlease login again.."))
            return@callbackFlow
        }

        val storageRef = storage.reference.child("$PARTNERS/$userId/images/${UUID.randomUUID()}.jpg")
        val snapshot = storageRef.putFile(uri).await()
        val imageUrl = snapshot.storage.downloadUrl.await().toString()
        trySend(NetworkResult.Success(imageUrl))

        awaitClose()
    }

}