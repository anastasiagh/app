package com.example.app

import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("quotes?page=1")
    fun getData(): Call<responseDataClass>
}