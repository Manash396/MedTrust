package com.mk.medtrust.util

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

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
       return try{
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(time1) ?: return 0

            val cal = Calendar.getInstance().apply { time = date }
            return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }catch (e : Exception){
            0
        }
    }

/// Junit not possible required the android - runtime (called Integration testing )
    fun saveViewAsPdf(context : Context , view: View , fileName: String) : Uri {
        // measure & layout view
        // since i am using the inflated view not rendered one
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            Resources.getSystem().displayMetrics.widthPixels,
            View.MeasureSpec.EXACTLY
        )

        view.measure(
            widthSpec ,
            View.MeasureSpec.makeMeasureSpec(0 , View.MeasureSpec.UNSPECIFIED)
        )

        view.layout(0,0,view.measuredWidth , view.measuredHeight)

        // a white paper
        val bitMap  = createBitmap(view.measuredWidth, view.measuredHeight)

        val canvas = Canvas(bitMap)
        view.draw(canvas)

        // A4 size at 72 dpi
        val pageWidth = 595
        val pageHeight = 842

        val scaledBitmap =
            bitMap.scale(pageWidth, ((bitMap.height * pageWidth.toFloat()) / bitMap.width).toInt())

        val pdf  = PdfDocument()
        var yOffset = 0
        var pageNumber  = 1

        while (yOffset < scaledBitmap.height){
            val pageInfo  = PdfDocument.PageInfo.Builder(
                pageWidth,pageHeight,pageNumber++
            ).create()

            val page = pdf.startPage(pageInfo)

            val src  = Rect(
                0, yOffset , pageWidth , minOf(yOffset+pageHeight, scaledBitmap.height)
            )

            val dst  = Rect(0,0,pageWidth , src.height())

            page.canvas.drawBitmap(scaledBitmap, src, dst, null)
            pdf.finishPage(page)

            yOffset+=pageHeight
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePdfViaMediaStore(context, pdf, fileName)
        } else {
            savePdfViaFileSystem(context, pdf, fileName)
        }

        Log.d("KrishnaMk",uri.toString())
        pdf.close() // what ever buffer is used will be released eg. Bitmap , painter etc
        return uri
    }

    // API 29+
    private fun savePdfViaMediaStore(
        context: Context,
        pdfDocument: PdfDocument,
        fileName: String
    ): Uri {
        // relative path not actual path is used android will decide which path to give
        val values = ContentValues().apply {  // like a column and row
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            values
        ) ?: throw IOException("MediaStore failed")

        context.contentResolver.openOutputStream(uri)?.use {
            pdfDocument.writeTo(it)
        }

        return uri
    }

//    File system (API ≤28)
    private fun savePdfViaFileSystem(
        context: Context,
        pdfDocument: PdfDocument,
        fileName: String
    ): Uri {
        val downloads =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        if (!downloads.exists()) downloads.mkdirs()

        val file = File(downloads, "$fileName.pdf")

        FileOutputStream(file).use {
            pdfDocument.writeTo(it)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }


}