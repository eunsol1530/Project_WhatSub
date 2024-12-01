package com.example.whatsub.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.whatsub.R
import com.example.whatsub.databinding.FragmentHomeBinding
import com.example.whatsub.ui.search.SearchFragment

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
        searchButton.setOnClickListener{
            if (startInput.text.isNullOrBlank() || destinationInput.text.isNullOrBlank()) {
                Toast.makeText(context, "출발지와 도착지를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = Bundle()
            bundle.putString("startLocation", startInput.text.toString())
            bundle.putString("destinationLocation", destinationInput.text.toString())

            findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
            }
        }
    }