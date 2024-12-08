package com.example.whatsub.ui.favorites

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.whatsub.data.api.model.TransferPath
import com.google.gson.Gson

class FavoritesViewModel(private val context: Context) : ViewModel() {

    private val sharedPref = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    private val _favorites = MutableLiveData<MutableList<TransferPath>>(loadFavorites(context))
    val favorites: LiveData<MutableList<TransferPath>> get() = _favorites

    fun addFavorite(transferPath: TransferPath) {
        val currentList = _favorites.value ?: mutableListOf()
        currentList.add(transferPath)
        _favorites.value = currentList
        saveFavorites(context, currentList)
    }

    companion object {
        private fun loadFavorites(context: Context): MutableList<TransferPath> {
            val sharedPref = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
            val favoritesSet = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()
            return favoritesSet.map { Gson().fromJson(it, TransferPath::class.java) }.toMutableList()
        }

        private fun saveFavorites(context: Context, favorites: MutableList<TransferPath>) {
            val sharedPref = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            val favoritesSet = favorites.map { Gson().toJson(it) }.toSet()
            editor.putStringSet("routes", favoritesSet)
            editor.apply()
        }
    }

    fun removeFavorite(transferPath: TransferPath) {
        val currentList = _favorites.value ?: mutableListOf()
        currentList.remove(transferPath)
        _favorites.value = currentList
        saveFavorites(context, currentList)
    }

    private fun saveToFavorites(transferPath: TransferPath) {
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()
        favorites.add(Gson().toJson(transferPath))
        sharedPref.edit().putStringSet("routes", favorites).apply()
    }

    private fun removeFromFavorites(transferPath: TransferPath) {
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()
        favorites.remove(Gson().toJson(transferPath))
        sharedPref.edit().putStringSet("routes", favorites).apply()
    }

    private fun loadFavorites(): MutableList<TransferPath> {
        val favorites = sharedPref.getStringSet("routes", mutableSetOf()) ?: mutableSetOf()
        return favorites.map { Gson().fromJson(it, TransferPath::class.java) }.toMutableList()
    }
}