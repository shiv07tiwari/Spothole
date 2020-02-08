package com.example.wheresthepothole

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.view.menu.MenuBuilder
import android.view.Menu





class ProfileActivity : AppCompatActivity(){
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val myDataset: MutableList<String> = ArrayList()
    lateinit var drawerLayout : DrawerLayout

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
                    startActivity( Intent(this,MapActivity::class.java))
                }
                R.id.invite_id -> {
                }
                R.id.logout_id -> {

                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        myDataset.add("You reported pothole at this Address")
        myDataset.add("You reported pothole at this Address")

        myDataset.add("You reported pothole at this Address")

        myDataset.add("You reported pothole at this Address")

        myDataset.add("You reported pothole at this Address")
        myDataset.add("You reported pothole at this Address")
        myDataset.add("You reported pothole at this Address")
        myDataset.add("You reported pothole at this Address")


        viewManager = LinearLayoutManager(this)
        viewAdapter = ProfileAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {

            setHasFixedSize(true)

            layoutManager = viewManager

            adapter = viewAdapter

        }

    }




}