package com.example.whatsub.ui.search

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.whatsub.R

class SearchFragment : Fragment (R.layout.fragment_search){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // arguments로 전달받은 데이터 가져오기
        val startLocation = arguments?.getString("startLocation") ?: "출발지가 없습니다"
        val destinationLocation = arguments?.getString("destinationLocation") ?: "도착지가 없습니다"

        // 가져온 데이터를 TextView에 표시
        val textView: TextView? = view.findViewById(R.id.text_search)
        if (textView == null) {
            Log.e("SearchFragment", "TextView not found!")
            return
        }
        textView.text = "출발지: $startLocation, 도착지: $destinationLocation"
    }
}