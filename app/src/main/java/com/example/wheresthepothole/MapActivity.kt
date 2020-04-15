package com.example.wheresthepothole

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.media.MediaPlayer
import android.os.Handler
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wheresthepothole.networking.APIClient
import com.example.wheresthepothole.networking.NetworkService
import com.example.wheresthepothole.objects.DataReading
import com.example.wheresthepothole.objects.Pothole
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.skyfishjy.library.RippleBackground
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.osmdroid.api.IMapController
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Call
import retrofit2.Response
import java.sql.Timestamp
import java.util.*
import java.util.function.DoubleUnaryOperator
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.sin


open class MapActivity : AppCompatActivity(), SensorEventListener, OnMapReadyCallback {

    lateinit var map : MapView
    lateinit var mLocationRequest: LocationRequest
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    var latitude:Double = 0.0
    var longitude:Double = 0.0
    val potholeData : MutableList<Pothole> = ArrayList()
    lateinit var mLocationOverlay : MyLocationNewOverlay
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorGyroscope: Sensor
    private var isTripOn:Boolean = false
    private var gyrox : Double = 0.0
    private var gyroy : Double = 0.0
    private var gyroz : Double = 0.0
    private var accx : Double = 0.0
    private var accy : Double = 0.0
    private var accz : Double = 0.0
    private var speed : Double = 0.0
    var address : String = "NA"
    private var lastUpdate : Long = 0
    lateinit var handler : Handler
    lateinit var repeat_runnable : Runnable
    val apiService = APIClient.getClient().create<NetworkService>(NetworkService::class.java)
    lateinit var ring : MediaPlayer
    lateinit var warning : MediaPlayer
    lateinit var mapController : IMapController
    lateinit var prevAdd : MarkerOptions
    private lateinit var mMap: GoogleMap
    lateinit var mapFragment : SupportMapFragment
    var myMarker : Marker? = null
    lateinit var mPopupWindow: PopupWindow

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val myDataset: MutableList<String> = ArrayList()
    var pointArr : MutableList<LatLng> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        val ctx = applicationContext
        Configuration.getInstance().load(ctx,
            PreferenceManager.getDefaultSharedPreferences(ctx))
        checkPermissionForLocation(this)

        mLocationRequest = LocationRequest()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        setContentView(R.layout.activity_maps)

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val geoPoint = LatLng(latitude,longitude)

