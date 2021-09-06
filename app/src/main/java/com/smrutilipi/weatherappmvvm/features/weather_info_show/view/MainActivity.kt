package com.smrutilipi.weatherappmvvm.features.weather_info_show.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.smrutilipi.weatherappmvvm.R
import com.smrutilipi.weatherappmvvm.features.weather_info_show.model.WeatherInfoShowModel
import com.smrutilipi.weatherappmvvm.features.weather_info_show.model.WeatherInfoShowModelImpl
import com.smrutilipi.weatherappmvvm.features.weather_info_show.model.data_class.City
import com.smrutilipi.weatherappmvvm.features.weather_info_show.model.data_class.WeatherData
import com.smrutilipi.weatherappmvvm.features.weather_info_show.viewmodel.WeatherInfoViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_input_part.*
import kotlinx.android.synthetic.main.layout_sunrise_sunset.*
import kotlinx.android.synthetic.main.layout_weather_additional_info.*
import kotlinx.android.synthetic.main.layout_weather_basic_info.*
import java.io.IOException
import java.util.*
import android.location.Geocoder
import android.util.Log


class MainActivity : AppCompatActivity() {

    private lateinit var city: City
    private lateinit var geocoder:Geocoder
    private val PERMISSION_REQUEST_ACCESS_FINE_LOCATION:Int = 100
    private lateinit var addressField: String
//    val appContext:Context = applicationContext
    private lateinit var model: WeatherInfoShowModel
    private lateinit var viewModel: WeatherInfoViewModel
   private  lateinit var locationManager : LocationManager
   private lateinit var locationListener : LocationListener
    private var cityList: MutableList<City> = mutableListOf()
     private  var addresses: List<Address>?=null
     var  lat: Double=0.0
      var long: Double=0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize model. (I know we should not initialize model in View. But for simplicity...)
        model = WeatherInfoShowModelImpl(applicationContext)
        // initialize ViewModel
        viewModel = ViewModelProviders.of(this).get(WeatherInfoViewModel::class.java)



        // set LiveData and View click listeners before the call for data fetching
        setLocation()
        setLiveDataListeners()
        setViewClickListener()


