package com.mk.medtrust.auth.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mk.medtrust.auth.data.model.AppUser
import com.mk.medtrust.doctor.model.Doctor
import com.mk.medtrust.patient.model.Patient
import com.mk.medtrust.util.AppConstant
import com.mk.medtrust.util.Result
import com.yourpackage.app.AppPreferences
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val storeDb: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun registerDoctor(doctor: Doctor, key: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(doctor.email, key).await()
            val uid = result.user?.uid ?: return Result.Error("User ID not generated")

            doctor.uid = uid

            val appUser = AppUser(
                userId = uid,
                role = "Doctor",
                email = doctor.email,
                name = doctor.name,
                mobile = doctor.contact,
                specialisation = doctor.specialisation
            )

            AppPreferences.setString("role",appUser.role )
            AppPreferences.setString("userName",appUser.name )
            AppPreferences.setString("mobile", appUser.mobile)
            AppPreferences.setString("spec",appUser.specialisation)
            AppPreferences.setString("email",appUser.email)
            AppPreferences.setString(AppConstant.UID,appUser.userId)

            // Save user first
            if (!saveAppUser(appUser)) {
                return Result.Error("Failed saving user data")
            }

            // Save doctor details
            if (!saveDoctor(doctor)) {
                return Result.Error("Failed saving doctor details")
            }

            return Result.Success("Successful")

        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }


    suspend fun saveDoctor(doctor: Doctor): Boolean {
        return try {
            storeDb.collection("doctors")
                .document(doctor.uid)
                .set(doctor)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveAppUser(user: AppUser): Boolean {
        return try {
            storeDb.collection("users")
                .document(user.userId)
                .set(user)
                .await()
            true   // success
        } catch (e: Exception) {
            false  // failed
        }
    }


    suspend fun registerPatient(patient: Patient, key: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(patient.email, key).await()
            val uid = result.user?.uid ?: return Result.Error("User Id now generated")

            patient.uid = uid
            val appUser = AppUser(uid, "Patient", email = patient.email, name = patient.name,
                dob = patient.dob, mobile = patient.mobile , gender = patient.gender
            )

            AppPreferences.setString("role",appUser.role )
            AppPreferences.setString("userName",appUser.name )
            AppPreferences.setString("email",appUser.email )
            AppPreferences.setString("dob",appUser.dob)
            AppPreferences.setString("gender",appUser.gender)
            AppPreferences.setString("mobile",appUser.mobile)

            AppPreferences.setString(AppConstant.UID,appUser.userId)

            if (!saveAppUser(appUser)) return Result.Error("Failed saving user")
            if (!savePatient(patient)) return Result.Error("Failed saving patient")

            Result.Success("Successful")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun savePatient(patient: Patient): Boolean {
        return try {
            storeDb.collection("patients")
                .document(patient.uid)
                .set(patient)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }


    suspend fun login(email: String, key: String, isDoctor: Boolean): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, key).await()
            val userId = authResult.user?.uid ?: return Result.Error("User id not found")

            val snapshot = storeDb.collection("users").document(userId).get().await()

            if (snapshot.exists()) {
                val appUser = snapshot.toObject(AppUser::class.java)
                Log.d("Krishna ", appUser.toString())
                return if (isDoctor) { // want to enter as a doctor
                    if (appUser?.role == "Patient") Result.Error("User id not found")
                    else {
                        AppPreferences.setString("role",appUser?.role ?: "")
                        AppPreferences.setString("userName",appUser?.name ?: "")
                        AppPreferences.setString("mobile", appUser?.mobile ?: "")
                        AppPreferences.setString("spec",appUser?.specialisation ?: "")
                        AppPreferences.setString("email",appUser?.email ?: "")

                        AppPreferences.setString(AppConstant.UID,appUser?.userId ?: "")

                        Result.Success("Successful")
                    }
                } else {
                    if (appUser?.role == "Patient") {
                        AppPreferences.setString("role",appUser.role )
                        AppPreferences.setString("userName",appUser.name )
                        AppPreferences.setString("email",appUser.email )
                        AppPreferences.setString("dob",appUser.dob)
                        AppPreferences.setString("gender",appUser.gender)
                        AppPreferences.setString("mobile",appUser.mobile)

                        AppPreferences.setString(AppConstant.UID,appUser.userId)
                        Result.Success("Successful")
                    }
                    else Result.Error("User id not found")
                }
            } else {
                Result.Error("User data not found in Firestore")
            }


        } catch (e: Exception) {
            Result.Error(e.message ?: "Login failed")
        }
    }


}