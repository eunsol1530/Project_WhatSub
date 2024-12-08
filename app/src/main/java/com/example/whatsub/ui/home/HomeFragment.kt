package com.example.whatsub.ui.home

import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Response
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.whatsub.R
import com.example.whatsub.data.api.RetrofitClient
import com.example.whatsub.databinding.FragmentHomeBinding
import com.example.whatsub.data.api.model.PathData
import com.google.gson.Gson
import retrofit2.Callback
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }*/
    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.resetPathData() // ViewModel 데이터 초기화
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        /*
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
*/

// 출발지와 도착지 입력 필드
        val startInput = binding.startInput
        val destinationInput = binding.destinationInput

        // 교환 버튼 ImageButton
        val exchangeButton: ImageButton = view.findViewById(R.id.btn_exchange)

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

        // 검색 버튼 클릭 이벤트
        binding.btnSearch.setOnClickListener {

            val startStation = startInput.text.toString().trim()
            val endStation = destinationInput.text.toString().trim()

            if (startStation.isBlank() || endStation.isBlank()) {
                Toast.makeText(requireContext(), "출발지와 도착지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (startStation == endStation) {
                Toast.makeText(requireContext(), "출발지와 도착지가 동일합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //val bundle = Bundle().apply {
            //  putString("startLocation", startInput.text.toString())
            //putString("destinationLocation", destinationInput.text.toString())
            //}

            // 새로운 검색 시작 시
            homeViewModel.resetPathData() // LiveData 초기화


            // ViewModel을 통해 데이터 요청
            homeViewModel.fetchShortestPath(startStation, endStation)


            /* val bundle = Bundle().apply {
                putString("startLocation", startStation)
                putString("destinationLocation", endStation)
            }
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)

        }

            */

            // LiveData 관찰
            homeViewModel.pathData.observe(viewLifecycleOwner) { pathData ->
                if (pathData?.shortestPath == null && pathData?.cheapestPath == null) {
                    Toast.makeText(
                        requireContext(),
                        "경로 데이터를 불러올 수 없습니다. 다시 시도해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 입력된 값을 초기화
                    startInput.setText("")
                    destinationInput.setText("")
                    return@observe

                } else {
                    // 중복 Navigation 방지
                    if (findNavController().currentDestination?.id != R.id.navigation_home) {
                        return@observe
                    }
                    val bundle = Bundle().apply {
                        putString("startLocation", startStation)
                        putString("destinationLocation", endStation)
                        putSerializable("pathData", pathData)
                    }
                    findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
                }
                }



        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "maybe null error")
                return@observe
            }
        }

    }
}


/*

            // `observe`를 명시적으로 호출
            homeViewModel.pathData.observeOnce(viewLifecycleOwner) { pathData ->
                if (pathData != null) {
                    val bundle = Bundle().apply {
                        putString("startLocation", startStation)
                        putString("destinationLocation", endStation)
                        putSerializable("pathData", pathData)
                    }
                    findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
                } else {
                    Toast.makeText(requireContext(), "경로를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
                observe(owner, object : Observer<T> {
                    override fun onChanged(t: T?) {
                        observer.onChanged(t)
                        removeObserver(this)
                    }
                })
            }


        }
/*
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

            val startStation = startInput.text.toString()
            val endStation = destinationInput.text.toString()

            if (startStation.isBlank() || endStation.isBlank()) {
                Toast.makeText(context, "출발지와 도착지를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (startStation == endStation) {
                Toast.makeText(context, "출발지와 도착지가 동일합니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /*
            if (startInput.text.isNullOrBlank() || destinationInput.text.isNullOrBlank()) {
                Toast.makeText(context, "출발지와 도착지를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startInput.text.toString() == destinationInput.text.toString()) {
                Toast.makeText(context, "출발지와 도착지가 동일합니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }*/

            // Retrofit 호출
            RetrofitClient.instance.getPathData(startStation.toInt(), endStation.toInt())
                .enqueue(object : Callback<PathData> {
                    override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                        if (response.isSuccessful && response.body() != null) {
                            val pathData = response.body()
                            val bundle = Bundle().apply {
                                putSerializable("pathData", pathData)
                            }
                            findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
                        } else {
                            Toast.makeText(context, "경로 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<PathData>, t: Throwable) {
                        Toast.makeText(context, "서버 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                })

            /*
            // JSON 데이터 확인
            val pathData = loadPathDataFromJson()

            if (pathData == null) {
                Toast.makeText(context, "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("startLocation", startInput.text.toString())
                putString("destinationLocation", destinationInput.text.toString())
            }

            findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
            */*/