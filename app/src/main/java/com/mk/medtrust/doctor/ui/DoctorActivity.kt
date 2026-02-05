package com.mk.medtrust.doctor.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mk.medtrust.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DoctorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor)

        WindowInsetsControllerCompat(window,window.decorView).isAppearanceLightNavigationBars = false
        WindowInsetsControllerCompat(window,window.decorView).isAppearanceLightStatusBars = false
    }


}