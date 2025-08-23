package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.api.ApiService
import com.example.dive_pre.databinding.ActivityLoginBinding
import com.example.dive_pre.databinding.ViewInputRowBinding
import com.example.dive_pre.utils.FCMUtils
import com.example.dive_pre.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var vb: ActivityLoginBinding
    
    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // 재사용 row에 라벨/힌트/입력 타입 설정
        setupRow(vb.rowId, label = "ID", hint = "")
        setupRow(vb.rowPw, label = "Password", hint = "", isPassword = true)


        // 로그인
        vb.btnLogin.setOnClickListener {
            performLogin()
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
    
    private fun performLogin() {
        val userId = vb.rowId.etValue.text.toString().trim()
        val password = vb.rowPw.etValue.text.toString().trim()
        
        if (userId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val fcmToken = FCMUtils.getFCMToken()
                if (fcmToken == null) {
                    Toast.makeText(this@LoginActivity, "FCM 토큰을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                val loginResponse = ApiService.login(userId, password, fcmToken)
                
                if (loginResponse.success) {
                    TokenManager.saveAccessToken(this@LoginActivity, loginResponse.access_token)
                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "로그인 실패: ${loginResponse.message}", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "로그인 오류", e)
                Toast.makeText(this@LoginActivity, "로그인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
