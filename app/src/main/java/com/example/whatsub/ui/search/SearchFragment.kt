package com.example.whatsub.ui.search

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsub.R
import com.example.whatsub.model.PathData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class SearchFragment : Fragment (R.layout.fragment_search) {

    private fun loadPathDataFromJson(): List<PathData> {
        return try {
            val jsonString = requireContext().resources.openRawResource(R.raw.example_path_data)
                .bufferedReader().use { it.readText() }

            val gson = Gson()
            val listType = object : TypeToken<List<PathData>>() {}.type
            gson.fromJson(jsonString, listType)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // arguments로 전달받은 데이터 가져오기
        val startLocation = arguments?.getString("startLocation") ?: "출발지가 없습니다"
        val destinationLocation = arguments?.getString("destinationLocation") ?: "도착지가 없습니다"

        // Log 추가
        Log.d("SearchFragment", "Received arguments: startLocation=$startLocation, destinationLocation=$destinationLocation")

        if (startLocation.isEmpty() || destinationLocation.isEmpty()) {
            Toast.makeText(context, "잘못된 데이터입니다. 홈 화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack() // 이전 화면으로 이동
            return
        }

        // UI 요소 초기화 및 데이터 처리
        val startInput: EditText = view.findViewById(R.id.start_location)
        val destinationInput: EditText = view.findViewById(R.id.destination_location)
        val pathDataTextViews = listOf(
            view.findViewById<TextView>(R.id.path_item1),
            view.findViewById<TextView>(R.id.path_item2),
            view.findViewById<TextView>(R.id.path_item3),
            view.findViewById<TextView>(R.id.path_item4)
        )
        // EditText 기본값 설정
        startInput.setText(startLocation)
        destinationInput.setText(destinationLocation)

        // JSON 데이터 불러오기

        val pathDataList = loadPathDataFromJson()
        Log.d("SearchFragment", "Parsed JSON: $pathDataTextViews")

        // 데이터를 업데이트하는 함수
        fun updatePathData(start: String, destination: String) {
            val matchedData = pathDataList.filter {
                it.startStation.toString() == start && it.endStation.toString() == destination
            }

            if (matchedData.isNotEmpty()) {
                matchedData.forEachIndexed { index, pathData ->
                    if (index < pathDataTextViews.size) {
                        val textView = pathDataTextViews[index]
                        textView.visibility = View.VISIBLE
                        textView.text = """
                            출발: ${pathData.startStation}
                            도착: ${pathData.endStation}
                            총 시간: ${pathData.totalTime}
                            총 비용: ${pathData.totalCost}
                        """.trimIndent()
                    }
                }
                pathDataTextViews.drop(matchedData.size).forEach { it.visibility = View.GONE }
            } else {
                Toast.makeText(requireContext(), "해당 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                pathDataTextViews.forEach { it.visibility = View.GONE }
            }
        }

        // 초기 데이터 표시
        updatePathData(startLocation, destinationLocation)

        val researchBtn: ImageButton = view.findViewById(R.id.research_btn)
        val exchangeButton: ImageButton = view.findViewById(R.id.btn_exchange)

        // 교환 버튼 클릭 이벤트
        exchangeButton.setOnClickListener {
            Log.d("SearchFragment", "Exchange Button Clicked!")

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

        // 재탐색 버튼 클릭 이벤트
        researchBtn.setOnClickListener {
            val newStart = startInput.text.toString()
            val newDestination = destinationInput.text.toString()

            if (newStart.isBlank() || newDestination.isBlank()) {
                Toast.makeText(requireContext(), "출발지와 도착지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newStart == newDestination) {
                Toast.makeText(context, "출발지와 도착지가 동일합니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            updatePathData(newStart, newDestination)
        }

    }
}