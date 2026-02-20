package com.mk.medtrust.util.utilobject

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.mk.medtrust.util.UtilObject
import org.junit.Test

class PDFSaverTest {

    @Test
    fun pdfSaverAndGetUriNotNull(){
        val context  = ApplicationProvider.getApplicationContext<Context>()
         val text  = TextView(context)
             .apply {
                 text = "Radhe Radhe"
             }
        val uri  = UtilObject.saveViewAsPdf(context, text ,"Test_pdf")
        assertThat(uri).isNotNull()
    }
}