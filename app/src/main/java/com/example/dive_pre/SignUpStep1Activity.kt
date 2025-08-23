package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivitySignUpStep1Binding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// 회원가입 요청 데이터 클래스
data class SignUpRequest(
    val user_id: String,
    val name: String,
    val phone_number: String,
    val password: String
)

// 회원가입 응답 데이터 클래스
data class SignUpResponse(
    val success: Boolean,
    val message: String,
    val user: UserData
)

data class UserData(
    val user_id: String,
    val name: String,
    val phone_number: String
)

class SignUpStep1Activity : AppCompatActivity() {
    private lateinit var vb: ActivitySignUpStep1Binding
    
    companion object {
        private const val TAG = "SignUpStep1Activity"
        private val BASE_URL = BuildConfig.API_BASE_URL
    }

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
            performSignUp()
        }
    }
    
    private fun performSignUp() {
        // 입력값 검증
        val name = vb.rowName.etValue.text.toString().trim()
        val phone = vb.rowPhone.etValue.text.toString().trim()
        val userId = vb.rowId.etValue.text.toString().trim()
        val password = vb.rowPw.etValue.text.toString().trim()
        
        if (name.isEmpty() || phone.isEmpty() || userId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 회원가입 API 호출
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = signUpApi(userId, name, phone, password)
                if (result.success) {
                    Toast.makeText(this@SignUpStep1Activity, "보호자 회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    // 보호자 회원가입 성공 시 로그인 화면으로 이동
                    startActivity(Intent(this@SignUpStep1Activity, LoginActivity::class.java))
                    finishAffinity() // 회원가입 관련 액티비티 모두 종료
                } else {
                    Toast.makeText(this@SignUpStep1Activity, "회원가입 실패: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "회원가입 오류", e)
                Toast.makeText(this@SignUpStep1Activity, "회원가입 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private suspend fun signUpApi(userId: String, name: String, phone: String, password: String): SignUpResponse {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            
            val requestBody = JSONObject().apply {
                put("user_id", userId)
                put("name", name)
                put("phone_number", phone)
                put("password", password)
            }.toString()
            
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBody.toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("$BASE_URL/api/user/register")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("응답이 비어있습니다")
            
            if (!response.isSuccessful) {
                throw Exception("HTTP 오류: ${response.code}")
            }
            
            val gson = Gson()
            gson.fromJson(responseBody, SignUpResponse::class.java)
        }
    }
}
