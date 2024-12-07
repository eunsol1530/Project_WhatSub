package com.example.whatsub.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.whatsub.R

class GameFragment : Fragment() {

    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        // WebView 초기화
        webView = view.findViewById(R.id.webView)
        webView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            loadUrl("https://pinball.flutter.dev/#/")
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // WebView 리소스 해제
        webView?.apply {
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
        webView = null
    }
}
