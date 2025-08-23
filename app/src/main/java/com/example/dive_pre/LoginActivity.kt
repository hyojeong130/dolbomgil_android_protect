package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivityLoginBinding
import com.example.dive_pre.databinding.ViewInputRowBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var vb: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // 재사용 row에 라벨/힌트/입력 타입 설정
        setupRow(vb.rowId, label = "ID", hint = "")
        setupRow(vb.rowPw, label = "Password", hint = "", isPassword = true)


        // 로그인
        vb.btnLogin.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        // 상단 우측 Sign Up 텍스트
        vb.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpStep1Activity::class.java))
        }
    }

    private fun setupRow(binding: ViewInputRowBinding, label: String, hint: String, isPassword: Boolean = false) {
        binding.tvLabel.text = label
        binding.etValue.apply {
            this.hint = hint
            if (isPassword) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                inputType = InputType.TYPE_CLASS_TEXT
            }
        }
    }

}
