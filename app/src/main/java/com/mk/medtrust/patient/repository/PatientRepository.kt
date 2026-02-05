package com.mk.medtrust.patient.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.patient.model.toMap
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.AppointmentStatus
import com.mk.medtrust.util.Result
import com.yourpackage.app.AppPreferences
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(
    private val storeDb: FirebaseFirestore,
    private val auth: FirebaseAuth
)  {


    suspend fun getDoctorList() : Result<List<Doctor>>{
        return try {
            val snapshot = storeDb.collection("doctors")
                .get()
                .await()

            val doctor = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Doctor::class.java)
            }

            Result.Success(doctor)
        }catch (e : Exception){
            Result.Error(e.message ?: "Failed to fetch Doctor List")
        }
    }
    suspend fun logout() : Result<String>{
        return try {
            auth.signOut()

            AppPreferences.clearAll()

            Result.Success("Logout is successful")
        }catch (e : Exception){
            Result.Error(e.message ?: "Logout failed")
        }
    }

    suspend fun updatePatientProfile(patient : Patient) : Result<String>{
        return try {
            val uid = auth.currentUser?.uid ?: return Result.Error("User not logged in")

            val updatedMap = patient.toMap()

            storeDb.collection("patients").document(uid)
                .update(updatedMap)
                .await()

            storeDb.collection("users").document(uid)
                .update(updatedMap)
                .await()

            Result.Success("Updated Successfully")

        }catch (e: Exception){
            Result.Error(e.message ?: "Update failed")
        }
    }

    suspend fun appointmentBooking(appointment: Appointment) : Result<String>{
        return try {
            val doctorBookingDateRef = storeDb.collection("doctors")
                .document(appointment.doctorId)
                .collection("bookings")
                .document(appointment.dateId)


            val patientAppointmentRef = storeDb.collection("patients")
                .document(appointment.patientId)
                .collection("appointments")
                .document(appointment.appointmentId)


            storeDb.runTransaction { transaction ->
                // read first
                val doctorAppointmentRef = doctorBookingDateRef.collection("appointments")
                    .document(appointment.appointmentId)

                val snapshot = transaction.get(doctorAppointmentRef)
                if (snapshot.exists()) {
                    throw Exception("Slot already booked")
                }
               // 2nd writes
                transaction.set(
                    doctorBookingDateRef,
                    mapOf(
                        "date" to appointment.dateId,
                        "createdAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )

                transaction.set(doctorAppointmentRef,appointment)
                transaction.set(patientAppointmentRef,appointment)
            }.await()

            Result.Success("Appointment booked successfully")
        }catch (e : Exception){
            Result.Error(e.message ?: "Appointment booking failed")
        }
    }

    suspend fun getAllAppointmentsByDoctor(doctorId : String) : Result<List<Appointment>> {
        return try {
            val allAppointments  = mutableListOf<Appointment>()

            val dateSnapshot = storeDb.collection("doctors")
                .document(doctorId)
                .collection("bookings")
                .get()
                .await()

            Log.d("Krishna",dateSnapshot.toString())

            for(dateDoc in dateSnapshot.documents){

                val appointmentSnapshot = storeDb.collection("doctors")
                    .document(doctorId)
                    .collection("bookings")
                    .document(dateDoc.id)
                    .collection("appointments")
                    .get().await()

                allAppointments.addAll(
                    appointmentSnapshot.toObjects(Appointment::class.java)
                )
            }

            Result.Success(allAppointments)
        }catch (e : Exception){
            Result.Error(e.message ?: "Failed to fetch bookings")
        }
    }

    suspend fun getAllAppointments(patientId : String) : Result<List<Appointment>> {
        return try {
            val allAppointments  = mutableListOf<Appointment>()

            val appointmentSnapshot = storeDb.collection("patients")
                .document(patientId)
                .collection("appointments")
                .get()
                .await()


            Log.d("Krishna",appointmentSnapshot.toString())

            allAppointments.addAll(appointmentSnapshot.toObjects(Appointment::class.java))

            Result.Success(allAppointments)
        }catch (e : Exception){
            Result.Error(e.message ?: "Failed to fetch appointments")
        }
    }


    suspend fun getAppointmentDetail(apptId : String): Result<Appointment>{
        return try {
            val appointmentSnapshot = storeDb.collection("patients")
                .document(AppPreferences.getString(AppConstant.UID))
                .collection("appointments")
                .document(apptId)
                .get()
                .await()

            val appointment = appointmentSnapshot.toObject(Appointment::class.java)
            if (appointment != null) {
                Result.Success(appointment)
            } else {
                Result.Error("Appointment not found")
            }
        }catch (e : Exception){
            Result.Error(e.message ?: "Failed to fetch appointments")
        }
    }

    suspend fun getDoctorDetail(doctorId : String): Result<Doctor>{
        return try {
            val doctorSnapshot  = storeDb.collection("doctors")
                .document(doctorId)
                .get().await()

            val doctor  =  doctorSnapshot.toObject(Doctor::class.java)
            if (doctor == null) Result.Error("Doctor not found")
            else Result.Success(doctor)
        }catch (e: Exception){
            Result.Error(e.message ?: "Failed to fetch doctor detail")
        }
    }

    suspend fun markCompleteAppointment(doctorId: String ,dateId: String , apptId: String): Result<String>{
        return try {
            val batch  =  storeDb.batch()

            val patientRef = storeDb.collection("patients")
                .document(AppPreferences.getString(AppConstant.UID))
                .collection("appointments")
                .document(apptId)

            val doctorRef = storeDb.collection("doctors")
                .document(doctorId)
                .collection("bookings")
                .document(dateId)
                .collection("appointments")
                .document(apptId)

            batch.update(patientRef, "status", AppointmentStatus.COMPLETED.name)
            batch.update(doctorRef, "status", AppointmentStatus.COMPLETED.name)

            batch.commit()

            Result.Success("Appointment Completed")
        }catch (e: Exception){
            Result.Error(e.message ?: "Failed to fetch doctor detail")
        }
    }

}