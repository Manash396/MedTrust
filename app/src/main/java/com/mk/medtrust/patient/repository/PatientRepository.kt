package com.mk.medtrust.patient.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.patient.model.toMap
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

    private var _updateState  = MutableLiveData<Result<String>>()
    val updateState : LiveData<Result<String>>  = _updateState

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
}