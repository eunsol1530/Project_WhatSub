package com.example.whatsub.ui.favorites

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsub.R
import com.example.whatsub.data.api.model.TransferPath
import com.example.whatsub.databinding.FragmentFavoritesBinding
import com.example.whatsub.ui.utils.RouteViewUtils
import com.google.gson.Gson

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var adapter: FavoritesAdapter


    private val favoritesList: MutableList<TransferPath> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 즐겨찾기 데이터 로드
        favoritesList.clear()
        favoritesList.addAll(loadFavorites())

        val factory = FavoritesViewModelFactory(requireContext())
        favoritesViewModel = ViewModelProvider(this, factory)[FavoritesViewModel::class.java]

        // SharedPreferences에서 즐겨찾기 데이터 로드
        val routeContainer: LinearLayout = binding.root.findViewById(R.id.routeContainer)
        val favoritesList = favoritesViewModel.favorites.value ?: mutableListOf()

        val routeViewMap = mutableMapOf<TransferPath, View>()

        favoritesList.forEach { path ->
            val routeView = RouteViewUtils.createRouteView(
                requireContext(),
                path,
                "즐겨찾기 경로"
            ) { transferPath, isFavorite ->
                if (!isFavorite) {
                    favoritesViewModel.removeFavorite(transferPath)
                    routeViewMap[transferPath]?.let { viewToRemove ->
                        routeContainer.removeView(viewToRemove)
                        routeViewMap.remove(transferPath)
                    }

                    if (favoritesList.isEmpty()) {
                        binding.textFavorites.visibility = View.VISIBLE
                    }
                }
            }
            routeViewMap[path] = routeView
            routeContainer.addView(routeView)
        }
/*
        if (favoritesList.isEmpty()) {
            binding.textFavorites.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            binding.textFavorites.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            val adapter = FavoritesAdapter(favoritesList) { removedFavorite ->
                removeFromFavorites(removedFavorite)
                favoritesList.remove(removedFavorite)
                recyclerView.adapter?.notifyDataSetChanged()

                if (favoritesList.isEmpty()) {
                    binding.textFavorites.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }

                Toast.makeText(requireContext(), "즐겨찾기에서 삭제되었습니다!", Toast.LENGTH_SHORT).show()
            }
            recyclerView.adapter = adapter
        }*/
    }

    private fun saveToFavorites(transferPath: TransferPath) {
        val sharedPref = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()

        val routeJson = Gson().toJson(transferPath) // 데이터를 JSON으로 변환
        favorites.add(routeJson) // 즐겨찾기에 추가
        sharedPref.edit().putStringSet("routes", favorites).apply() // SharedPreferences에 저장
    }

    private fun removeFromFavorites(transferPath: TransferPath) {
        val sharedPref = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()

        val routeJson = Gson().toJson(transferPath) // 데이터를 JSON으로 변환
        favorites.remove(routeJson) // 즐겨찾기에서 제거
        sharedPref.edit().putStringSet("routes", favorites).apply() // SharedPreferences에 저장
    }

    private fun loadFavorites(): MutableList<TransferPath> {
        val sharedPref = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()

        // JSON 문자열을 TransferPath 객체로 변환
        return favorites.map { Gson().fromJson(it, TransferPath::class.java) }.toMutableList()
    }


}
