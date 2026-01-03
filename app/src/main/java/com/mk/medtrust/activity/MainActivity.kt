package com.mk.medtrust.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mk.medtrust.R
import com.mk.medtrust.auth.LoginActivity
import com.mk.medtrust.databinding.ActivityMainBinding
import com.mk.medtrust.doctor.ui.DoctorActivity
import com.mk.medtrust.patient.ui.PatientActivity
import com.mk.medtrust.util.UtilObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var store: FirebaseFirestore
    private lateinit var binding : ActivityMainBinding
    private var dots  = listOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetsControllerCompat(window , window.decorView).isAppearanceLightNavigationBars = true
        WindowInsetsControllerCompat(window , window.decorView).isAppearanceLightStatusBars = true

        val currentUser = auth.currentUser

         dots  = listOf<View>()
        with(binding.progressAnim){
            dots = listOf(dot1,dot2,dot3)
        }

        showLoader(dots)

        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            redirectUser()
        }
    }

    private fun redirectUser() {
        val uid = auth.currentUser?.uid!!

        store.collection("users").document(uid)
            .get()
            .addOnSuccessListener { snap ->
                val role = snap.getString("role")

                when (role) {
                    "Patient" -> startActivity(Intent(this, PatientActivity::class.java))
                    "Doctor"  -> startActivity(Intent(this, DoctorActivity::class.java))
                    else -> startActivity(Intent(this, LoginActivity::class.java))
                }

                hideLoader(dots)
                finish()
            }
    }

    private fun showLoader(dots: List<View>){
        binding.progressAnim.mainProgress.visibility = View.VISIBLE

        UtilObject.startDotsAnimation(dots)

    }

    private fun hideLoader(dots1: List<View>) {
        UtilObject.stopDotsAnimation(dots1)
        binding.progressAnim.mainProgress.visibility = View.INVISIBLE
    }
}