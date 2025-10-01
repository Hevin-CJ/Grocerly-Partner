package com.example.grocerlypartners.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerlypartners.repository.remote.SettingsRepoImpl
import com.example.grocerlypartners.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val settingsRepoImpl: SettingsRepoImpl):ViewModel() {

    private val _logout = Channel<NetworkResult<String>>()
    val logout: Flow<NetworkResult<String>> get() = _logout.receiveAsFlow()

    fun logoutFromFirebase(){
        viewModelScope.launch { handleNetworkResultLogout() }
    }

    private suspend fun handleNetworkResultLogout() {
       _logout.send(NetworkResult.Loading())
        val state = settingsRepoImpl.signOut()
        _logout.send(state)
    }

}