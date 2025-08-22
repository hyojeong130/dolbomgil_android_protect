package com.example.dive_pre

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.example.dive_pre.databinding.ActivitySignUpStep1Binding
import com.example.dive_pre.databinding.ViewInputRowBinding

class SignUpStep1Activity : AppCompatActivity() {

    private lateinit var vb: ActivitySignUpStep1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivitySignUpStep1Binding.inflate(layoutInflater)
        setContentView(vb.root)

        // 각 줄(row) 세팅
        vb.rowName.setupRow(label = "Name",  inputType = InputType.TYPE_CLASS_TEXT,       hint = "이름 입력")
        vb.rowPhone.setupRow(label = "Phone", inputType = InputType.TYPE_CLASS_PHONE,      hint = "010-0000-0000")
        vb.rowId.setupRow(label   = "ID",    inputType = InputType.TYPE_CLASS_TEXT,       hint = "아이디 입력")
        vb.rowPw.setupRow(label   = "Password",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            hint = "비밀번호 입력")

        // 버튼
        vb.btnBack.setOnClickListener { finish() }
        vb.btnNext.setOnClickListener {
            startActivity(Intent(this, SignUpStep2Activity::class.java))
        }
    }
}

/** 재사용 가능한 Row 설정용 확장함수 */
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
