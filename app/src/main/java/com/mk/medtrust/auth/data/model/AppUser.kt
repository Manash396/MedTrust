package com.mk.medtrust.auth.data.model

import android.os.Parcelable
import com.mk.medtrust.util.AppointmentStatus
import kotlinx.parcelize.Parcelize
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

@Parcelize
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
    val createdAt: Long = System.currentTimeMillis(),
    var prescription : Prescription? = null
): Parcelable

@Parcelize
data class Prescription(
    val medicines: List<Medicine> = emptyList(),
    val patientDob : String = "",
    val patientGender : String = "",
    val doctorLisc : String = "",
    val hospitalName : String = "",
    val notes: String = ""
) : Parcelable

@Parcelize
data class Medicine(
    val name: String = "",
    val dose: String = "",      // e.g., "500mg"
    val frequency: String = ""  // e.g., "Twice a day"
) : Parcelable


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


