package com.mk.medtrust.doctor.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Doctor(
    var uid : String = "",
    val name: String = "",
    val email: String ="",
    val contact: String="",
    val medicalLicenseNo: String ="",
    val specialisation: String="",
    val experience : String = "",
    val rating : String = "",
    val rateNo : String = "",
    val consultationFee : String = "",
    val availability : Availability = Availability(),
    val hospital : String = ""
) : Parcelable {
   @Parcelize
    data class Availability(
        val days : List<String> = emptyList(),
        val startTime : String = "",
        val endTime : String = ""
    ) : Parcelable
}

fun Doctor.toMap() : Map<String,String>{
    val map = mutableMapOf<String,String>()

    map["experience"] = experience
    map["consultationFee"] = consultationFee

    return map
}