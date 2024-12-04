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

        // UI 요소 초기화
        val startInput: EditText = view.findViewById(R.id.start_location)
        val destinationInput: EditText = view.findViewById(R.id.destination_location)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPath)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // JSON 데이터 불러오기
        val pathDataList = loadPathDataFromJson()
        Log.d("SearchFragment", "Parsed JSON: $pathDataList")
        if (pathDataList.isEmpty()) {
            Log.d("SearchFragment", "No data available to display.")
            Toast.makeText(requireContext(), "표시할 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        // 전달받은 데이터와 JSON 데이터 비교
        val matchedData = pathDataList.filter {
            it.startStation.toString() == startLocation && it.endStation.toString() == destinationLocation
        }

        if (matchedData.isNotEmpty()) {
            Log.d("SearchFragment", "Matched Data: $matchedData")

            // RecyclerView 설정
            val adapter = PathAdapter(matchedData.toMutableList()) // 가변 리스트로 전달
            Log.d("SearchFragment", "Setting adapter with matched data: $matchedData")
            recyclerView.adapter = adapter
            Log.d("SearchFragment", "RecyclerView item count after setting adapter: ${recyclerView.adapter?.itemCount}")

            recyclerView.post {
                recyclerView.requestLayout() // 강제로 레이아웃 재갱신
                Log.d("SearchFragment", "RecyclerView Height After Post: ${recyclerView.height}")
            }

            // notifyDataSetChanged 호출
            (recyclerView.adapter as PathAdapter).notifyDataSetChanged()
            Log.d("SearchFragment", "RecyclerView Adapter set with ${pathDataList.size} items")

            recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
                Log.d("SearchFragment", "RecyclerView Height: ${recyclerView.height}")
            }

            recyclerView.visibility = View.VISIBLE

            recyclerView.addItemDecoration(
                object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        outRect.top = 8 // 각 아이템의 위쪽 간격
                        outRect.bottom = 8 // 각 아이템의 아래쪽 간격
                    }
                }
            )

            // 어댑터와 데이터 상태 확인
            Log.d("SearchFragment", "Adapter Item Count: ${recyclerView.adapter?.itemCount ?: 0}")
            if (pathDataList.isNotEmpty()) {
                Log.d("SearchFragment", "Path Data List: $pathDataList")
            } else {
                Log.d("SearchFragment", "Path Data List is empty")
            }

            Log.d("SearchFragment", "Adapter attached: ${recyclerView.adapter != null}")
            Log.d("SearchFragment", "RecyclerView height: ${recyclerView.height}")
            Log.d("SearchFragment", "RecyclerView item count: ${recyclerView.adapter?.itemCount}")

            // EditText 기본값 설정
            startInput.setText(matchedData[0].startStation.toString())
            destinationInput.setText(matchedData[0].endStation.toString())
        } else {
            Log.d("SearchFragment", "No matched data found")
            Toast.makeText(context, "해당 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        val sortWay: TextView = view.findViewById(R.id.sort_way)
        val sortWayDropdown: ImageView = view.findViewById(R.id.sort_way_dropdown)
        val researchBtn: TextView = view.findViewById(R.id.research_btn)
        val exchangeButton: ImageButton = view.findViewById(R.id.btn_exchange)

        // 버튼 클릭 이벤트
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

        // 이전 페이지에서 전달된 기본 옵션
        val defaultOption = arguments?.getString("selectedOption") ?: "option_min_time"

        // 기본 옵션 설정
        when (defaultOption) {
            "option_min_time" -> sortWay.text = "최소 시간"
            "option_min_cost" -> sortWay.text = "최소 비용"
            "option_min_transfer" -> sortWay.text = "최소 환승"
        }


        // 클릭 이벤트에 PopupMenu 연결
        val showPopupMenu: (View) -> Unit = { anchorView ->
            val popupMenu = PopupMenu(requireContext(), anchorView)
            popupMenu.menuInflater.inflate(R.menu.search_sort_menu, popupMenu.menu)

            // PopupMenu 항목 클릭 이벤트 처리
            popupMenu.setOnMenuItemClickListener { menuItem ->
                val sortedList = when (menuItem.itemId) {
                    R.id.option_min_time -> {
                        sortWay.text = "최소 시간"
                        pathDataList.sortedBy {
                            val timeParts = it.totalTime.split("분", "초").mapNotNull { part -> part.trim().toIntOrNull() }
                            val minutes = timeParts.getOrNull(0) ?: 0
                            val seconds = timeParts.getOrNull(1) ?: 0
                            minutes * 60 + seconds
                        }
                    }
                    R.id.option_min_cost -> {
                        sortWay.text = "최소 비용"
                        pathDataList.sortedBy {
                            it.totalCost.replace(",", "").replace("원", "").toIntOrNull() ?: Int.MAX_VALUE
                        }
                    }
                    R.id.option_min_transfer -> {
                        sortWay.text = "최소 환승"
                        pathDataList.sortedBy { it.transfers.size }
                    }
                    else -> pathDataList
                }

                // RecyclerView 어댑터 업데이트
                val adapter = recyclerView.adapter as PathAdapter
                adapter.updateData(sortedList)
                Log.d("SearchFragment", "RecyclerView updated with sorted list: ${sortedList.size} items")

                true
            }

            popupMenu.show()

            }

       /* when (menuItem.itemId) {
            R.id.option_min_time -> {
                sortWay.text = "최소 시간"
                val sortedByTime = pathDataList.sortedBy {
                    it.totalTime.replace("분", "").replace("초", "").replace(" ", "").toIntOrNull() ?: Int.MAX_VALUE
                }
                true
            }

            R.id.option_min_cost -> {
                sortWay.text = "최소 비용"
                val sortedByCost = pathDataList.sortedBy {
                    it.totalCost.replace(",", "").replace("원", "").toIntOrNull() ?: Int.MAX_VALUE
                }
                true
            }

            R.id.option_min_transfer -> {
                sortWay.text = "최소 환승"
                val sortedByTransfers = pathDataList.sortedBy { it.transfers.size }
                true
            }

            else -> false
        }

            // PopupMenu 표시
            popupMenu.show()
        }

        // 텍스트와 화살표 아이콘 클릭 시 PopupMenu 표시
        sortWay.setOnClickListener { showPopupMenu(it) }
        sortWayDropdown.setOnClickListener { showPopupMenu(it) }

*/
        // 텍스트와 화살표 아이콘 클릭 시 PopupMenu 표시
        sortWay.setOnClickListener { showPopupMenu(it) }
        sortWayDropdown.setOnClickListener { showPopupMenu(it) }

        // 재탐색 버튼 클릭 이벤트
        researchBtn.setOnClickListener {
            val currentSortText = sortWay.text.toString()
            val sortedList = when (currentSortText) {
                "최소 시간" -> pathDataList.sortedBy {
                    val timeParts = it.totalTime.split("분", "초").mapNotNull { part -> part.trim().toIntOrNull() }
                    val minutes = timeParts.getOrNull(0) ?: 0
                    val seconds = timeParts.getOrNull(1) ?: 0
                    minutes * 60 + seconds
                }
                "최소 비용" -> pathDataList.sortedBy {
                    it.totalCost.replace(",", "").replace("원", "").toIntOrNull() ?: Int.MAX_VALUE
                }
                "최소 환승" -> pathDataList.sortedBy { it.transfers.size }
                else -> pathDataList
            }

            val adapter = recyclerView.adapter as PathAdapter
            adapter.updateData(sortedList)
        }
    }
}