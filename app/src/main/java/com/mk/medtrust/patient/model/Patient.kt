package com.mk.medtrust.patient.model

data class Patient(
    var uid: String = "",
    val email: String = "",
    val dob: String = "",
    val mobile: String = "",
    var gender: String = "",
    var name: String = ""
)

fun Patient.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()

    map["dob"] = dob
    map["mobile"] = mobile
    map["gender"] = gender
    map["name"] = name

    return map
}