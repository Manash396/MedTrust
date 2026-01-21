package com.mk.medtrust.patient.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mk.medtrust.R
import com.mk.medtrust.util.PaymentConstants
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PaymentActivity : AppCompatActivity() , PaymentResultListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Checkout.preload(applicationContext)
        val co = Checkout()

        co.setKeyID("rzp_test_S5fQL2UvsSwF0N")
        val amt  = intent.getIntExtra(PaymentConstants.EXTRA_AMOUNT,0)
        initPaymentMethod(amt)

    }

    private fun initPaymentMethod(amount: Int) {
        val activity: Activity = this
        val co = Checkout()

        try {
            val options = JSONObject()
            options.put("name","MedTrust")
            options.put("description","Demoing Charges")
            //You can omit the image option to fetch the image from the Dashboard
            options.put("image","http://example.com/image/rzp.jpg")
            options.put("theme.color", "##03c69c");
            options.put("currency","INR");
            options.put("amount",amount)//pass amount in currency subunits

            val retryObj = JSONObject();
            retryObj.put("enabled", false);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            val prefill = JSONObject()
            prefill.put("email","manasash26@gmail.com")
            prefill.put("contact","+918133919030")

            options.put("prefill",prefill)
            co.open(activity,options)
        }catch (e: Exception){
            Toast.makeText(activity,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        setResult(PaymentConstants.RESULT_PAYMENT_SUCCESS)
        finish()
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        setResult(PaymentConstants.RESULT_PAYMENT_FAILED)
        Log.d("Krishna",p1.toString())
        finish()
    }
}