package com.example.whatsub.ui.home


import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.whatsub.data.api.ApiClient
import com.example.whatsub.data.api.model.PathData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _pathData = MutableLiveData<PathData?>() // PathData를 nullable로 변경
    val pathData: LiveData<PathData?> = _pathData

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage


    fun fetchShortestPath(startStation: String, endStation: String) {
        ApiClient.apiService.getShortestPath(startStation.toInt(), endStation.toInt())
            .enqueue(object : Callback<PathData> {
                override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                    if (response.isSuccessful) {
                        // 서버 응답이 성공적인 경우
                        _pathData.postValue(response.body())
                        _errorMessage.postValue(null) // 에러 메시지 초기화
                    } else {
                        // 상태 코드에 따라 에러 메시지 처리
                        val errorMessage = when (response.code()) {
                            400 -> "잘못된 요청입니다. 출발지와 도착지를 확인해주세요."
                            404 -> "경로 데이터를 찾을 수 없습니다. 입력한 정보를 확인해주세요."
                            500 -> "서버에 문제가 발생했습니다. 경로를 확인하시고 잠시 후 다시 시도해주세요."
                            else -> "알 수 없는 오류가 발생했습니다. 상태 코드: ${response.code()}"
                        }
                        // 서버 응답이 실패한 경우 에러 처리
                        Log.e("API_ERROR", errorMessage)
                        _errorMessage.postValue(errorMessage)
                    }
                }

                override fun onFailure(call: Call<PathData>, t: Throwable) {
                    // 네트워크 오류 또는 다른 문제로 요청이 실패한 경우
                    Log.e("API_ERROR", "Failed to fetch data: ${t.message}")
                    _errorMessage.postValue("Failed to fetch data: ${t.message}")
                    _pathData.postValue(null) // 실패 시 데이터를 null로 설정
                }
            })
    }

    fun resetPathData() {
        _pathData.postValue(null) // 기존 데이터 초기화
        _errorMessage.postValue(null) // 에러 메시지 초기화
    }



}