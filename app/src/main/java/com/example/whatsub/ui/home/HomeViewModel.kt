package com.example.whatsub.ui.home


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.whatsub.data.api.ApiClient
import com.example.whatsub.data.api.model.PathData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _pathData = MutableLiveData<PathData>()
    val pathData: LiveData<PathData> = _pathData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchShortestPath(startStation: String, endStation: String) {
        ApiClient.apiService.getShortestPath(startStation.toInt(), endStation.toInt())
            .enqueue(object : Callback<PathData> {
                override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                    if (response.isSuccessful) {
                        // 서버 응답이 성공적인 경우
                        _pathData.postValue(response.body())
                    } else {
                        // 서버 응답이 실패한 경우 에러 처리
                        Log.e("API_ERROR", "Error: ${response.errorBody()?.string()}")
                        _errorMessage.postValue("Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<PathData>, t: Throwable) {
                    // 네트워크 오류 또는 다른 문제로 요청이 실패한 경우
                    Log.e("API_ERROR", "Failed to fetch data: ${t.message}")
                    _errorMessage.postValue("Failed to fetch data: ${t.message}")
                }
            })
    }


}