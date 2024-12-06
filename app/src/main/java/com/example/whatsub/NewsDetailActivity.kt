package com.example.whatsub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsub.databinding.ActivityNewsDetailBinding

class NewsDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뉴스 제목 설정
        val newsTitle = intent.getStringExtra("newsTitle")
        // newsTitle 값이 null이면 로그로 확인
        Log.d("NewsDetailActivity", "newsTitle: $newsTitle")
        binding.newsTitle.text = newsTitle

        // WebView로 기사 URL 열기
        val newsUrl = intent.getStringExtra("newsUrl")
        // newsUrl 값이 null이면 로그로 확인
        Log.d("NewsDetailActivity", "newsUrl: $newsUrl")

        // URL이 null이 아니면 WebView로 로드
        if (newsUrl != null) {
            binding.webView.apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        view?.loadUrl(request?.url.toString())
                        return true
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: android.webkit.WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        Log.e("NewsDetailActivity", "WebView error: ${error?.description}")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("NewsDetailActivity", "Page finished loading: $url")
                    }
                }
                loadUrl(newsUrl)
            }
        }
    }
}