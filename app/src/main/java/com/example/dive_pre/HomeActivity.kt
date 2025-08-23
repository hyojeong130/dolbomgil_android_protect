package com.example.dive_pre

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // HomeActivity.kt (onCreate 안)
        val cardAuto = findViewById<FrameLayout>(R.id.cardAuto)
        val tvToggle = findViewById<TextView>(R.id.tvToggle)

// 초기 상태: ON
        var isOn = true
        cardAuto.setBackgroundResource(R.drawable.bg_card_auto_on)
        tvToggle.text = "ON"

// 토글 클릭으로 ON/OFF 전환
        findViewById<android.view.View>(R.id.toggleGroup).setOnClickListener {
            isOn = !isOn
            if (isOn) {
                cardAuto.setBackgroundResource(R.drawable.bg_card_auto_on)
                tvToggle.text = "ON"
            } else {
                cardAuto.setBackgroundResource(R.drawable.bg_card_auto_off)
                tvToggle.text = "OFF"
            }
        }

    }

}
