package com.example.wheresthepothole

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_sensor.*
import java.util.*


class SensorActivity : AppCompatActivity(), SensorEventListener {

    var isTripOn : Boolean = false
    lateinit var sensorManager: SensorManager
    lateinit var sensorAccelerometer: Sensor
    lateinit var sensorGyroscope: Sensor
    var tripTimer: Int = 0
    var potHoleCounter: Int = 0
    private val REQUEST_PERMISSION_LOCATION = 10
    internal lateinit var mLocationRequest: LocationRequest
    var latitude:Double = 0.0
    var longitude:Double = 0.0
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var countDownTimer:CountDownTimer
    lateinit var geocoder: Geocoder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

        mLocationRequest = LocationRequest()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


        btn_start.setOnClickListener{
            startNewTrip()
        }

    }

    fun startNewTrip() {
        Log.e("log","Starting new trip")

        if (isTripOn) {
            isTripOn = false
            btn_start.text = "Start"
//            text_intro.text = "Start the trip to store the data"
//            TripDesc.text = "Trip is Off"
            disableSensors()
            stopLocationService()
            countDownTimer.onFinish()
            countDownTimer.cancel()
            return
        }
        tripTimer = 0
        isTripOn = true
        btn_start.text = "Trip On"
//        TripDesc.text = "Trip is On"
//        text_intro.text = "Stop the trip to send the data"
        countDownTimer = object : CountDownTimer(50000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                txt_trip_duration.text = "Trip time : "+tripTimer+" seconds"
                tripTimer++
            }

            override fun onFinish() {
                txt_trip_duration.text = "Last trip finished in "+tripTimer+" seconds"
            }
        }.start()
        enableSensors()
        startLocationService()
    }

    fun startLocationService() {
        Log.e("log","Starting Location services")
        if (checkPermissionForLocation(this)) {
            startLocationUpdates()
        }
    }

    fun stopLocationService() {
        Log.e("log","Stopping location service")
        stoplocationUpdates()
    }

    protected fun startLocationUpdates() {

        // Create the location request to start receiving updates

        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 500

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // do work here
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }
    private fun stoplocationUpdates() {
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }

    fun onLocationChanged(location: Location) {
        // New location has now been determined
        latitude = location.latitude
        longitude = location.longitude
        Log.e("log","Location change : "+location.latitude+" "+location.longitude)

        // You can now create a LatLng Object for use with maps
    }


    fun enableSensors() {
        Log.e("log","Sensors enabled")

        sensorManager.registerListener(this,sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,sensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun disableSensors() {
        Log.e("log","Sensors disabled")

        sensorManager.unregisterListener(this, sensorAccelerometer)
        sensorManager.unregisterListener(this,sensorGyroscope)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Not of any use
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        val mySensor = p0?.sensor

        if (mySensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = p0.values[0]
            val y = p0.values[1]
            val z = p0.values[2]


            if (tripTimer%10 == 0) {

            }

        }

        if (mySensor?.type == Sensor.TYPE_GYROSCOPE) {
            val x = p0.values[0]
            val y = p0.values[1]
            val z = p0.values[2]
            if (tripTimer%10 ==0) {
//                Log.e("log","Gyroscope data : "+x+" "+y+" "+z+" "+latitude+" "+longitude)

                var addresses : List<Address>

                geocoder = Geocoder(this, Locale.getDefault())

                addresses = geocoder.getFromLocation(latitude, longitude, 1)

                var gyroData = ArrayList<String>()

                gyroData?.add(x.toString())
                gyroData?.add(y.toString())
                gyroData?.add(z.toString())
                gyroData?.add(latitude.toString())
                gyroData?.add(longitude.toString())
                gyroData?.add(addresses.get(0).featureName.toString())
                gyroData?.add(addresses.get(0).locality.toString())
                gyroData?.add(addresses.get(0).adminArea.toString())
                gyroData?.add(addresses.get(0).postalCode)
                gyroData?.add(addresses.get(0).countryName.toString())
//                gyroData?.add(addresses.get(0).getAddressLine(0).toString())

                Log.e("log","Gyroscope data: "+ gyroData)
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
                true
            }else{
                // Show the permission request
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have to add startlocationUpdate() method later instead of Toast
                Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , 11)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()


    }

}