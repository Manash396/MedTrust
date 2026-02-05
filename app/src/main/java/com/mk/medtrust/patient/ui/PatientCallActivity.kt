package com.mk.medtrust.patient.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mk.medtrust.R
import com.mk.medtrust.util.AppConstant
import com.yourpackage.app.AppPreferences
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

class PatientCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_call)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addCallFragment()
    }

    private fun addCallFragment() {
        val appID: Long = 129447961
        val appSign: String = "873e98c886d7afee9a49237acb2ffe43be4437e79a16739395506f2333501c5b"
        var callID: String = intent.getStringExtra("callId") ?: ""
        val userID: String = AppPreferences.getString(AppConstant.UID)
        val userName: String = AppPreferences.getString(AppConstant.USERNAME)

        Log.d("Krishna",callID)
        callID = callID.replace(":","")

        // You can also use GroupVideo/GroupVoice/OneOnOneVoice to make more types of calls.
        val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()

        config.leaveCallListener =  ZegoUIKitPrebuiltCallFragment.LeaveCallListener {
            val intent = Intent().apply {
                putExtra("Call_Ended", true)
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            appID, appSign, userID, userName, callID, config
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitNow()
    }
}