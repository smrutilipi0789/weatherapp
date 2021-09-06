package com.smrutilipi.weatherappmvvm.features.weather_info_show.model

import com.smrutilipi.weatherappmvvm.common.RequestCompleteListener
import com.smrutilipi.weatherappmvvm.features.weather_info_show.model.data_class.City
import com.smrutilipi.weatherappmvvm.features.weather_info_show.model.data_class.WeatherInfoResponse

interface WeatherInfoShowModel {
    fun getCityList(callback: RequestCompleteListener<MutableList<City>>)
    fun getWeatherInfo(citylat: Double,citylong: Double,callback: RequestCompleteListener<WeatherInfoResponse>)

}