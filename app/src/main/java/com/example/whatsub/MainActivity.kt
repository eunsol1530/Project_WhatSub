package com.example.whatsub

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.whatsub.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // 네비게이션 버튼 클릭 이벤트 설정
        setupNavigationButtons(navController)
    }

    private fun setupNavigationButtons(navController: androidx.navigation.NavController) {
        findViewById<ImageButton>(R.id.nav_icon_home_ib).setOnClickListener {
            navController.navigate(R.id.navigation_home)
        }
        findViewById<ImageButton>(R.id.nav_icon_favorites_ib).setOnClickListener {
            navController.navigate(R.id.navigation_favorites)
        }
        findViewById<ImageButton>(R.id.nav_icon_game_ib).setOnClickListener {
            navController.navigate(R.id.navigation_game)
        }
        findViewById<ImageButton>(R.id.nav_icon_news_ib).setOnClickListener {
            navController.navigate(R.id.navigation_news)
        }
    }

    private fun stopWebView() {
        // WebView 리소스 해제는 GameFragment에서 관리
    }
}
