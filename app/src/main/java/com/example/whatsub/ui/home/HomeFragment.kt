package com.example.whatsub.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.whatsub.R
import com.example.whatsub.databinding.FragmentHomeBinding
import com.example.whatsub.data.api.model.PathData
import com.google.gson.Gson
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // JSON 데이터 로드 함수 추가
        fun loadPathDataFromJson(): PathData? {
            return try {
                val jsonString = requireContext().resources.openRawResource(R.raw.example_path_data)
                    .bufferedReader()
                    .use { it.readText() }
                Gson().fromJson(jsonString, PathData::class.java)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }



        // 출발지와 도착지 EditText
        val startInput: EditText = view.findViewById(R.id.start_input)
        val destinationInput: EditText = view.findViewById(R.id.destination_input)

        // 교환 버튼 ImageButton
        val exchangeButton: ImageButton = view.findViewById(R.id.btn_exchange)
        // 검색 버튼
        val searchButton: ImageButton = view.findViewById(R.id.btn_search)

        // 버튼 클릭 이벤트
        exchangeButton.setOnClickListener {
            if (startInput.text.isNullOrBlank() || destinationInput.text.isNullOrBlank()) {
                Toast.makeText(context, "출발지와 도착지를 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                val startText = startInput.text.toString()
                val destinationText = destinationInput.text.toString()
                startInput.setText(destinationText)
                destinationInput.setText(startText)
            }
        }
        searchButton.setOnClickListener {
            if (startInput.text.isNullOrBlank() || destinationInput.text.isNullOrBlank()) {
                Toast.makeText(context, "출발지와 도착지를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startInput.text.toString() == destinationInput.text.toString()) {
                Toast.makeText(context, "출발지와 도착지가 동일합니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // JSON 데이터 확인
            val pathData = loadPathDataFromJson()

            if (pathData == null) {
                Toast.makeText(context, "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
/*
            if (matchedData.isEmpty()) {
                Toast.makeText(context, "해당 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }*/

            val bundle = Bundle().apply {
                putString("startLocation", startInput.text.toString())
                putString("destinationLocation", destinationInput.text.toString())
            }

            findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
        }
    }
}