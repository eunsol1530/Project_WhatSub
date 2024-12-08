package com.example.whatsub.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.whatsub.data.api.model.TransferPath

class FavoritesViewModel : ViewModel() {

    private val _favorites = MutableLiveData<MutableList<TransferPath>>(mutableListOf())
    val favorites: LiveData<MutableList<TransferPath>> = _favorites

    fun addFavorite(transferPath: TransferPath) {
        val currentList = _favorites.value ?: mutableListOf()
        currentList.add(transferPath)
        _favorites.value = currentList
    }

    fun removeFavorite(transferPath: TransferPath) {
        val currentList = _favorites.value ?: mutableListOf()
        currentList.remove(transferPath)
        _favorites.value = currentList
    }
}
