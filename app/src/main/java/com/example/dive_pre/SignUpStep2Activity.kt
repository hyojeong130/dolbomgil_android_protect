package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivitySignUpStep2Binding

class SignUpStep2Activity : AppCompatActivity() {
    private lateinit var vb: ActivitySignUpStep2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivitySignUpStep2Binding.inflate(layoutInflater)
        setContentView(vb.root)

        // 라벨 세팅
        vb.rowName.tvLabel.text = "Name"

        vb.rowId.tvLabel.text = "ID"
        vb.rowId.tvLabel.hint="영어 + 숫자"

        vb.rowBirth.tvLabel.text = "Birth"
        vb.rowBirth.tvLabel.hint="0000.00.00"

        // Complete 버튼 → HomeActivity로 이동
        vb.btnComplete.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity() // 회원가입 스텝 액티비티 전부 종료
        }
    }
}
