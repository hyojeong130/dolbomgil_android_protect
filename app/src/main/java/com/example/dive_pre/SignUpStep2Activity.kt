package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivitySignUpStep2Binding
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
import java.text.SimpleDateFormat
import java.util.*
import com.example.dive_pre.utils.TokenManager

// 피보호자 회원가입 요청 데이터 클래스
data class CareeSignUpRequest(
    val name: String,
    val gender: String,
    val birth_date: String,
    val care_level: Int
)

// 피보호자 회원가입 응답 데이터 클래스
data class CareeSignUpResponse(
    val success: Boolean,
    val message: String,
    val caree: CareeData,
    val registration_code: String
)

data class CareeData(
    val caree_id: Int,
    val name: String,
    val gender: String,
    val birth_date: String,
    val pairing_status: String,
    val watch_device_id: String,
    val watch_device_token: String,
    val care_level: Int,
    val created_by_user_id: String
)

class SignUpStep2Activity : AppCompatActivity() {
    private lateinit var vb: ActivitySignUpStep2Binding
    
    companion object {
        private const val TAG = "SignUpStep2Activity"
        private val BASE_URL = BuildConfig.API_BASE_URL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivitySignUpStep2Binding.inflate(layoutInflater)
        setContentView(vb.root)

        // 라벨 세팅
        vb.rowName.tvLabel.text = "Name"
        vb.rowName.etValue.hint = "이름을 입력하세요"

        vb.rowBirth.tvLabel.text = "Birth"
        vb.rowBirth.etValue.hint = "0000.00.00"

        vb.rowGender.tvLabel.text = "Gender"
        setupGenderRadioButtons()

        vb.rowCareLevel.tvLabel.text = "Care Level"
        vb.rowCareLevel.etValue.hint = "돌봄 수준을 선택하세요"
        

        // Complete 버튼 → 피보호자 회원가입 API 호출
        vb.btnComplete.setOnClickListener {
            performCareeSignUp()
        }
    }
    
    private fun setupGenderRadioButtons() {
        // 남성 선택 시
        vb.rowGender.llMale.setOnClickListener {
            updateGenderSelection(true)
        }
        
        // 여성 선택 시
        vb.rowGender.llFemale.setOnClickListener {
            updateGenderSelection(false)
        }
        
        // 기본값으로 남성 선택
        updateGenderSelection(true)
    }
    
    private fun updateGenderSelection(isMale: Boolean) {
        if (isMale) {
            // 남성 선택
            vb.rowGender.ivMaleIndicator.setBackgroundResource(R.drawable.bg_radio_selected)
            vb.rowGender.ivFemaleIndicator.setBackgroundResource(R.drawable.bg_radio_unselected)
        } else {
            // 여성 선택
            vb.rowGender.ivMaleIndicator.setBackgroundResource(R.drawable.bg_radio_unselected)
            vb.rowGender.ivFemaleIndicator.setBackgroundResource(R.drawable.bg_radio_selected)
        }
    }
    
    private fun performCareeSignUp() {
        // 입력값 검증
        val name = vb.rowName.etValue.text.toString().trim()
        val birthText = vb.rowBirth.etValue.text.toString().trim()
        val careLevelText = vb.rowCareLevel.etValue.text.toString().trim()
        
        if (name.isEmpty() || birthText.isEmpty() || careLevelText.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 생년월일 형식 검증 (YYYY-MM-DD)
        val birthDate = parseBirthDate(birthText)
        if (birthDate == null) {
            Toast.makeText(this, "생년월일을 YYYY-MM-DD 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 돌봄 수준 검증 (1-3)
        val careLevel = careLevelText.toIntOrNull()
        if (careLevel == null || careLevel < 1 || careLevel > 3) {
            Toast.makeText(this, "돌봄 수준을 1-3 사이의 숫자로 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 성별 값 가져오기 (현재 선택된 상태 확인)
        val isMaleSelected = vb.rowGender.ivMaleIndicator.background.constantState == 
            resources.getDrawable(R.drawable.bg_radio_selected, theme).constantState
        val validGender = if (isMaleSelected) "male" else "female"
        
        // 피보호자 회원가입 API 호출
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = careeSignUpApi(name, validGender, birthDate, careLevel)
                if (result.success) {
                    Toast.makeText(this@SignUpStep2Activity, "피보호자 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    // 등록 코드를 표시하거나 다음 화면으로 이동
                    showRegistrationSuccess(result.registration_code)
                } else {
                    Toast.makeText(this@SignUpStep2Activity, "피보호자 등록 실패: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "피보호자 등록 오류", e)
                Toast.makeText(this@SignUpStep2Activity, "피보호자 등록 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun parseBirthDate(birthText: String): String? {
        return try {
            // 다양한 형식 지원 (YYYY.MM.DD, YYYY-MM-DD, YYYY/MM/DD)
            val formats = listOf("yyyy.MM.dd", "yyyy-MM-dd", "yyyy/MM/dd")
            var parsedDate: Date? = null
            
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    parsedDate = sdf.parse(birthText)
                    if (parsedDate != null) break
                } catch (e: Exception) {
                    continue
                }
            }
            
            if (parsedDate != null) {
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                outputFormat.format(parsedDate)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun showRegistrationSuccess(registrationCode: String) {
        // 등록 성공 시 등록 코드를 표시하고 홈 화면으로 이동
        Toast.makeText(this, "피보호자 등록이 완료되었습니다. 등록 코드: $registrationCode", Toast.LENGTH_LONG).show()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finishAffinity() // 회원가입 스텝 액티비티 전부 종료
    }
    
    private suspend fun careeSignUpApi(name: String, gender: String, birthDate: String, careLevel: Int): CareeSignUpResponse {
        return withContext(Dispatchers.IO) {
            val accessToken = getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                throw Exception("액세스 토큰이 없습니다. 다시 로그인해주세요.")
            }
            
            val client = OkHttpClient()
            
            val requestBody = JSONObject().apply {
                put("name", name)
                put("gender", gender)
                put("birth_date", birthDate)
                put("care_level", careLevel)
            }.toString()
            
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBody.toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("$BASE_URL/api/caree/register")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken") // 액세스 토큰 추가
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("응답이 비어있습니다")
            
            if (!response.isSuccessful) {
                throw Exception("HTTP 오류: ${response.code}")
            }
            
            val gson = Gson()
            gson.fromJson(responseBody, CareeSignUpResponse::class.java)
        }
    }
    
    private fun getAccessToken(): String? {
        return TokenManager.getAccessToken(this)
    }
}
