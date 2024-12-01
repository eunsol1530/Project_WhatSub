package com.example.whatsub.ui.search

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.whatsub.R

class SearchFragment : Fragment (R.layout.fragment_search) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // arguments로 전달받은 데이터 가져오기
        val startLocation = arguments?.getString("startLocation") ?: "출발지가 없습니다"
        val destinationLocation = arguments?.getString("destinationLocation") ?: "도착지가 없습니다"

        // EditText 가져오기
        val startInput: EditText = view.findViewById(R.id.start_location)
        val destinationInput: EditText = view.findViewById(R.id.destination_location)

        // EditText에 기본값 설정
        startInput.setText(startLocation)
        destinationInput.setText(destinationLocation)

        // 교환 버튼 ImageButton
        val exchangeButton: ImageButton = view.findViewById(R.id.btn_exchange)

        // 버튼 클릭 이벤트
        exchangeButton.setOnClickListener {

            val startText = startInput.text.toString()
            val destinationText = destinationInput.text.toString()

            if (startText.isBlank() || destinationText.isBlank()) {
                Toast.makeText(context, "출발지와 도착지를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (startText == destinationText) {
                Toast.makeText(context, "출발지와 도착지가 동일합니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startInput.setText(destinationText)
            destinationInput.setText(startText)
        }

        // 최소 시간 텍스트와 드롭다운 아이콘
        val sortWayText: TextView = view.findViewById(R.id.sort_way)
        val sortWayDropdown: ImageButton = view.findViewById(R.id.min_time_dropdown)

        // 이전 페이지에서 전달된 기본 옵션
        val defaultOption = arguments?.getString("selectedOption") ?: "option_min_time"

        // 기본 옵션 설정
        when (defaultOption) {
            "option_min_time" -> sortWayText.text = "최소 시간"
            "option_min_cost" -> sortWayText.text = "최소 비용"
            "option_min_transfer" -> sortWayText.text = "최소 환승"
        }


        // 클릭 이벤트에 PopupMenu 연결
        val showPopupMenu: (View) -> Unit = { anchorView ->
            val popupMenu = PopupMenu(requireContext(), anchorView)
            popupMenu.menuInflater.inflate(R.menu.min_sort_menu, popupMenu.menu)

            // PopupMenu 항목 클릭 이벤트 처리
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.option_min_time -> {
                        sortWayText.text = "최소 시간"
                        true
                    }

                    R.id.option_min_cost -> {
                        sortWayText.text = "최소 비용"
                        true
                    }

                    R.id.option_min_transfer -> {
                        sortWayText.text = "최소 환승"
                        true
                    }

                    else -> false
                }
            }

            // PopupMenu 표시
            popupMenu.show()
        }

        // 텍스트와 화살표 아이콘 클릭 시 PopupMenu 표시
        sortWayText.setOnClickListener { showPopupMenu(it) }
        sortWayDropdown.setOnClickListener { showPopupMenu(it) }
    }
}