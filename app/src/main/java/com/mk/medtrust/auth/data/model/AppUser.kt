package com.mk.medtrust.auth.data.model

import com.mk.medtrust.util.AppointmentStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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

data class Appointment(
    val appointmentId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val dateId: String = "",
    val slotId: String = "",
    val slotTime: String = "",
    var status: AppointmentStatus = AppointmentStatus.BOOKED,
    val createdAt: Long = System.currentTimeMillis()
)


fun Appointment.toLocalDateTime(): LocalDateTime {
    val dateParts = dateId.split("_")
    val day = dateParts[0].toInt()
    val month = dateParts[1].toInt()
    val year = dateParts[2].toInt()

    val timeFormater = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    val localTime  = LocalTime.parse(slotTime,timeFormater)

    return LocalDateTime.of(
        LocalDate.of(year,month,day),
        localTime
    )
}


