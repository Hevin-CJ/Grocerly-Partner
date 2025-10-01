package com.example.grocerlypartners.repository.remote

import com.example.grocerlypartners.preferences.GrocerlyDataStore
import com.example.grocerlypartners.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class SettingsRepoImpl @Inject constructor(private val auth: FirebaseAuth,private val db:FirebaseFirestore,private val grocerlyDataStore: GrocerlyDataStore) {


    suspend fun signOut(): NetworkResult<String> {

        return try {
            auth.signOut()
            grocerlyDataStore.setLoginState(false)

            return if (auth.currentUser==null){
                NetworkResult.Success("Logged Out")
            }else{
                NetworkResult.Error("Failed To Logout")
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message)
        }
    }

}