        val toolbar :Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar_main)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener{

                startActivity(Intent(this,ProfileActivity::class.java))
                finish()

        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        ring = MediaPlayer.create(this,R.raw.metoo)
        warning = MediaPlayer.create(this, R.raw.potholewarning)

        val imageRipple = findViewById<ImageView>(R.id.centerImage)
        imageRipple.visibility = View.GONE

        val call = apiService.getUserPotholes("shiv07tiwari", "existing")
        call.enqueue(object : retrofit2.Callback<ArrayList<Pothole>> {
            override fun onFailure(call: Call<ArrayList<Pothole>>?, t: Throwable?) {
                Log.e("log",t!!.message)
            }

            override fun onResponse(
                call: Call<ArrayList<Pothole>>?,
                response: Response<ArrayList<Pothole>>?
            ) {
                imageRipple.visibility = View.VISIBLE
                Log.e("locationResponse","Response")
                for (i in response!!.body()) {
                    val location = i.location.city + " " + i.location.state + " " + i.location.street
                    val time = i.time
                    myDataset.add(location + "split" + time)
                    potholeData.add(i)

                }


            }
        })

        val rippleBackground = findViewById<RippleBackground>(R.id.content)


        val ts = Timestamp(System.currentTimeMillis()).time
        handler = Handler()
        repeat_runnable = object : Runnable {
            override fun run() {
                val data = DataReading(ts.toInt(), accx, accy, accz, gyrox, gyroy, gyroz, latitude, longitude)
                val call = apiService.checkPothole(data)

                call.enqueue(object : retrofit2.Callback<String> {
                    override fun onFailure(call: Call<String>?, t: Throwable?) {
                        Log.e("log",t?.message)
                    }

                    override fun onResponse(call: Call<String>?, response: Response<String>?) {
                        Toast.makeText(applicationContext, response?.body(),Toast.LENGTH_SHORT).show()
                        if (response?.body().equals("true") || response?.body().equals("True")) {
                            ring.start()
                        } else {

                        }
                    }

                })

                handler.postDelayed(this, 4000)
            }
        }


        var mRelativeLayout: LinearLayout = findViewById(R.id.map_id)
        var mContext = this.applicationContext
        imageRipple.setOnClickListener {

            if (isTripOn) {

                isTripOn = false

                rippleBackground.stopRippleAnimation()
                stoplocationUpdates()
                disableSensors()
                stopHandler()
            } else {
                isTripOn = true

//                recyclerView = findViewById<RecyclerView>(R.id.car_list_id).apply {
//
//                    layoutManager = viewManager
//
//                    adapter = viewAdapter
//
//                }
//
//                var inflater: LayoutInflater = mContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
//
//                var customView:View = inflater.inflate(R.layout.activity_popup,null)
//
//
//
//                mPopupWindow = PopupWindow(
//                    customView,
//                    LayoutParams.WRAP_CONTENT,
//                    LayoutParams.WRAP_CONTENT
//                )
//
//
//                if(Build.VERSION.SDK_INT>=21){
//                    mPopupWindow.setElevation(5.0f)
//                }
//
//                mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER,0,0)

                rippleBackground.startRippleAnimation()
                startLocationService()
                enableSensors()
                startHandler()
            }
        }




    }



    fun startHandler() {
        handler.postDelayed(repeat_runnable, 2000)
    }

    fun stopHandler() {
        Log.e("log","Handler End")
        handler.removeCallbacks(repeat_runnable)
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val geoPoint = LatLng(latitude,longitude)
        var zoomLevel = 0.0f
        val bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.track)


        if (myMarker == null) {
            zoomLevel = 16.0f
            myMarker = mMap.addMarker(MarkerOptions().position(geoPoint).title("Marker in Sydney"))
            myMarker?.setIcon(bitmapDescriptor)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, zoomLevel))

            pointArr.add(LatLng(25.4284849, 81.7714439))
            pointArr.add(LatLng(25.4305884,81.767292))
            pointArr.add(LatLng(25.435963, 81.777748))
            pointArr.add(LatLng(25.435557, 81.766054))
            pointArr.add(LatLng(25.432981, 81.787976))

            pointArr.add(LatLng(25.426054, 81.777227))

            var pointArr2 : MutableList<LatLng> = ArrayList()
            pointArr2.add(LatLng(25.4284866, 81.7714458))
            pointArr2.add(LatLng(25.4357257, 81.7745042))

            try{
                for(startPoint in pointArr) {

                    //Log.e("test",startPoint.latitude+" "+startPoint.longitude)

                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(startPoint.latitude.toDouble(),startPoint.longitude.toDouble()))
                            .title("POTHOLE!")
                            .icon(
                                BitmapDescriptorFactory
                                    .fromResource(R.drawable.alarm)
                            )
                    )
                }
                for(startPoint in pointArr2) {

                    mMap.addMarker(
                        MarkerOptions()
                            .position(startPoint)
                            .title("Diversion")
                            .icon(
                                BitmapDescriptorFactory
                                    .fromResource(R.drawable.road)
                            )
                    )
                }
            }
            catch (e:Exception){
                Log.e("mMap Exception","Found Null---------------------")
            }
        }
        else{

            myMarker!!.position = geoPoint
            mMap.moveCamera(CameraUpdateFactory.newLatLng(geoPoint))
        }

    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor
        if (sensorType?.type == Sensor.TYPE_ACCELEROMETER) {
            val prex = accx
            val prey = accy
            val prez = accz

            accx = event.values[0].toDouble()
            accy = event.values[1].toDouble()
            accz = event.values[2].toDouble()
            val curTime = System.currentTimeMillis()

            if (curTime - lastUpdate > 200) {
                val diff = curTime - lastUpdate
                speed = abs(accx + accy + accz
                - prex - prey - prez)/diff*1000
            }

        }
        if (sensorType?.type == Sensor.TYPE_GYROSCOPE) {
            gyrox = event.values[0].toDouble()
            gyroy = event.values[1].toDouble()
            gyroz = event.values[2].toDouble()
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
                true
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    100)
                false
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have to add startlocationUpdate() method later instead of Toast
                Toast.makeText(this,"Permission granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startLocationService() {
        Log.e("log","Starting Location services")
        if (checkPermissionForLocation(this)) {
            startLocationUpdates()
        }
    }

    fun distance(lat1:Double,lon1:Double,lat2:Double,lon2:Double) : Double {
        val theta = lon1 - lon2
        var dist = Math.sin(deg2rad(lat1)) * sin(deg2rad(lat2)) + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta)))
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist)
    }

    fun deg2rad(deg:Double) : Double {
        return (deg*Math.PI / 180.0)
    }

    fun rad2deg(rad:Double) : Double {
        return (rad * 180.0 / Math.PI)
    }

    fun startLocationUpdates() {

        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 500

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
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }
    private fun stoplocationUpdates() {
        if (mFusedLocationProviderClient != null)
            mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }

    fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
//        setOnMyLocation()

        for (data in pointArr) {
            val c = distance(latitude,longitude, data.latitude, data.longitude)*100
            if (c < 25) {
                warning.start()
            }
            Log.e("Diatance",c.toString())
        }



        mapFragment.getMapAsync(this)
        var addresses = listOf<Address>()
        val geocoder = Geocoder(this, Locale.getDefault())
        addresses = geocoder.getFromLocation(latitude,longitude,1)

        address = if (address.isNotEmpty()) {
            addresses[0].getAddressLine(0) + " " + addresses[0].locality
        } else {
            "NA"
        }

        Log.e("log","Location change : "+location.latitude+" "+location.longitude)
    }


    override fun onDestroy() {
        super.onDestroy()
        stoplocationUpdates()
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
