package com.mk.medtrust.application

import android.app.Application
import com.yourpackage.app.AppPreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
    }
}