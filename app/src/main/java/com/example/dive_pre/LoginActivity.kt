package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var vb: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // 라벨/힌트 세팅
        vb.rowId.apply {
            findViewById<android.widget.TextView>(R.id.tvLabel).text = "ID"
            findViewById<android.widget.EditText>(R.id.etValue).hint = "아이디"
        }
        vb.rowPw.apply {
            findViewById<android.widget.TextView>(R.id.tvLabel).text = "Password"
            findViewById<android.widget.EditText>(R.id.etValue).apply {
                hint = "비밀번호"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        vb.btnLogin.setOnClickListener {
            // TODO: 로그인 로직
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        vb.btnGoSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpStep1Activity::class.java))
        }
    }
}
