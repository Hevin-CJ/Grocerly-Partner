package com.example.grocerlypartners.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    var addressId: String = "",
    var firstName: String = "",
    var phoneNumber: String = "",
    var alternateNumber: String = "",
    var state: String = "",
    var city: String = "",
    var deliveryAddress: String = "",
    var landMark: String = "",
    var pinCode: String = "",
    var addressType: String = "",
    var default: Boolean = false
): Parcelable
