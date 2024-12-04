package com.example.whatsub.ui.search

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.TestLooperManager
import android.util.Log
import android.view.Gravity
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
import com.example.whatsub.model.CheapestPath
import com.example.whatsub.model.TransferPath
import com.example.whatsub.model.PathData
import com.example.whatsub.model.ShortestPath
import com.example.whatsub.model.Transfer
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

    private fun getLineBackground(lineNumber: Int): Int {
        return when (lineNumber) {
            1 -> Color.parseColor("#00af50")
            2 -> Color.parseColor("#002060")
            3 -> Color.parseColor("#973b38")
            4 -> Color.parseColor("#ff0000")
            5 -> Color.parseColor("#4a7ebc")
            6 -> Color.parseColor("#ffc00c")
            7 -> Color.parseColor("#94d055")
            8 -> Color.parseColor("#00aff0")
            9 -> Color.parseColor("#70309f")
            else -> Color.GRAY
        }
    }

    // 경로 View 생성 함수
    private fun createRouteView(path: TransferPath, label: String): View {
        // 최상위 컨테이너
        val routeView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                115.dp
            ).apply {
                setMargins(16.dp, 8.dp, 16.dp, 8.dp)
            }
            setBackgroundColor(Color.parseColor("#ffffff"))
        }

        // 라벨 (최소 시간/최소 비용/최소 환승)
        val labelTextView = TextView(requireContext()).apply {
            text = label
            textSize = 12f
            setPadding(8.dp, 4.dp, 8.dp, 4.dp)
            setTypeface(null, Typeface.BOLD)
        }
        routeView.addView(labelTextView)
// 총 시간, 총 비용, 즐겨찾기 버튼을 포함할 컨테이너
        val infoContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL // 수직 중앙 정렬
        }

        // 총 시간 텍스트
        val totalTimeTextView = TextView(requireContext()).apply {
            text = "${path.totalTime}"
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                0, // 남은 공간 균등 배분
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // 가중치
            )
            setPadding(8.dp, 4.dp, 0, 4.dp)
            setTypeface(null, Typeface.BOLD) // 폰트 두껍게 설정
        }
        infoContainer.addView(totalTimeTextView)

        // 총 비용 텍스트
        val totalCostTextView = TextView(requireContext()).apply {
            text = "${path.totalCost}"
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                0, // 남은 공간 균등 배분
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // 가중치
            )
            setPadding(0, 4.dp, 8.dp, 4.dp)
        }
        infoContainer.addView(totalCostTextView)

        // 즐겨찾기 버튼
        val favoriteButton = ImageButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                40.dp,
                40.dp
            ).apply {
                setMargins(8.dp, 0, 8.dp, 0)
            }
            setImageResource(R.drawable.icon_favorites_blank)
            scaleType = ImageView.ScaleType.FIT_CENTER // 버튼 안에서 축소 및 중앙 배치
            setBackgroundColor(Color.TRANSPARENT) // 배경 투명

            var isFavorite = false // 즐겨찾기 여부 상태 변수

            setOnClickListener {
                isFavorite = !isFavorite // 상태 반전

                if (isFavorite) {
                    setImageResource(R.drawable.icon_favorites_fill) // 아이콘 변경
                    saveToFavorites(path) // 즐겨찾기 저장
                    Toast.makeText(context, "즐겨찾기에 추가되었습니다!", Toast.LENGTH_SHORT).show()
                } else {
                    setImageResource(R.drawable.icon_favorites_blank) // 아이콘 변경
                    removeFromFavorites(path) // 즐겨찾기 삭제
                    Toast.makeText(context, "즐겨찾기가 취소되었습니다!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        infoContainer.addView(favoriteButton)

        // infoContainer를 routeView에 추가
        routeView.addView(infoContainer)


        // 경로 표시 영역
        val routeContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8.dp, 0, 8.dp) // 상하 여백
            }
        }

        fun String.extractMinutes(): Int {
            val timeParts = this.split("시간", "분", "초").mapNotNull { it.trim().toIntOrNull() }
            val hours = if (timeParts.size > 1) timeParts[0] else 0
            val minutes = timeParts.getOrElse(timeParts.size - 1) { 0 }
            return hours * 60 + minutes
        }

