package com.mk.medtrust.patient.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import com.mk.medtrust.R
import com.mk.medtrust.databinding.FragmentDiseasePredictionBinding
import com.mk.medtrust.util.UtilObject.DISEASE_P_WEB_URL


class DiseasePredictionFragment : Fragment() {

    private var _binding  : FragmentDiseasePredictionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiseasePredictionBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        setUpWebView()

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setUpWebView(){
        val webView = binding.webView

        // js required dynamic website
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
        }

        // restriction navigation and does not allow unknown url to be clicked from the webview
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                return !url.startsWith("https://yourdomain.com")
            }
        }
        webView.loadUrl(DISEASE_P_WEB_URL)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}