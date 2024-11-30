package com.example.whatsub

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.whatsub.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: ConstraintLayout = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        // 네비게이션 버튼 클릭 이벤트 설정
        setupNavigationButtons(navController)
    }

    private fun setupNavigationButtons(navController: androidx.navigation.NavController) {
        // Home 버튼
        findViewById<ImageButton>(R.id.nav_icon_home_ib).setOnClickListener {
            navController.navigate(R.id.navigation_home)
        }

        // Favorites 버튼
        findViewById<ImageButton>(R.id.nav_icon_favorites_ib).setOnClickListener {
            navController.navigate(R.id.navigation_favorites)
        }

        // Game 버튼
        findViewById<ImageButton>(R.id.nav_icon_game_ib).setOnClickListener {
            navController.navigate(R.id.navigation_dashboard)
        }

        // News 버튼
        findViewById<ImageButton>(R.id.nav_icon_news_ib).setOnClickListener {
            navController.navigate(R.id.navigation_news)
        }
    }
}