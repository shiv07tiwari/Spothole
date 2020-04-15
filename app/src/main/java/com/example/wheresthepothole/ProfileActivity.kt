package com.example.wheresthepothole

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wheresthepothole.networking.APIClient
import com.example.wheresthepothole.networking.NetworkService
import com.example.wheresthepothole.objects.Pothole
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Response
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.io.Serializable


class ProfileActivity : AppCompatActivity(){
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val myDataset: MutableList<String> = ArrayList()
    val potholeData : MutableList<String> = ArrayList()
    lateinit var drawerLayout : DrawerLayout
    val apiService = APIClient.getClient().create<NetworkService>(NetworkService::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var toolbar : Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.open_menu)


        drawerLayout  = findViewById(R.id.drawer_layout)
        var navView : NavigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener{item ->
            when(item.itemId) {
                R.id.trip_id -> {
                    val i = Intent(this,CarActivity::class.java)
                    startActivity(i)

                }
                R.id.invite_id -> {
                }
                R.id.logout_id -> {

                }
                R.id.contri_id -> {
                    val i = Intent(this,RewardActivity::class.java)
                    startActivity(i)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        val call = apiService.getUserPotholes("shiv07tiwari", "existing")
        call.enqueue(object : retrofit2.Callback<ArrayList<Pothole>> {
            override fun onFailure(call: Call<ArrayList<Pothole>>?, t: Throwable?) {
                Log.e("log",t!!.message)
            }

            override fun onResponse(
                call: Call<ArrayList<Pothole>>?,
                response: Response<ArrayList<Pothole>>?
            ) {
                Log.e("log","Response")
                for (i in response!!.body()) {
                    val location = i.location.city + " " + i.location.state + " " + i.location.street
                    val time = i.time
                    myDataset.add(location + "split" + time)
                    potholeData.add(i.latitude+"split"+i.longitude)
                }
                viewAdapter = ProfileAdapter(myDataset)
                viewManager = LinearLayoutManager(applicationContext)

                recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {

                    layoutManager = viewManager

                    adapter = viewAdapter

                }
            }
        })
    }




}