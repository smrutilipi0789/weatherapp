package com.smrutilipi.weatherappmvvm.features.weather_info_show.model.data_class

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class City(
        @SerializedName("id")
        val id: Int = 0,
        @SerializedName("lat")
        val lat: Double = 0.0,
        @SerializedName("lon")
        val long: Double = 0.0,
        @SerializedName("name")
        var name: String = "",
        @SerializedName("country")
        val country: String = ""
): Serializable