package com.example.whatsub.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is game Fragment"
    }
    val text: LiveData<String> = _text
}