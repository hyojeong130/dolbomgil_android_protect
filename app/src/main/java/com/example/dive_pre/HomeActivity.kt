package com.example.dive_pre

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivityHomeBinding
import com.example.dive_pre.databinding.ViewCardAutoBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var vb: ActivityHomeBinding
    private lateinit var autoCardBinding: ViewCardAutoBinding   // nullable 아님

    private var isAutoOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // ⬇️ bind() 호출 없이 그대로 사용
        autoCardBinding = vb.includeAuto

        renderAutoCard()

        autoCardBinding.toggleGroup.setOnClickListener {
            isAutoOn = !isAutoOn
            renderAutoCard()
        }
    }

    private fun renderAutoCard() = with(autoCardBinding) {
        ivToggleOuter.setBackgroundResource(
            if (isAutoOn) R.drawable.bg_toggle_outer_on else R.drawable.bg_toggle_outer_off
        )
        tvToggle.setBackgroundResource(
            if (isAutoOn) R.drawable.bg_toggle_inner_on else R.drawable.bg_toggle_inner_off
        )
        tvToggle.text = if (isAutoOn) "ON" else "OFF"
        ivDot.setBackgroundResource(R.drawable.bg_status_dot)
        tvStatus.text = if (isAutoOn) "활성화" else "비활성화"
    }
}
