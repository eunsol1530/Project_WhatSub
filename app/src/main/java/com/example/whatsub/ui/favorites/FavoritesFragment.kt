package com.example.whatsub.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsub.R
import com.example.whatsub.data.api.model.TransferPath
import com.example.whatsub.databinding.FragmentFavoritesBinding
import com.google.gson.Gson

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

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
        favoritesList.addAll(loadFavorites())

        // RecyclerView와 Adapter 설정
        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

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
        }
    }

    private fun loadFavorites(): List<TransferPath> {
        val sharedPref = requireContext().getSharedPreferences("favorites", android.content.Context.MODE_PRIVATE)
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: return emptyList()
        return favorites.map { Gson().fromJson(it, TransferPath::class.java) }
    }

    private fun removeFromFavorites(transferPath: TransferPath) {
        val sharedPref = requireContext().getSharedPreferences("favorites", android.content.Context.MODE_PRIVATE)
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()

        val jsonToRemove = Gson().toJson(transferPath)
        favorites.remove(jsonToRemove)
        sharedPref.edit().putStringSet("routes", favorites).apply()
    }
}
