package com.example.whatsub.ui.search

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.whatsub.R
import com.example.whatsub.data.api.model.CheapestPath
import com.example.whatsub.data.api.model.TransferPath
import com.example.whatsub.data.api.model.PathData
import com.example.whatsub.data.api.model.ShortestPath
import com.example.whatsub.data.api.model.Transfer
import com.google.gson.Gson
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
            PathData(null, null, null, emptyList()) // 기본값으로 반환
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
                120.dp
            ).apply {
                setMargins(0, 8.dp, 0, 4.dp)
            }
            setBackgroundColor(Color.parseColor("#ffffff"))
        }

        // 구분 선 추가
        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.dp // 선의 두께
            ).apply {
                setMargins(0, 0, 0, 0)
            }
            setBackgroundColor(Color.LTGRAY) // 선의 색상
        }
        routeView.addView(divider)

        // 라벨과 즐겨찾기 버튼을 포함하는 컨테이너
        val headerContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL // 수직 중앙 정렬
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16.dp, 0, 10.dp, 0)
            }
        }

        // 라벨 (최소 시간/최소 비용/최소 환승)
        val labelTextView = TextView(requireContext()).apply {
            //text = label
            id = R.id.route_label // ID 설정
            text = createStyledLabel(label) // 수정된 함수로 스타일 적용
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1F
            )
        }
        headerContainer.addView(labelTextView)

        // 즐겨찾기 버튼
        val favoriteButton = ImageButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                40.dp,
                40.dp
            ).apply {
                setMargins(16.dp, 4.dp, 0, 0)
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
        headerContainer.addView(favoriteButton)

        routeView.addView(headerContainer)


// 총 시간, 총 비용을 포함할 컨테이너
        val infoContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 총 시간 텍스트
        val totalTimeTextView = TextView(requireContext()).apply {
            text = "${path.totalTime}"
            textSize = 16f
            setTextColor(Color.DKGRAY) // 텍스트 색상 설정
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM // 하단 정렬
            }
            setPadding(16.dp, 0, 8.dp, 8.dp)
            setTypeface(null, Typeface.BOLD) // 폰트 두껍게 설정
        }
        infoContainer.addView(totalTimeTextView)

        // 총 비용 텍스트
        val totalCostTextView = TextView(requireContext()).apply {
            text = "${path.totalCost}"
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM // 하단 정렬
            }
            setPadding(0, 4.dp, 8.dp, 8.dp)
        }
        infoContainer.addView(totalCostTextView)

        // infoContainer를 routeView에 추가
        routeView.addView(infoContainer)


        // 경로 표시 영역
        val routeItemContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 0) // 상하 여백
            }
        }

        fun String.extractMinutes(): Int {
            val regex = "(\\d+)시간".toRegex()
            val hours = regex.find(this)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            val minutesRegex = "(\\d+)분".toRegex()
            val minutes = minutesRegex.find(this)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            val secondsRegex = "(\\d+)초".toRegex()
            val seconds = secondsRegex.find(this)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            return (hours * 60) + minutes + (seconds / 60)
        }

        fun String.formatTimeWithoutSeconds(): String {
            val regex = "(\\d+시간)?\\s*(\\d+분)?".toRegex()
            val matchResult = regex.find(this)
            val hours = matchResult?.groupValues?.get(1)?.trim() ?: ""
            val minutes = matchResult?.groupValues?.get(2)?.trim() ?: ""

            return listOf(hours, minutes).filter { it.isNotEmpty() }.joinToString(" ")
        }


