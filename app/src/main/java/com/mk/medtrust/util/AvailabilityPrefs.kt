package com.mk.medtrust.util

import com.google.gson.Gson
import com.mk.medtrust.doctor.model.Doctor
import com.yourpackage.app.AppPreferences

object AvailabilityPrefs {

    private val gson = Gson()

    fun saveDoctorAvailability(av: Doctor.Availability) {
        val jsonString = gson.toJson(av)
        AppPreferences.setString(AppConstant.AVAILABILITY, jsonString)
    }

    fun getDoctorAvailability(): Doctor.Availability {
        val jsonString = AppPreferences.getString(AppConstant.AVAILABILITY)

        return if (!jsonString.isEmpty()) {
            gson.fromJson(jsonString, Doctor.Availability::class.java)
        } else {
            Doctor.Availability()
        }
    }
}