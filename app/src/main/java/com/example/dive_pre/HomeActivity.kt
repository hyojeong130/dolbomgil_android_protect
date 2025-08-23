package com.example.dive_pre


import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.TransitionManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.example.dive_pre.databinding.ActivityHomeBinding
import com.example.dive_pre.databinding.ViewCardAutoBinding
import com.example.dive_pre.databinding.ViewCardLocationBinding
import com.example.dive_pre.databinding.ViewCardSettingBinding
import com.example.dive_pre.databinding.ViewCardGuideBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var vb: ActivityHomeBinding
    private lateinit var autoCardBinding: ViewCardAutoBinding
    private lateinit var locationCard: ViewCardLocationBinding
    private lateinit var settingsCard: ViewCardSettingBinding
    private lateinit var guideCard: ViewCardGuideBinding

    // 데모 상태값
    private var isAutoOn = true
    private var inSafeZone = true
    private var radiusMeters = 100
    private var targetName = "김다무"
    private var targetLat = 35.2329
    private var targetLng = 129.0827

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vb = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // include -> 바인딩 (activity_home.xml에서 include의 id가 이 이름이어야 함)
        autoCardBinding = vb.includeAuto
        locationCard    = vb.includeLocation
        settingsCard    = vb.includeSettings
        guideCard       = vb.includeGuide

        // 초기 렌더
        renderAutoCard()
        renderLocationCard()
        renderSettingsCard()
        renderGuideCard()

        // 리스너
        autoCardBinding.toggleGroup.setOnClickListener {
            isAutoOn = !isAutoOn
            renderAutoCard()
        }

        // 위치 카드: 전체 클릭으로 상태 토글(데모)
        locationCard.root.setOnClickListener {
            inSafeZone = !inSafeZone
            renderLocationCard()
            renderGuideCard()
        }


        // 설정 카드: 반경 토글(데모)
        settingsCard.root.setOnClickListener {
            radiusMeters = if (radiusMeters == 100) 200 else 100
            renderSettingsCard()
        }

        // 길안내 카드: 지도 열기
        guideCard.root.setOnClickListener {
            val uri = Uri.parse("geo:$targetLat,$targetLng?q=$targetLat,$targetLng($targetName)")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        vb.btnMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }


    private fun renderAutoCard() = with(autoCardBinding) {
        // 1) 카드 전체 배경(부모 FrameLayout: activity_home.xml의 @id/cardAuto)
        vb.cardAuto.setBackgroundResource(
            if (isAutoOn) R.drawable.bg_card_auto_on else R.drawable.bg_card_auto_off
        )

        // 2) 토글 비주얼
        ivToggleOuter.setBackgroundResource(
            if (isAutoOn) R.drawable.bg_toggle_outer_on else R.drawable.bg_toggle_outer_off
        )
        tvToggle.setBackgroundResource(
            if (isAutoOn) R.drawable.bg_toggle_inner_on else R.drawable.bg_toggle_inner_off
        )
        tvToggle.text = if (isAutoOn) "ON" else "OFF"

        // 3) 상태 문구 & 점 색상
        tvStatus.text = if (isAutoOn) "활성화" else "비활성화"

        // 점은 하나의 shape(bg_status_dot)만 쓰고 틴트로 색만 변경
        // ▶ 아래 색상 코드는 피그마에 맞춰 원하는 값으로 바꿔도 됨
        val dotColor = if (isAutoOn) Color.parseColor("#CFEFF4") else Color.parseColor("#BDBDBD")
        ViewCompat.setBackgroundTintList(ivDot, ColorStateList.valueOf(dotColor))

        // (선택) 타이틀 글자색도 OFF일 때 살짝 흐리게 하고 싶다면:
        // tvTitle.setTextColor(Color.parseColor(if (isAutoOn) "#111111" else "#8E8E93"))
    }


    private fun renderLocationCard() = with(locationCard) {
        tvTitle.text = "$targetName 님 위치"

        val dotColor = if (inSafeZone) "#C4B5FD" else "#FCA5A5"   // 보라/빨강
        ViewCompat.setBackgroundTintList(ivDot, ColorStateList.valueOf(Color.parseColor(dotColor)))

        tvStatus.text = if (inSafeZone) "현재 안전 구역 내에 위치" else "안전 구역 밖에 있음"
    }


    private fun renderSettingsCard() = with(settingsCard) {
        tvTitle.text = "설정"
        ViewCompat.setBackgroundTintList(ivDot, ColorStateList.valueOf(Color.parseColor("#FDBA74")))
        tvDesc.text = "반경 ${radiusMeters}m"
    }

    private fun applyGuidePlacement() {
        val root = vb.homeRoot
        val cs = ConstraintSet().apply { clone(root) }

        // 기존 TOP 제약을 모두 정리
        cs.clear(R.id.cardGuide, ConstraintSet.TOP)
        cs.clear(R.id.cardLocation, ConstraintSet.TOP)
        cs.clear(R.id.cardSettings, ConstraintSet.TOP)

        val gap = resources.getDimensionPixelSize(R.dimen.home_gap)

        if (inSafeZone) {
            // ✅ 안전: 가운데 2개(위치/설정)가 'cardAuto' 바로 아래, 그 아래가 길안내
            cs.connect(R.id.cardLocation, ConstraintSet.TOP, R.id.cardAuto, ConstraintSet.BOTTOM)
            cs.connect(R.id.cardSettings, ConstraintSet.TOP, R.id.cardAuto, ConstraintSet.BOTTOM)
            cs.connect(R.id.cardGuide,    ConstraintSet.TOP, R.id.cardLocation, ConstraintSet.BOTTOM)
        } else {
            // ✅ 이탈: 길안내를 위로 올리고, 그 아래에 가운데 2개(위치/설정)
            cs.connect(R.id.cardGuide,    ConstraintSet.TOP, R.id.cardAuto, ConstraintSet.BOTTOM)
            cs.connect(R.id.cardLocation, ConstraintSet.TOP, R.id.cardGuide, ConstraintSet.BOTTOM)
            cs.connect(R.id.cardSettings, ConstraintSet.TOP, R.id.cardGuide, ConstraintSet.BOTTOM)
        }
        cs.setMargin(R.id.cardLocation, ConstraintSet.TOP, gap)
        cs.setMargin(R.id.cardSettings, ConstraintSet.TOP, gap)
        cs.setMargin(R.id.cardGuide,    ConstraintSet.TOP, gap)
        cs.setMargin(R.id.cardLocation, ConstraintSet.END, gap)
        // 부드러운 전환(선택)
        TransitionManager.beginDelayedTransition(root)
        cs.applyTo(root)
    }


    private fun renderGuideCard() = with(guideCard) {
        tvTitle.text = "$targetName 님 위치로 바로 길안내"

        if (inSafeZone) {
            // 연두 배경, 알림 숨김
            vb.cardGuide.setBackgroundResource(R.drawable.bg_card_guide)
            alertRow.visibility = View.GONE
        } else {
            // 빨강 배경, 알림 표시
            vb.cardGuide.setBackgroundResource(R.drawable.bg_card_guide_alert)
            alertRow.visibility = View.VISIBLE
            tvAlert.text = "$targetName 님이 안전 구역을 벗어났습니다"

            // 경고 점을 붉은색으로
            ViewCompat.setBackgroundTintList(
                ivAlertDot,
                ColorStateList.valueOf(Color.parseColor("#FCA5A5"))
            )
        }

        // ⬇️ 배치도 함께 바꾸기
        applyGuidePlacement()
    }

}
