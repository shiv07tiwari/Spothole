package com.example.wheresthepothole

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_popup.*

class CarActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val myDataset: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup)

        myDataset.add("Maruti 800splitVERY HIGH")
        myDataset.add("ZestsplitHIGH")
        myDataset.add("Vitara BreezasplitLOW")
        myDataset.add("XUVsplitVERY LOW")
        myDataset.add("ScorpiosplitVERY LOW")
        myDataset.add("WagonRsplitHIGH")
        myDataset.add("Swift DeziresplitMEDIUM")
        myDataset.add("HummersplitVERY LOW")

        lalla.setOnClickListener {
            startActivity(Intent(this,MapActivity::class.java))
        }
        viewManager = LinearLayoutManager(this)
        viewAdapter = ProfileAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.car_list_id).apply {

                    layoutManager = viewManager

                    adapter = viewAdapter

                }

    }
}