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
            Log.d("SearchFragment", "Loaded JSON data: $jsonString")

            val gson = Gson()
            gson.fromJson(jsonString, PathData::class.java)
        } catch (e: IOException) {
            Log.e("SearchFragment", "Error loading JSON data", e)
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
            text = label
            textSize = 12f
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
            val timeParts = this.split("시간", "분", "초").mapNotNull { it.trim().toIntOrNull() }
            val hours = if (timeParts.size > 1) timeParts[0] else 0
            val minutes = timeParts.getOrElse(timeParts.size - 1) { 0 }
            return hours * 60 + minutes
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
                text = "${segment.lineNumber}호선"
                textSize = 8f
                setTextColor(Color.WHITE)
                setBackgroundColor(getLineBackground(segment.lineNumber)) // 호선별 배경
                setPadding(4.dp, 0, 4.dp, 0)
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


    private fun displayRoutes(pathData: PathData, routeContainer: LinearLayout) {
        // 기존 뷰 모두 삭제
        routeContainer.removeAllViews()

        // 최소 시간 경로 추가
        pathData.shortestPath?.let { shortestPath ->
            val shortestPathView = createRouteView(shortestPath, "최소 시간")
            routeContainer.addView(shortestPathView)
            Log.d("RouteContainer", "Added ShortestPath View")
        }

        // 최소 비용 경로 추가
        pathData.cheapestPath?.let { cheapestPath ->
            val cheapestPathView = createRouteView(cheapestPath, "최소 비용")
            routeContainer.addView(cheapestPathView)
            Log.d("RouteContainer", "Added CheapestPath View")
        }

        // 최소 환승 경로 추가
        pathData.leastTransfersPath?.paths?.forEachIndexed { index, transferPath ->
            val leastTransfersPathView = createRouteView(transferPath, "최소 환승 (${index + 1})")
            routeContainer.addView(leastTransfersPathView)
            Log.d("RouteContainer", "Added LeastTransfersPath View for index $index")
        }

        // 현재 컨테이너에 추가된 뷰 개수 확인
        Log.d("RouteContainer", "Final Child Count: ${routeContainer.childCount}")
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