package com.mk.medtrust.util

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object UtilObject {

    fun startDotsAnimation(dots: List<View>) {
        val animators = dots.mapIndexed { index, dot ->
            ObjectAnimator.ofFloat(dot, "translationY", 0f, -20f, 0f).apply {
                duration = 600
                startDelay = index * 200L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
            }
        }

        animators.forEach { it.start() }
    }

    fun stopDotsAnimation(dots: List<View>) {
        dots.forEach { it.animate().cancel() }
    }

    fun TextInputLayout.clearErrorOnType() {
        this.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                this@clearErrorOnType.error = null
                this@clearErrorOnType.isErrorEnabled = false
            }
        })
    }

    fun timeToMinutes(time1: String): Int {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = sdf.parse(time1) ?: return 0

        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

}