        /**
         * Fetch city list when Activity open.
         * It's not a very good way that, passing model in every methods of ViewModel. For the sake
         * of simplicity I did so. In real production level App, we can inject out model to ViewModel
         * as a parameter by any dependency injection library like Dagger.
         */
        viewModel.getCityList(model)
    }

    private fun setLocation() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
         {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
            return
        }

        else{
            var LocationGps:Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            var LocationNetwork:Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            var LocationPassive:Location? = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            when {
                LocationGps != null -> {
                     lat = LocationGps.getLatitude();
                     long = LocationGps.getLongitude();
                    getTheAddress(lat, long);
                }
                LocationNetwork != null -> {
                     lat = LocationNetwork.getLatitude();
                     long = LocationNetwork.getLongitude();
                    getTheAddress(lat, long);
                }
                LocationPassive != null -> {
                     lat = LocationPassive.getLatitude();
                     long= LocationPassive.getLongitude();
                    getTheAddress(lat, long);
                }
                else -> {
                    Toast.makeText(applicationContext, "Can't Get Your Location", Toast.LENGTH_SHORT).show();
                }
            }

        }

    }

    private fun getTheAddress(lat: Double, long: Double) {

        geocoder = Geocoder(applicationContext, Locale.getDefault())
        addresses = geocoder.getFromLocation(lat, long, 1)


        try {
        //    addresses = geocoder.getFromLocation(lat, long, 1)
            var address = (addresses as MutableList<Address>?)!![0].getAddressLine(0)
          //  val city = City(,addresses[0].locality,addresses[0].countryName)
            val name= (addresses as MutableList<Address>?)!![0].locality
            val state = (addresses as MutableList<Address>?)!![0].adminArea
            val country = (addresses as MutableList<Address>?)!![0].countryName
            val postalCode = (addresses as MutableList<Address>?)!![0].postalCode
            val knownName = (addresses as MutableList<Address>?)!![0].featureName
            Log.d("neel", address)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
                when (grantResults[0]) {
                    PackageManager.PERMISSION_GRANTED -> setLocation()
                   // PackageManager.PERMISSION_DENIED -> //Tell to user the need of grant permission
                }
            }
        }

        companion object {
            private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
        }


    private fun setViewClickListener() {
        // View Weather button click listener
        btn_view_weather.setOnClickListener {
          //  val selectedCityId = cityList[spinner.selectedItemPosition].id
            var city = City(0,lat,long, addresses!![0].locality, addresses!![0].countryName)
            cityList.add(city)
            val selectedCitylat = cityList[0].lat
            val selectedCitylong = cityList[0].long
          //  viewModel.getWeatherInfo(selectedCityId, model) // fetch weather info
            viewModel.getWeatherInfo(selectedCitylat!!,selectedCitylong!!, model) // fetch weather info
        }
    }

    private fun setLiveDataListeners() {

        /**
         * When ViewModel PUSH city list to LiveData then this `onChanged()`‍ method will be called.
         * Here we subscribe the LiveData of City list. We don't pull city list from ViewModel.
         * We subscribe to the data source for city list. When LiveData of city list is updated
         * inside ViewModel, below onChanged() method will triggered instantly.
         * City list is fetching from a small local JSON file. So we don't need any ProgressBar here.
         *
         * For better understanding, I didn't use lambda in this method call. Rather thant lambda I
         * implement `Observer` interface in general format. Hope you will understand the inline
         * implementation of `Observer` interface. Rest of the `observe()` method, I've used lambda
         * to short the code.
         */


        /**
         * If ViewModel failed to fetch City list from data source, this LiveData will be triggered.
         * I know it's not good to make separate LiveData both for Success and Failure, but for sake
         * of simplification I did it. We can handle all of our errors from our Activity or Fragment
         * Base classes. Another way is: using a Generic wrapper class where you can set the success
         * or failure status for any types of data model.
         *
         * Here I've used lambda expression to implement Observer interface in second parameter.
         */
        viewModel.cityListFailureLiveData.observe(this, Observer { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        })

        /**
         * ProgressBar visibility will be handled by this LiveData. ViewModel decides when Activity
         * should show ProgressBar and when hide.
         *
         * Here I've used lambda expression to implement Observer interface in second parameter.
         */
        viewModel.progressBarLiveData.observe(this, Observer { isShowLoader ->
            if (isShowLoader)
                progressBar.visibility = View.VISIBLE
            else
                progressBar.visibility = View.GONE
        })

        /**
         * This method will be triggered when ViewModel successfully receive WeatherData from our
         * data source (I mean Model). Activity just observing (subscribing) this LiveData for showing
         * weather information on UI. ViewModel receives Weather data API response from Model via
         * Callback method of Model. Then ViewModel apply some business logic and manipulate data.
         * Finally ViewModel PUSH WeatherData to `weatherInfoLiveData`. After PUSHING into it, below
         * method triggered instantly! Then we set the data on UI.
         *
         * Here I've used lambda expression to implement Observer interface in second parameter.
         */
        viewModel.weatherInfoLiveData.observe(this, Observer { weatherData ->
            setWeatherInfo(weatherData)
        })

        /**
         * If ViewModel faces any error during Weather Info fetching API call by Model, then PUSH the
         * error message into `weatherInfoFailureLiveData`. After that, this method will be triggered.
         * Then we will hide the output view and show error message on UI.
         *
         * Here I've used lambda expression to implement Observer interface in second parameter.
         */
        viewModel.weatherInfoFailureLiveData.observe(this, Observer { errorMessage ->
            output_group.visibility = View.GONE
            tv_error_message.visibility = View.VISIBLE
            tv_error_message.text = errorMessage
        })
    }



    private fun setWeatherInfo(weatherData: WeatherData) {
        output_group.visibility = View.VISIBLE
        tv_error_message.visibility = View.GONE

        tv_date_time?.text = weatherData.dateTime
        tv_temperature?.text = weatherData.temperature
        tv_city_country?.text = weatherData.cityAndCountry
        Glide.with(this).load(weatherData.weatherConditionIconUrl).into(iv_weather_condition)
        tv_weather_condition?.text = weatherData.weatherConditionIconDescription

        tv_humidity_value?.text = weatherData.humidity
        tv_pressure_value?.text = weatherData.pressure
        tv_visibility_value?.text = weatherData.visibility

        tv_sunrise_time?.text = weatherData.sunrise
        tv_sunset_time?.text = weatherData.sunset
    }




}
