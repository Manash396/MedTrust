package com.mk.medtrust.doctor.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mk.medtrust.auth.data.model.Appointment
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.doctor.model.toMap
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.util.Result
import com.yourpackage.app.AppPreferences
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorRepository @Inject constructor(
    private val storeDB: FirebaseFirestore,
    private val auth: FirebaseAuth
) {


    suspend fun updateDoctorProfDetails(doctor: Doctor): Result<String>{
        return try {
            val uid = auth.currentUser?.uid ?: return Result.Error("User not logged in")

            val updatedMap = doctor.toMap()

            storeDB.collection("doctors").document(uid)
                .update(updatedMap).await()

            Result.Success("Updated Successfully")
        }catch (e: Exception){
            Result.Error(e.message ?: "Update Failed")
        }
    }

    suspend fun getDoctorDetails(uid: String): Result<Doctor> {
        return try {
            val snapShot = storeDB.collection("doctors").document(uid).get().await()
            if (snapShot.exists()){
                val doctor  = snapShot.toObject(Doctor::class.java) ?: Doctor()
                Result.Success(doctor)
            } else {
                Result.Error("Failed to get Doctor Details")
            }

        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to Get Doctor Details")
        }
    }

    suspend fun updateDoctorAvailability(
        availability: Doctor.Availability
    ): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.Error("User not logged in")

            storeDB.collection("doctors")
                .document(uid)
                .update("availability", availability)
                .await()

            Result.Success("Availability updated successfully")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update availability")
        }
    }

    suspend fun getAllAppointments(doctorId : String) : Result<List<Appointment>> {
        return try {
            val allAppointments  = mutableListOf<Appointment>()

            val dateSnapshot = storeDB.collection("doctors")
                .document(doctorId)
                .collection("bookings")
                .get()
                .await()

            Log.d("Krishna",dateSnapshot.toString())

            for(dateDoc in dateSnapshot.documents){

                val appointmentSnapshot = storeDB.collection("doctors")
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

    suspend fun getAppointmentDetail(appt : Appointment) : Result<Appointment>{
        return try {
            val apptSnapshot  = storeDB.collection("doctors")
                .document(appt.doctorId)
                .collection("bookings")
                .document(appt.dateId)
                .collection("appointments")
                .document(appt.appointmentId)
                .get()
                .await()
            val appointment = apptSnapshot.toObject(Appointment::class.java)
            if (appointment != null) {
                Result.Success(appointment)
            }else{
                Result.Error("No data found")
            }

        }catch (e: Exception){
            Result.Error(e.message ?: "Failed to fetch Appointment Detail")
        }
    }


    suspend fun getPatientDetail(patientId: String) : Result<Patient>{
        return try {
            val apptSnapshot  = storeDB.collection("patients")
                .document(patientId)
                .get()
                .await()

            val patient = apptSnapshot.toObject(Patient::class.java)

            if (patient != null) {
                Result.Success(patient)
            }else{
                Result.Error("No data found")
            }

        }catch (e: Exception){
            Result.Error(e.message ?: "Failed to fetch Patient Detail")
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

}