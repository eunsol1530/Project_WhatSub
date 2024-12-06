// FavoritesViewModel.kt
package com.example.whatsub.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class FavoriteItem(
    val routeName: String,
    val startStation: String,
    val endStation: String
)

class FavoritesViewModel : ViewModel() {

    private val _favoritesData = MutableLiveData<List<FavoriteItem>>()
    val favoritesData: LiveData<List<FavoriteItem>> = _favoritesData

    // 즐겨찾기 항목을 추가하는 메서드
    fun addFavorite(routeName: String, startStation: String, endStation: String) {
        val currentList = _favoritesData.value ?: emptyList()
        val newFavorite = FavoriteItem(routeName, startStation, endStation)
        _favoritesData.value = currentList + newFavorite  // 기존 리스트에 새로운 항목 추가
    }
}
