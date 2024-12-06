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

        container.post {
            val containerHeight = (container.height * 3 / 4 - 115) // 화면의 75% 높이 사용
            if (containerHeight > 0) {
                // 출발 아이콘 추가
                val startIconView = createTransferIconView()
                container.addView(startIconView)

                routeData.segments.forEachIndexed { index, segment ->
                    val weight = parseTimeToMinutes(segment.timeOnLine).toFloat() / totalMinutes

                    // 구간 View 추가
                    val segmentView = createSegmentView(
                        segment = segment,
                        weight = weight,
                        containerHeight = containerHeight
                    )
                    container.addView(segmentView)

                    // 중간 환승 아이콘 추가 (마지막 구간 제외)
                    if (index < routeData.segments.size - 1) {
                        val transferIconView = createTransferIconView()
                        container.addView(transferIconView)
                    }
                }

                // 도착 아이콘 추가
                val endIconView = createTransferIconView()
                container.addView(endIconView)
            }
        }

    }
        /*
        // Segments를 화면에 추가
        container.post {
            val containerHeight = container.height
            if (containerHeight > 0) {
                routeData.segments.forEach { segment ->
                    val segmentMinutes = parseTimeToMinutes(segment.timeOnLine)
                    val weight = if (totalMinutes > 0) segmentMinutes.toFloat() / totalMinutes.toFloat() else 0f

                    if (weight > 0) {
                        val segmentView = createSegmentView(segment, weight, containerHeight, isFirst, isLast)
                        container.addView(segmentView)

                        Log.d("DetailFragment", "Added Segment: fromStation=${segment.fromStation}, toStation=${segment.toStation}")
                    } else {
                        Log.e("DetailFragment", "Weight calculation failed: segmentMinutes=$segmentMinutes, totalMinutes=$totalMinutes")
                    }
                }
            } else {
                Log.e("DetailFragment", "Container height is zero. Unable to create segment views.")
            }

            // 최종적으로 자식 뷰 개수 로그 출력
            Log.d("DetailFragment", "Final Child Count=${container.childCount}")
        }*/

    private fun parseTimeToMinutes(time: String): Int {
        val regex = "(\\d+)분\\s*(\\d+)?초?".toRegex()
        val matchResult = regex.find(time)
        if (matchResult != null) {
            val minutes = matchResult.groupValues[1].toIntOrNull() ?: 0
            val seconds = matchResult.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            return minutes + (seconds / 60)
        }
        return 0
    }

    // 노선 라인과 데이터 간 여유 간격 및 아이콘 크기 조정
    private fun createSegmentView(
        segment: Transfer,
        weight: Float,
        containerHeight: Int
    ): View {
        val segmentLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ((containerHeight * weight)+20).toInt()
            ).apply {
                setMargins(0, 12, 0, 12) // 위아래 여유 간격
            }
        }

        val iconAndLineContainer = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                80, // 고정된 너비
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(100, 0, 0, 0) // 노선 라인과 데이터 간 간격
            }
        }

        // 노선 라인
        val lineBar = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                30, // 라인 두께
                LinearLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER_VERTICAL // 라인 중앙 정렬
            )
            setBackgroundColor(getLineColor(segment.lineNumber))
        }
        iconAndLineContainer.addView(lineBar)

        // 정보 레이아웃 (아이콘과 텍스트 여유 간격)
        val infoLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(70, 0, 0, 0) // 노선 라인과 데이터 간 간격
            }
        }

        // 역 정보 텍스트
        val stationInfo = TextView(requireContext()).apply {
            text = "${segment.fromStation} ➡ ${segment.toStation} | ${segment.lineNumber}호선 | ${segment.timeOnLine}"
            textSize = 16f
        }

        // 화장실, 편의점 아이콘 및 텍스트
        val facilityInfo = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(ImageView(requireContext()).apply {
                setImageResource(R.drawable.icon_toilet)
                layoutParams = LinearLayout.LayoutParams(36, 36) // 아이콘 크기 축소
            })
            addView(TextView(requireContext()).apply {
                text = "${segment.toiletCount}개"
                textSize = 14f
                setTextColor(Color.DKGRAY)
            })
            addView(ImageView(requireContext()).apply {
                setImageResource(R.drawable.icon_store)
                layoutParams = LinearLayout.LayoutParams(36, 36) // 아이콘 크기 축소
            })
            addView(TextView(requireContext()).apply {
                text = "${segment.storeCount}개"
                textSize = 14f
                setTextColor(Color.DKGRAY)
            })
        }

        infoLayout.addView(stationInfo)
        infoLayout.addView(facilityInfo)

        segmentLayout.addView(iconAndLineContainer)
        segmentLayout.addView(infoLayout)

        return segmentLayout
    }

    // 환승 아이콘 중앙 배치
    private fun createTransferIconView(): View {
        val iconLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                120F
            ).apply {
                setMargins(63, 0, 0, 0) // 여유 간격 추가
            }
            gravity = Gravity.CENTER_VERTICAL // 아이콘이 노선 라인과 수직 정렬되도록 설정
        }

        val icon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.icon_transfer)
            layoutParams = LinearLayout.LayoutParams(
                100, 100 // 아이콘 크기 설정
            ).apply {
                gravity = Gravity.CENTER_VERTICAL // 수직 중앙 정렬
            }
        }

        iconLayout.addView(icon)

        return iconLayout
    }

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

            container.post {
                val containerHeight = (container.height * 3 / 4 - 115) // 화면의 75% 높이 사용
                if (containerHeight > 0) {
                    // 출발 아이콘 추가
                    val startIconView = createTransferIconView()
                    container.addView(startIconView)

                    routeData.segments.forEachIndexed { index, segment ->
                        val weight = parseTimeToMinutes(segment.timeOnLine).toFloat() / totalMinutes

                        // 구간 View 추가
                        val segmentView = createSegmentView(
                            segment = segment,
                            weight = weight,
                            containerHeight = containerHeight
                        )
                        container.addView(segmentView)

                        // 중간 환승 아이콘 추가 (마지막 구간 제외)
                        if (index < routeData.segments.size - 1) {
                            val transferIconView = createTransferIconView()
                            container.addView(transferIconView)
                        }
                    }

                    // 도착 아이콘 추가
                    val endIconView = createTransferIconView()
                    container.addView(endIconView)
                }
            }

        }
        /*
        // Segments를 화면에 추가
        container.post {
            val containerHeight = container.height
            if (containerHeight > 0) {
                routeData.segments.forEach { segment ->
                    val segmentMinutes = parseTimeToMinutes(segment.timeOnLine)
                    val weight = if (totalMinutes > 0) segmentMinutes.toFloat() / totalMinutes.toFloat() else 0f

                    if (weight > 0) {
                        val segmentView = createSegmentView(segment, weight, containerHeight, isFirst, isLast)
                        container.addView(segmentView)

                        Log.d("DetailFragment", "Added Segment: fromStation=${segment.fromStation}, toStation=${segment.toStation}")
                    } else {
                        Log.e("DetailFragment", "Weight calculation failed: segmentMinutes=$segmentMinutes, totalMinutes=$totalMinutes")
                    }
                }
            } else {
                Log.e("DetailFragment", "Container height is zero. Unable to create segment views.")
            }

            // 최종적으로 자식 뷰 개수 로그 출력
            Log.d("DetailFragment", "Final Child Count=${container.childCount}")
        }*/

        private fun parseTimeToMinutes(time: String): Int {
            val regex = "(\\d+)분\\s*(\\d+)?초?".toRegex()
            val matchResult = regex.find(time)
            if (matchResult != null) {
                val minutes = matchResult.groupValues[1].toIntOrNull() ?: 0
                val seconds = matchResult.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
                return minutes + (seconds / 60)
            }
            return 0
        }

        // 노선 라인과 데이터 간 여유 간격 및 아이콘 크기 조정
        private fun createSegmentView(
            segment: Transfer,
            weight: Float,
            containerHeight: Int
        ): View {
            val segmentLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ((containerHeight * weight)+20).toInt()
                ).apply {
                    setMargins(0, 12, 0, 12) // 위아래 여유 간격
                }
            }

            val iconAndLineContainer = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    80, // 고정된 너비
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(100, 0, 0, 0) // 노선 라인과 데이터 간 간격
                }
            }

            // 노선 라인
            val lineBar = View(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    30, // 라인 두께
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER_VERTICAL // 라인 중앙 정렬
                )
                setBackgroundColor(getLineColor(segment.lineNumber))
            }
            iconAndLineContainer.addView(lineBar)

            // 정보 레이아웃 (아이콘과 텍스트 여유 간격)
            val infoLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(70, 0, 0, 0) // 노선 라인과 데이터 간 간격
                }
            }

            // 역 정보 텍스트
            val stationInfo = TextView(requireContext()).apply {
                text = "${segment.fromStation} ➡ ${segment.toStation} | ${segment.lineNumber}호선 | ${segment.timeOnLine}"
                textSize = 16f
            }

            // 화장실, 편의점 아이콘 및 텍스트
            val facilityInfo = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(ImageView(requireContext()).apply {
                    setImageResource(R.drawable.icon_toilet)
                    layoutParams = LinearLayout.LayoutParams(36, 36) // 아이콘 크기 축소
                })
                addView(TextView(requireContext()).apply {
                    text = "${segment.toiletCount}개"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                })
                addView(ImageView(requireContext()).apply {
                    setImageResource(R.drawable.icon_store)
                    layoutParams = LinearLayout.LayoutParams(36, 36) // 아이콘 크기 축소
                })
                addView(TextView(requireContext()).apply {
                    text = "${segment.storeCount}개"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                })
            }

            infoLayout.addView(stationInfo)
            infoLayout.addView(facilityInfo)

            segmentLayout.addView(iconAndLineContainer)
            segmentLayout.addView(infoLayout)

            return segmentLayout
        }

        // 환승 아이콘 중앙 배치
        private fun createTransferIconView(): View {
            val iconLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    120F
                ).apply {
                    setMargins(63, 0, 0, 0) // 여유 간격 추가
                }
                gravity = Gravity.CENTER_VERTICAL // 아이콘이 노선 라인과 수직 정렬되도록 설정
            }

            val icon = ImageView(requireContext()).apply {
                setImageResource(R.drawable.icon_transfer)
                layoutParams = LinearLayout.LayoutParams(
                    100, 100 // 아이콘 크기 설정
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL // 수직 중앙 정렬
                }
            }

            iconLayout.addView(icon)

            return iconLayout
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
}
