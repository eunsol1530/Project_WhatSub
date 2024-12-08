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

        favoritesViewModel = ViewModelProvider(requireActivity())[FavoritesViewModel::class.java]


        val routeContainer: LinearLayout = binding.root.findViewById(R.id.routeContainer)

        val routeViewMap = mutableMapOf<TransferPath, View>() // Path와 View 매핑


        favoritesList.forEach { path ->
            val routeView = RouteViewUtils.createRouteView(
                requireContext(),
                path,
                "즐겨찾기 경로"
            ) { transferPath, isFavorite ->
                if (!isFavorite) {
                    removeFromFavorites(transferPath)
                    favoritesList.remove(transferPath)

                    // routeViewMap에서 해당 View를 찾아 제거
                    val viewToRemove = routeViewMap[transferPath]
                    if (viewToRemove != null) {
                        routeContainer.removeView(viewToRemove)
                        routeViewMap.remove(transferPath)
                    }

                    if (favoritesList.isEmpty()) {
                        binding.textFavorites.visibility = View.VISIBLE
                    }
                }
            }
            // View와 Path를 매핑
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

    // SharedPreferences에서 즐겨찾기 데이터를 로드하는 함수
    private fun loadFavorites(): List<TransferPath> {
        val sharedPref = requireContext().getSharedPreferences("favorites", android.content.Context.MODE_PRIVATE)
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: return emptyList()

        // JSON 문자열을 TransferPath 객체로 변환
        return favorites.map { Gson().fromJson(it, TransferPath::class.java) }
    }

    // SharedPreferences에서 즐겨찾기 데이터를 삭제하는 함수
    private fun removeFromFavorites(transferPath: TransferPath) {
        val sharedPref = requireContext().getSharedPreferences("favorites", android.content.Context.MODE_PRIVATE)
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()

        val jsonToRemove = Gson().toJson(transferPath)
        favorites.remove(jsonToRemove)
        sharedPref.edit().putStringSet("routes", favorites).apply()
    }


}
