package com.smrutilipi.weatherappmvvm.network

import com.smrutilipi.weatherappmvvm.features.weather_info_show.model.data_class.WeatherInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
  //  fun callApiForWeatherInfo(@Query("name") cityName: String): Call<WeatherInfoResponse>
    fun callApiForWeatherInfo(@Query("lat") latitude: Double,@Query("lon")
    longitude: Double): Call<WeatherInfoResponse>



}