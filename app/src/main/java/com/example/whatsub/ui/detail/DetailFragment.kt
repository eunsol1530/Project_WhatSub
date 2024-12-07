package com.example.whatsub.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.whatsub.R
import com.example.whatsub.model.Transfer
import com.example.whatsub.model.TransferPath

class DetailFragment : Fragment(R.layout.fragment_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기 버튼 클릭 시 SearchFragment로 이동
        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().popBackStack()
        }

        // 전달받은 데이터 수신
        val routeType = arguments?.getString("routeType")
        val routeData = arguments?.getSerializable("routeData") as? TransferPath
        Log.d("DetailFragment", "Received routeType: $routeType")
        Log.d("DetailFragment", "Received routeData: $routeData")

        if (routeType == null || routeData == null) {
            Toast.makeText(requireContext(), "잘못된 데이터입니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        // 출발지와 도착지 업데이트
        val startLocationTextView: TextView = view.findViewById(R.id.start_location)
        val destinationLocationTextView: TextView = view.findViewById(R.id.destination_location)

        startLocationTextView.text = "${routeData.segments.first().fromStation}"
        destinationLocationTextView.text = "${routeData.segments.last().toStation}"

        // 경로 데이터를 UI에 표시
        displayRouteDetail(routeType, routeData)
    }

    private fun displayRouteDetail(routeType: String, routeData: TransferPath) {

        val routeTypeTextView: TextView = view?.findViewById(R.id.routeTypeTextView) ?: return
        val totalTimeTextView: TextView = view?.findViewById(R.id.totalTimeTextView) ?: return
        val totalCostTextView: TextView = view?.findViewById(R.id.totalCostTextView) ?: return

        // 텍스트 업데이트
        routeTypeTextView.text = routeType
        totalTimeTextView.text = routeData.totalTime
        totalCostTextView.text = routeData.totalCost

        Log.d(
            "DetailFragment",
            "Updated UI with routeType: $routeType, totalTime: ${routeData.totalTime}, totalCost: ${routeData.totalCost}"
        )

        // 컨테이너 초기화
        val container = requireView().findViewById<LinearLayout>(R.id.routeDetailContainer)
        container.removeAllViews()

        val totalMinutes = routeData.segments.sumOf { parseTimeToMinutes(it.timeOnLine) }

        routeData.segments.forEachIndexed { index, segment ->
            // 출발역 및 환승 아이콘 추가
            if (index == 0) {
                val startContainer = createStationContainer(
                    iconRes = R.drawable.icon_transfer,
                    stationName = segment.fromStation.toString(),
                    toiletCount = segment.toiletCount,
                    storeCount = segment.storeCount
                )
                container.addView(startContainer)
            }

            // 노선 구간 정보 추가
            val segmentView = createSegmentView(segment, totalMinutes)
            container.addView(segmentView)

            // 환승 정보 추가
            if (index < routeData.segments.size - 1) {
                val transferContainer = createStationContainer(
                    iconRes = R.drawable.icon_transfer,
                    stationName = segment.toStation.toString()
                )
                container.addView(transferContainer)
            }

            // 도착역 정보 추가
            if (index == routeData.segments.size - 1) {
                val endContainer = createStationContainer(
                    iconRes = R.drawable.icon_transfer,
                    stationName = segment.toStation.toString(),
                    toiletCount = segment.toiletCount,
                    storeCount = segment.storeCount
                )
                container.addView(endContainer)
            }
        }

    }

    private fun createSegmentView(segment: Transfer, totalMinutes:Int): View {
        val segmentLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL // 가로 방향으로 설정
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(100, 10, 0, 20)
            }
            gravity = Gravity.CENTER_VERTICAL // 중앙 정렬
        }
// 노선 라인의 높이를 비율에 따라 설정
        val segmentTime = parseTimeToMinutes(segment.timeOnLine)
        val lineHeight = if (totalMinutes > 0) {
            (segmentTime.toFloat() / totalMinutes * 700).toInt() // 비율 기반 높이 계산 (예: 1000dp 기준)
        } else {
            100 // 기본값
        }

        // 노선 라인 (세로)
        val lineView = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                25, lineHeight // 높이를 동적으로 설정
            ).apply {
                setMargins(35, 0, 0, 0)
            }
            setBackgroundColor(getLineColor(segment.lineNumber))
        }

