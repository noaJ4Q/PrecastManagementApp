package com.example.precastmanagementapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PrecastElement (
    val dbId: Int = 0,
    val name: String = "",
    val category: String = "",
    val type: String = "",
    val description: String = "Unknown",
    val status: String = "No iniciado",
    val rfidTag: String? = null,
    val expectedDelivery: String? = null,
    val expectedInstallation: String? = null
) : Parcelable {
    fun isComplete(): Boolean {
        return rfidTag != null &&
                expectedDelivery != null &&
                expectedInstallation != null
    }
}