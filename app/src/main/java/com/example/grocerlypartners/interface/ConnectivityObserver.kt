package com.example.grocerlypartners

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun observe():  Flow<Boolean>
}
