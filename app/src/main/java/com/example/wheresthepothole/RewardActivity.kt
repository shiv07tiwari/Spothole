package com.example.wheresthepothole

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.app_bar_main.*

class RewardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)


        val toolbar : Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar_main)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener{

            startActivity(Intent(this,ProfileActivity::class.java))
            finish()

        }

    }

}