// 총 시간의 분 단위로 비율 계산
        val totalMinutes = path.segments.sumOf { it.timeOnLine.extractMinutes() }
        path.segments.forEachIndexed { index, segment ->
            // 출발 지점 추가
            if (index == 0) {
                val startStationView = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16.dp, 0, 0, 0) // 아이콘과 라인 간격 조정
                    }
                    setBackgroundColor(Color.WHITE) // 배경 설정
                    gravity = Gravity.CENTER
                }

                val startIcon = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.icon_transfer) // 환승 아이콘
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        31.dp,
                        31.dp
                    )
                }
                startStationView.addView(startIcon)

                val startStationText = TextView(requireContext()).apply {
                    text = "${segment.fromStation}"
                    textSize = 8f // 텍스트 크기
                    setTextColor(Color.BLACK) // 텍스트 색상
                    setTypeface(null, Typeface.BOLD) // 텍스트 두껍게
                    gravity = Gravity.CENTER // 중앙 정렬
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                startStationText.elevation = 10f // 우선순위 높임

                Log.d("StationDebug", "startStationText=${startStationText.text}")

                startStationView.addView(startStationText)

                routeItemContainer.addView(startStationView)
                Log.d("RouteContainer", "Child count: ${routeItemContainer.childCount}")

            }

            // 각 구간 비율 및 역 정보 추가
            val segmentView = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, // 비율에 따라 동적으로 설정
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    segment.timeOnLine.extractMinutes().toFloat() / totalMinutes // 가중치로 비율 반영
                ).apply {
                    setMargins(0, 8.dp, 0, 0)
                }
            }

            // 호선 정보
            val lineTextView = TextView(requireContext()).apply {
                text = "${segment.lineNumber}"
                textSize = 8f
                setTextColor(Color.WHITE)
                setBackgroundColor(getLineBackground(segment.lineNumber)) // 호선별 배경
                setPadding(4.dp, 0, 4.dp, 0)
                gravity = Gravity.CENTER
            }
            segmentView.addView(lineTextView)

           // 소요 시간 정보
            val timeTextView = TextView(requireContext()).apply {
                text = segment.timeOnLine.formatTimeWithoutSeconds() // 초 제거된 문자열 적용
                textSize = 10f
                gravity = Gravity.CENTER
            }
            segmentView.addView(timeTextView)

            routeItemContainer.addView(segmentView)

            // 환승 아이콘 및 역 이름 추가
            if (index < path.segments.size - 1) {
                val transferView = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        31.dp,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 0)
                    }
                    gravity = Gravity.CENTER
                }

                val transferIcon = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.icon_transfer) // 환승 아이콘
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        31.dp,
                        31.dp
                    )
                }
                transferView.addView(transferIcon)

                val transferText = TextView(requireContext()).apply {
                    text = if (segment.toStation != 0) "${segment.toStation}" else "환승역 미지정"
                    textSize = 8f
                    setTextColor(Color.BLACK) // 텍스트 색상 설정
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                }

                Log.d("StationDebug", "transferText=${transferText.text}")

                transferView.addView(transferText)

                routeItemContainer.addView(transferView)
            }

            // 도착 지점 추가
            if (index == path.segments.size - 1) {
                val endStationView = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        setMargins(0, 0, 16.dp, 0)
                    }
                    gravity = Gravity.CENTER
                }

                val endIcon = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.icon_transfer) // 환승 아이콘
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        31.dp,
                        31.dp
                    )
                }
                endStationView.addView(endIcon)

                val endStationText = TextView(requireContext()).apply {
                    text = if (segment.toStation != 0) "${segment.toStation}" else "도착역 미지정"
                    textSize = 8f
                    setTextColor(Color.BLACK) // 텍스트 색상 설정
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                }
                Log.d("StationDebug", "endStationText=${endStationText.text}")
                endStationView.addView(endStationText)

                routeItemContainer.addView(endStationView)

                routeView.addView(routeItemContainer)
            }
        }

        // 경로 데이터를 태그로 저장
        routeView.tag = path

        // 클릭 리스너 추가
        routeView.setOnClickListener {
            Log.d("RouteViewClick", "RouteView clicked with data: $path")
            val bundle = Bundle().apply {
                putString("routeType", label) // 라벨 정보 전달
                putSerializable("routeData", path) // TransferPath 전달
            }
            Log.d("NavigationDebug", "routeType: $label, routeData: $path")
            findNavController().navigate(R.id.action_searchFragment_to_detailFragment, bundle)
            Log.d("createRouteView", "Navigating to DetailFragment with routeData: $path")
        }

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

    private fun createStyledLabel(label: String): SpannableString {
        val spannable = SpannableString(label)
        val ampersandIndex = label.indexOf('&')

        if (ampersandIndex != -1) {
            // "최소 시간 & 최소 환승"에서 & 이후를 굵게 설정
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                ampersandIndex, label.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }

    private fun displayRoutes(pathData: PathData, routeContainer: LinearLayout) {
        routeContainer.removeAllViews() // 기존 View 초기화

        val comparisonResults = pathData.comparisonResults ?: return
        val renderedPaths = mutableMapOf<String, View>() // 고유 키와 View 저장
        val labelsMap = mutableMapOf<String, String>() // 고유 키와 라벨 매핑

        fun renderPath(path: TransferPath, label: String, updateLabelOnly: Boolean = false) {
            val uniqueKey = "${path.totalTime}-${path.totalCost}-${path.segments.joinToString { "${it.fromStation}-${it.toStation}-${it.lineNumber}" }}"

            if (renderedPaths.containsKey(uniqueKey)) {
                Log.d("FilterDebug", "Path already exists: $uniqueKey. Updating label.")

                val existingLabel = labelsMap[uniqueKey] ?: label
                val newLabelPart = label.split("&").last().trim()

                // **중복된 라벨 방지 로직**
                if (existingLabel.contains(newLabelPart)) {
                    Log.d("LabelDebug", "Skipping duplicate label update: $newLabelPart")
                    return
                }

                val updatedLabel = "$existingLabel & $newLabelPart"
                labelsMap[uniqueKey] = updatedLabel

                // 기존 뷰의 라벨 업데이트
                val existingView = renderedPaths[uniqueKey] ?: return
                val labelTextView = existingView.findViewById<TextView>(R.id.route_label)
                labelTextView?.text = updatedLabel
                Log.d("RenderDebug", "Updated label: $updatedLabel for path: $uniqueKey")

                return
            }

            if (!updateLabelOnly) {
                // 새로운 경로 추가
                val routeView = createRouteView(path, label).apply {
                    tag = uniqueKey
                }
                routeContainer.addView(routeView)
                renderedPaths[uniqueKey] = routeView
                labelsMap[uniqueKey] = label
                Log.d("RenderDebug", "Rendered path with label: $label for key: $uniqueKey")
            }
        }

        // 최소 시간 경로 추가
        pathData.shortestPath?.paths?.forEachIndexed { index, path ->
            val result = comparisonResults.getOrNull(index) ?: 0
            val label = when (result) {
                3 -> "최소 시간 & 최소 환승"
                4 -> "최소 시간 & 최소 비용 & 최소 환승"
                1 -> "최소 시간 & 최소 비용"
                else -> "최소 시간"
            }
            renderPath(path, label)
        }

        // 최소 비용 경로 추가
        pathData.cheapestPath?.paths?.forEachIndexed { index, path ->
            val result = comparisonResults.getOrNull(index) ?: 0
            val label = when (result) {
                2 -> "최소 비용 & 최소 환승"
                4 -> "최소 시간 & 최소 비용 & 최소 환승"
                else -> "최소 비용"
            }

            // 중복 여부 확인 및 라벨 업데이트
            if (result in listOf(2, 4)) {
                renderPath(path, label, updateLabelOnly = true)
            } else {
                renderPath(path, label)
            }
        }

        // 최소 환승 경로 추가
        pathData.leastTransfersPath?.paths?.forEachIndexed { index, path ->
            val result = comparisonResults.getOrNull(index) ?: 0
            if (result in listOf(2, 3, 4)) {
                Log.d("RenderDebug", "Skipping least transfer path as it's already included.")
                renderPath(path, "최소 환승", updateLabelOnly = true)
            } else {
                renderPath(path, "최소 환승")
            }
        }

        Log.d("FinalRenderedPaths", "Rendered Paths: ${renderedPaths.keys}")
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

        if (routeContainer == null) {
            Log.e("SearchFragment", "routeContainer is null! Check fragment_search.xml")
            return
        } else {
            Log.d("SearchFragment", "routeContainer initialized successfully!")
        }

        // EditText 기본값 설정
        startInput.setText(startLocation)
        destinationInput.setText(destinationLocation)

        // JSON 데이터 불러오기

        val pathData = loadPathDataFromJson()

        // displayRoutes 호출
        displayRoutes(pathData, routeContainer)
/*
        // 경로 데이터를 동적으로 추가
        fun displayRoutes(pathData: PathData) {
            routeContainer.removeAllViews() // 기존 View 초기화

            pathData.shortestPath?.paths?.forEachIndexed { index, path ->
                val routeView = createRouteView(path, "최소 시간 (${index + 1})")
                routeContainer.addView(routeView)
            }

            pathData.cheapestPath?.paths?.forEachIndexed { index, path ->
                val routeView = createRouteView(path, "최소 비용 (${index + 1})")
                routeContainer.addView(routeView)
            }

            pathData.leastTransfersPath?.paths?.forEachIndexed { index, path ->
                val routeView = createRouteView(path, "최소 환승 (${index + 1})")
                routeContainer.addView(routeView)
            }
        }
        Log.d("displayRoutes", "ShortestPath: ${pathData.shortestPath}")
        Log.d("displayRoutes", "CheapestPath: ${pathData.cheapestPath}")
        Log.d("displayRoutes", "LeastTransfersPath: ${pathData.leastTransfersPath?.paths}")


        // 초기 데이터 표시
        displayRoutes(pathData)
*/
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

            // 출발지와 도착지에 값이 없을 경우
            if (newStart.isBlank() || newDestination.isBlank()) {
                Toast.makeText(requireContext(), "출발지와 도착지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 출발지와 도착지가 동일할 경우
            if (newStart == newDestination) {
                Toast.makeText(context, "출발지와 도착지가 동일합니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 경로 탐색을 시작
            Log.d("ResearchButton", "Start: $newStart, Destination: $newDestination")

            // 새로운 JSON 데이터 로드 (예시로 동일한 JSON 로드 사용)
            val updatedPathData = loadPathDataFromJson()

            // RouteContainer 초기화 후 새 데이터로 업데이트
            routeContainer.removeAllViews()
            displayRoutes(updatedPathData, routeContainer)

            Toast.makeText(requireContext(), "새로운 경로를 탐색했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}