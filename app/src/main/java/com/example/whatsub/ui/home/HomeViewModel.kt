package com.example.whatsub.ui.home


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.whatsub.data.api.ApiClient
import com.example.whatsub.data.api.NewsData
import com.example.whatsub.data.api.PathData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _pathData = MutableLiveData<PathData>()
    val pathData: LiveData<PathData> = _pathData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchShortestPath(startStation: String, endStation: String) {
        ApiClient.apiService.getShortestPath(startStation, endStation)
            .enqueue(object : Callback<PathData> {
                override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                    if (response.isSuccessful) {
                        _pathData.postValue(response.body())
                    } else {
                        _errorMessage.postValue("Error: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<PathData>, t: Throwable) {
                    _errorMessage.postValue("Failed to fetch data: ${t.message}")
                }
            })
    }

}