// 노선 정보 텍스트를 포함하는 LinearLayout
        val textLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL // 세로 방향으로 정렬
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(140, 0, 0, 10) // 텍스트를 라인에서 떨어뜨리는 마진
            }
        }

        val textfirstLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 0) // 텍스트를 라인에서 떨어뜨리는 마진
            }
        }

        // 첫 번째 줄: 노선 정보 (몇 호선, 걸리는 시간)
        val lineInfo = TextView(requireContext()).apply {
            text = "${segment.lineNumber}호선"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = Gravity.START
            setTypeface(null, android.graphics.Typeface.BOLD) // **굵은 글꼴**
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 0) // 텍스트를 라인에서 떨어뜨리는 마진
            }
        }
        textfirstLayout.addView(lineInfo)

        val lineMinute = TextView(requireContext()).apply {
            text = "${segment.timeOnLine}"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = Gravity.START
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(60, 0, 0, 0) // 텍스트를 라인에서 떨어뜨리는 마진
            }
        }
        textfirstLayout.addView(lineMinute)

        // 두 번째 줄: 비용 정보
        val lineInfoBottom = TextView(requireContext()).apply {
            text = "${segment.costOnLine}"
            textSize = 13f
            setTextColor(Color.GRAY)
            gravity = Gravity.START
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(30, 10, 0, 0) // 텍스트를 라인에서 떨어뜨리는 마진
            }
        }

        // 텍스트 레이아웃에 추가
        textLayout.addView(textfirstLayout)
        textLayout.addView(lineInfoBottom)

        // 전체 레이아웃에 추가
        segmentLayout.addView(lineView)
        segmentLayout.addView(textLayout)

        return segmentLayout
    }

    private fun parseTimeToMinutes(time: String): Int {
        val regex = "(?:(\\d+)시간)?\\s*(?:(\\d+)분)?\\s*(?:(\\d+)초)?".toRegex()
        val matchResult = regex.find(time)
        if (matchResult != null) {
            val hours = matchResult.groupValues[1].toIntOrNull() ?: 0
            val minutes = matchResult.groupValues[2].toIntOrNull() ?: 0
            val seconds = matchResult.groupValues[3].toIntOrNull() ?: 0
            return hours * 60 + minutes + (seconds / 60)
        }
        return 0
    }


    private fun createStationContainer(
        iconRes: Int,
        stationName: String,
        toiletCount: Int? = null,
        storeCount: Int? = null
    ): View {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(100, 0, 0, 0)
            }
            gravity = Gravity.CENTER_VERTICAL // 세로 가운데 정렬
        }

        // 아이콘 추가
        val icon = ImageView(requireContext()).apply {
            setImageResource(iconRes)
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(0,0,70,0)
            }
        }

        // 역 이름 추가
        val stationNameText = TextView(requireContext()).apply {
            text = stationName
            textSize = 20f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD) // **굵은 글꼴**
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(22, 0, 40, 0) // **텍스트를 라인에서 떨어뜨리는 마진**
            }
        }

        // 화장실/편의점 정보 레이아웃
        val facilityInfoLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 5, 0, 5) // 텍스트와의 간격 조정
            }
            gravity = Gravity.CENTER_VERTICAL
        }

        // 화장실 아이콘과 텍스트
        toiletCount?.let {
            val toiletIcon = ImageView(requireContext()).apply {
                setImageResource(R.drawable.icon_toilet) // 화장실 아이콘 적용
                layoutParams = LinearLayout.LayoutParams(60, 60).apply {
                    setMargins(0, 0, 10, 0)
                }
            }

            val toiletText = TextView(requireContext()).apply {
                text = "$it"
                textSize = 16f
                setTextColor(Color.GRAY)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            facilityInfoLayout.addView(toiletIcon)
            facilityInfoLayout.addView(toiletText)
        }

        // 편의점 아이콘과 텍스트
        storeCount?.let {
            val storeIcon = ImageView(requireContext()).apply {
                setImageResource(R.drawable.icon_store) // 편의점 아이콘 적용
                layoutParams = LinearLayout.LayoutParams(60, 60).apply {
                    setMargins(20, 0, 10, 0) // 간격 조정
                }
            }

            val storeText = TextView(requireContext()).apply {
                text = "$it"
                textSize = 16f
                setTextColor(Color.GRAY)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            facilityInfoLayout.addView(storeIcon)
            facilityInfoLayout.addView(storeText)
        }

        // 컨테이너에 요소 추가
        container.addView(icon)
        container.addView(stationNameText)
        container.addView(facilityInfoLayout)
        return container
    }
    }

    private fun getLineColor(lineNumber: Int): Int {
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
