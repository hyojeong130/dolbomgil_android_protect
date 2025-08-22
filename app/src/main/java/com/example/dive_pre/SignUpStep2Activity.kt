package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivitySignUpStep2Binding
import com.example.dive_pre.databinding.ViewInputRowBinding

class SignUpStep2Activity : AppCompatActivity() {

    private lateinit var vb: ActivitySignUpStep2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivitySignUpStep2Binding.inflate(layoutInflater)
        setContentView(vb.root)

        // 행 세팅
        vb.rowName.setupRow(
            label = "Name",
            inputType = InputType.TYPE_CLASS_TEXT,
            hint = "이름 입력"
        )
        vb.rowId.setupRow(
            label = "ID",
            inputType = InputType.TYPE_CLASS_TEXT,
            hint = "아이디 입력"
        )
        vb.rowBirth.setupRow(
            label = "Birth",
            // 날짜 입력은 별도 변형 상수가 없어 보통 숫자/날짜 조합으로 처리
            inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE,
            hint = "YYYY.MM.DD"
        )

        // 버튼
        vb.btnBack.setOnClickListener { finish() }
        vb.btnComplete.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity() // 회원가입 플로우 백스택 정리
        }
    }
}

/** 재사용 가능한 입력 Row 설정용 확장함수 (Step1과 동일) */
private fun ViewInputRowBinding.setupRow(
    label: String,
    inputType: Int? = null,
    text: String? = null,
    hint: String? = null
) {
    tvLabel.text = label
    inputType?.let { etValue.inputType = it }
    text?.let { etValue.setText(it) }
    hint?.let { etValue.hint = it }
}
