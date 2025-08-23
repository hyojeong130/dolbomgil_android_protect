package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivitySignUpStep1Binding

class SignUpStep1Activity : AppCompatActivity() {
    private lateinit var vb: ActivitySignUpStep1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivitySignUpStep1Binding.inflate(layoutInflater)
        setContentView(vb.root)

        // 타이틀/역할 문구
        vb.tvTitle.text = "Sign Up"
        vb.tvRole.text = "For 보호자용"

        // 각 입력 라벨/힌트/타입 세팅 (view_input_row 재사용)
        vb.rowName.tvLabel.text = "Name"
        vb.rowName.etValue.hint = ""

        vb.rowPhone.tvLabel.text = "Phone"
        vb.rowPhone.etValue.apply {
            hint = "010-0000-0000"
            inputType = InputType.TYPE_CLASS_PHONE
        }

        vb.rowId.tvLabel.text = "ID"
        vb.rowId.etValue.hint = "영어 + 숫자"

        vb.rowPw.tvLabel.text = "Password"
        vb.rowPw.etValue.apply {
            hint = "영어 + 숫자 + 특수문자"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        // 상단 왼쪽 뒤로가기: 로그인 화면으로 복귀
        vb.ibBack.setOnClickListener { finish() } // 로그인에서 들어왔으니 finish()면 복귀

        // 다음 단계 이동
        vb.btnNext.setOnClickListener {
            startActivity(Intent(this, SignUpStep2Activity::class.java))
        }
    }
}