// 총 시간의 분 단위로 비율 계산
        val totalMinutes = path.segments.sumOf { it.timeOnLine.extractMinutes() }
        path.segments.forEachIndexed { index, segment ->
            // 각 구간의 비율
            val widthRatio = segment.timeOnLine.extractMinutes().toFloat() / totalMinutes

            // 각 구간 뷰
            val segmentView = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, // 비율에 따라 동적으로 설정
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    widthRatio // 가중치로 비율 반영
                ).apply {
                    setMargins(0, 0, 0, 0)
                }
            }

            // 호선 정보
            val lineTextView = TextView(requireContext()).apply {
                text = "${segment.lineNumber}호선"
                textSize = 8f
                setBackgroundColor(getLineBackground(segment.lineNumber)) // 호선별 배경
                setPadding(4.dp, 2.dp, 4.dp, 2.dp)
                gravity = Gravity.CENTER
            }
            segmentView.addView(lineTextView)

            // 소요 시간 정보
            val timeTextView = TextView(requireContext()).apply {
                text = segment.timeOnLine
                textSize = 10f
                gravity = Gravity.CENTER
            }
            segmentView.addView(timeTextView)

            routeContainer.addView(segmentView)

            // 환승 아이콘 추가
            if (index < path.segments.size - 1) {
                val transferIcon = ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        30.dp,
                        30.dp
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        setMargins(0, 0, 0, 4.dp)
                    }
                    setImageResource(R.drawable.icon_transfer) // 환승 아이콘
                    setBackgroundColor(Color.TRANSPARENT) // 배경 투명
                    scaleType = ImageView.ScaleType.FIT_CENTER // 버튼 안에서 축소 및 중앙 배치

                }
                routeContainer.addView(transferIcon)
            }
        }
        routeView.addView(routeContainer)

        return routeView
    }


    // 즐겨찾기 삭제 함수
    private fun removeFromFavorites(route: TransferPath) {
        val sharedPref = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()

        // 경로를 JSON으로 변환하여 저장된 항목에서 제거
        val routeJson = Gson().toJson(route)
        if (favorites.contains(routeJson)) {
            favorites.remove(routeJson)
            sharedPref.edit().putStringSet("routes", favorites).apply()
        }
    }

    private fun String.getMinutesRatio(segments: List<Transfer>): Float {
        val totalMinutes = segments.sumOf { it.timeOnLine.extractMinutes() }
        return this.extractMinutes() / totalMinutes.toFloat()
    }

    private fun String.extractMinutes(): Int {
        val minutes = this.split("분")[0].toIntOrNull() ?: 0
        return minutes
    }

    // 즐겨찾기 저장 함수
    private fun saveToFavorites(route: TransferPath) {
        val sharedPref = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()
        favorites.add(Gson().toJson(route))
        sharedPref.edit().putStringSet("routes", favorites).apply()
        Toast.makeText(context, "즐겨찾기에 추가되었습니다!", Toast.LENGTH_SHORT).show()
    }

    // dp 변환 확장 함수
    private val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    // createRouteView 오버로드: ShortestPath
    private fun createRouteView(path: ShortestPath, label: String): View {
        return createRouteView(
            TransferPath(
                totalTime = path.totalTime,
                totalCost = path.totalCost,
                segments = path.transfers
            ),
            label
        )
    }

    // createRouteView 오버로드: CheapestPath
    private fun createRouteView(path: CheapestPath, label: String): View {
        return createRouteView(
            TransferPath(
                totalTime = path.totalTime,
                totalCost = path.totalCost,
                segments = path.transfers
            ),
            label
        )
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
        val routeContainer: LinearLayout = view.findViewById(R.id.routeContainer)


        // EditText 기본값 설정
        startInput.setText(startLocation)
        destinationInput.setText(destinationLocation)

        // JSON 데이터 불러오기

        val pathData = loadPathDataFromJson()

        // 경로 데이터를 동적으로 추가
        fun displayRoutes(pathData: PathData) {
            routeContainer.removeAllViews() // 기존 View 초기화

            pathData.shortestPath?.let { shortestPath ->
                routeContainer.addView(createRouteView(shortestPath, label = "최소 시간"))
            }

            pathData.cheapestPath?.let { cheapestPath ->
                routeContainer.addView(createRouteView(cheapestPath, label = "최소 비용"))
            }

            pathData.leastTransfersPath?.paths?.forEachIndexed { index, transferPath ->
                routeContainer.addView(
                    createRouteView(transferPath, label = "최소 환승 (${index + 1})")
                )
            }
        }
        Log.d("displayRoutes", "ShortestPath: ${pathData.shortestPath}")
        Log.d("displayRoutes", "CheapestPath: ${pathData.cheapestPath}")
        Log.d("displayRoutes", "LeastTransfersPath: ${pathData.leastTransfersPath?.paths}")


        // 초기 데이터 표시
        displayRoutes(pathData)

        // 초기 데이터 표시
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

        }

    }
}