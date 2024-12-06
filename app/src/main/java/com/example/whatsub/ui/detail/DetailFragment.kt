package com.example.whatsub.ui.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.whatsub.R
import com.example.whatsub.model.CheapestPath
import com.example.whatsub.model.PathData
import com.example.whatsub.model.ShortestPath
import com.example.whatsub.model.Transfer
import com.example.whatsub.model.TransferPath
import com.google.gson.Gson

class DetailFragment : Fragment(R.layout.fragment_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기 버튼 클릭 시 SearchFragment로 이동
        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().popBackStack()
        }

        /*// SearchFragment에서 전달받은 경로 타입
        val routeType = arguments?.getString("routeType") ?: return

        // JSON 데이터를 로드하여 PathData 객체로 변환
        val jsonString = requireContext().resources.openRawResource(R.raw.example_path_data)
            .bufferedReader().use { it.readText() }
        val pathData = Gson().fromJson(jsonString, PathData::class.java)

        // 선택된 경로 데이터를 가져오기
        val selectedRoute = when (routeType) {
            "shortest" -> pathData.shortestPath
            "cheapest" -> pathData.cheapestPath
            "leastTransfers" -> pathData.leastTransfersPath?.paths?.get(0) // 첫 번째 경로
            else -> null
        }
         */

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
        Log.d("DetailFragment", "Received routeType: $routeType")
        Log.d("DetailFragment", "Received routeData: $routeData")

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

        Log.d("DetailFragment", "Updated UI with routeType: $routeType, totalTime: ${routeData.totalTime}, totalCost: ${routeData.totalCost}")

        val container = requireView().findViewById<LinearLayout>(R.id.routeDetailContainer)

        // 기존 뷰 제거 및 로그 추가
        container.removeAllViews()
        Log.d("DetailFragment", "Container Cleared: Child Count=${container.childCount}")

        // Segments를 화면에 추가
        routeData.segments.forEach { segment ->
            val segmentView = createSegmentView(segment)
            container.addView(segmentView)
            Log.d("DetailFragment", "Added Segment: fromStation=${segment.fromStation}, toStation=${segment.toStation}")
        }
// 최종적으로 자식 뷰 개수 로그 출력
        Log.d("DetailFragment", "Final Child Count=${container.childCount}")

    }


    /*private fun displayRouteDetail(route: Any) {
        val container = requireView().findViewById<LinearLayout>(R.id.routeDetailContainer)

        // Segments 추출
        val segments: List<Transfer> = when (route) {
            is ShortestPath -> route.transfers // ShortestPath의 transfers
            is CheapestPath -> route.transfers // CheapestPath의 transfers
            is TransferPath -> route.segments  // TransferPath의 segments
            else -> emptyList() // 알 수 없는 타입일 경우 빈 리스트 반환
        }

        // Segments를 화면에 추가
        segments.forEach { segment ->
            val segmentView = createSegmentView(segment) // 각 segment를 처리하는 View 생성 함수
            container.addView(segmentView)
        }
    }*/
    private fun createSegmentView(segment: Transfer): View {
        val segmentView = layoutInflater.inflate(R.layout.segment_item, null)

        // 역 이름, 소요 시간, 비용, 화장실 개수, 편의점 개수 설정
        segmentView.findViewById<TextView>(R.id.fromStation).text = "출발역: ${segment.fromStation}"
        segmentView.findViewById<TextView>(R.id.toStation).text = "도착역: ${segment.toStation}"
        segmentView.findViewById<TextView>(R.id.lineNumber).text = "${segment.lineNumber}호선"
        segmentView.findViewById<TextView>(R.id.timeOnLine).text = "소요 시간: ${segment.timeOnLine}"
        segmentView.findViewById<TextView>(R.id.costOnLine).text = "비용: ${segment.costOnLine}"
        segmentView.findViewById<TextView>(R.id.toiletCount).text = "화장실: ${segment.toiletCount}개"
        segmentView.findViewById<TextView>(R.id.storeCount).text = "편의점: ${segment.storeCount}개"

        return segmentView
    }
}