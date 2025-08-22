package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var vb: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.btnOpenNavi.setOnClickListener {
            startActivity(Intent(this, NaviActivity::class.java))
        }
//        vb.btnSettings.setOnClickListener {
//            startActivity(Intent(this, SettingsActivity::class.java))
//        }
    }
}
