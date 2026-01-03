package com.mk.medtrust.auth.data.model

data class AppUser(
    val userId: String = "",
    val role : String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val name : String = "",
    val dob : String = "",
    val mobile: String = "",
    var gender: String = "",
    val specialisation: String="",
)
