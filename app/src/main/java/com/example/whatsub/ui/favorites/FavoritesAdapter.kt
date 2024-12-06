// FavoritesAdapter.kt
package com.example.whatsub.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsub.R // R을 정확히 임포트
import com.example.whatsub.databinding.FragmentFavoritesBinding

class FavoritesAdapter(private val favoriteRoutes: List<RouteItem>) :
    RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val route = favoriteRoutes[position]
        holder.bind(route)
    }

    override fun getItemCount(): Int {
        return favoriteRoutes.size
    }

    inner class FavoriteViewHolder(private val binding: ItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(route: RouteItem) {
            binding.routeName.text = route.routeName
            binding.startEndStations.text = "${route.startStation} - ${route.endStation}"
            binding.details.text = route.details
        }
    }
}


