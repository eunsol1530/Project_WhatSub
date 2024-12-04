package com.example.whatsub.ui.search

import android.graphics.Rect
import android.os.Bundle
import android.os.TestLooperManager
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsub.R
import com.example.whatsub.model.TransferPath
import com.example.whatsub.model.PathData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class SearchFragment : Fragment (R.layout.fragment_search) {

    private fun loadPathDataFromJson(): PathData {
        return try {
            val jsonString = requireContext().resources.openRawResource(R.raw.example_path_data)
                .bufferedReader().use { it.readText() }

            val gson = Gson()
            gson.fromJson(jsonString, PathData::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
            PathData(null, null, null) // 기본값으로 빈 PathData 반환
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
        // UI 요소 초기화
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

        val pathData = loadPathDataFromJson()
        Log.d("SearchFragment", "Parsed JSON: $pathDataTextViews")

        // 데이터 바인딩
        // 최소 시간 데이터 처리
        val shortestPath = pathData.shortestPath
        if (shortestPath != null) {
            pathDataTextViews[0].text =
                "최소 시간: ${shortestPath.totalTime}, 비용: ${shortestPath.totalCost}"
        } else {
            pathDataTextViews[0].text = "최소 시간 데이터 없음"
        }

        // 최소 비용 데이터 처리
        val cheapestPath = pathData.cheapestPath
        if (cheapestPath != null) {
            pathDataTextViews[1].text =
                "최저 비용: ${cheapestPath.totalTime}, 비용: ${cheapestPath.totalCost}"
        } else {
            pathDataTextViews[1].text = "최저 비용 데이터 없음"
        }

        // 최소 환승 경로 데이터 처리
        val leastTransfersPaths = pathData.leastTransfersPath?.paths
        if (!leastTransfersPaths.isNullOrEmpty()) {
            val transferInfo = leastTransfersPaths.joinToString(separator = "\n") { path ->
                val time = path.totalTime ?: "시간 정보 없음"
                val cost = path.totalCost ?: "비용 정보 없음"
                "시간: $time, 비용: $cost"
            }
            val totalTransfers = pathData.leastTransfersPath?.totalTransfers ?: "정보 없음"
            pathDataTextViews[2].text = "최소 환승: ${totalTransfers}번 환승\n$transferInfo"
        } else {
            pathDataTextViews[2].text = "최소 환승 데이터 없음"
        }


        // 데이터를 업데이트하는 함수
        fun updatePathData(start: String, destination: String) {
            val pathData = loadPathDataFromJson()

            // 매칭된 경로 데이터를 순서대로 저장
            val matchedPaths = mutableListOf<String>()

            // shortestPath 확인
            pathData.shortestPath?.let { shortestPath ->
                if (shortestPath.startStation.toString() == start && shortestPath.endStation.toString() == destination) {
                    matchedPaths.add(
                        """
                출발: ${shortestPath.startStation}
                도착: ${shortestPath.endStation}
                총 시간: ${shortestPath.totalTime}
                총 비용: ${shortestPath.totalCost}
                """.trimIndent()
                    )
                }
            }

            // cheapestPath 확인
            pathData.cheapestPath?.let { cheapestPath ->
                if (cheapestPath.startStation.toString() == start && cheapestPath.endStation.toString() == destination) {
                    matchedPaths.add(
                        """
                출발: ${cheapestPath.startStation}
                도착: ${cheapestPath.endStation}
                총 시간: ${cheapestPath.totalTime}
                총 비용: ${cheapestPath.totalCost}
                """.trimIndent()
                    )
                }
            }

            // leastTransfersPath 확인
            pathData.leastTransfersPath?.paths?.forEach { transferPath ->
                val fromStation = transferPath.segments.firstOrNull()?.fromStation?.toString()
                val toStation = transferPath.segments.lastOrNull()?.toStation?.toString()

                if (fromStation == start && toStation == destination) {
                    matchedPaths.add(
                        """
                출발: $fromStation
                도착: $toStation
                총 시간: ${transferPath.totalTime ?: "정보 없음"}
                총 비용: ${transferPath.totalCost ?: "정보 없음"}
                """.trimIndent()
                    )
                }
            }

            // 고정된 TextView에 데이터 매핑
            pathDataTextViews.forEachIndexed { index, textView ->
                if (index < matchedPaths.size) {
                    textView.visibility = View.VISIBLE
                    textView.text = matchedPaths[index]
                } else {
                    textView.visibility = View.GONE // 남는 TextView 숨기기
                }
            }

            // 매칭된 데이터가 없을 경우 처리
            if (matchedPaths.isEmpty()) {
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