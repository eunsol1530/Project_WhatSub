package com.example.whatsub.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsub.databinding.FragmentFavoritesBinding
import com.google.gson.Gson

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!  // 바인딩 객체를 안전하게 가져오기 위해 _binding!!

    private lateinit var recyclerViewFavorites: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // RecyclerView 초기화
        recyclerViewFavorites = binding.recyclerViewFavorites  // 바인딩 객체를 통해 recyclerViewFavorites 참조
        recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())

        // FavoritesAdapter 초기화 및 RecyclerView 설정
        favoritesAdapter = FavoritesAdapter(getFavoriteRoutes())
        recyclerViewFavorites.adapter = favoritesAdapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 뷰 바인딩 해제
    }

    // JSON 데이터를 파싱하여 RouteItem 리스트로 반환
    private fun getFavoriteRoutes(): List<RouteItem> {
        // JSON 문자열 예시
        val jsonString = """[
            {"routeName": "Shortest Route", "startStation": "Station 101", "endStation": "Station 201", "details": "Details 1"},
            {"routeName": "Most Comfortable Route", "startStation": "Station 202", "endStation": "Station 303", "details": "Details 2"},
            {"routeName": "Scenic Route", "startStation": "Station 404", "endStation": "Station 505", "details": "Details 3"}
        ]"""

        // Gson 객체 생성
        val gson = Gson()

        // JSON 데이터를 RouteItem 리스트로 변환
        return gson.fromJson(jsonString, Array<RouteItem>::class.java).toList()
    }
}
