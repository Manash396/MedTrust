package com.mk.medtrust.patient.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mk.medtrust.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_patient)

        WindowInsetsControllerCompat(window , window.decorView).isAppearanceLightNavigationBars = false
        WindowInsetsControllerCompat(window , window.decorView).isAppearanceLightStatusBars = false
    }
}