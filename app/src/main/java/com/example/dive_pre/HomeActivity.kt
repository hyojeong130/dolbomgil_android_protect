package com.example.dive_pre


import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.TransitionManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.dive_pre.databinding.ActivityHomeBinding
import com.example.dive_pre.databinding.ViewCardAutoBinding
import com.example.dive_pre.databinding.ViewCardLocationBinding
import com.example.dive_pre.databinding.ViewCardSettingBinding
import com.example.dive_pre.databinding.ViewCardGuideBinding
import com.example.dive_pre.api.ApiService
import com.example.dive_pre.api.HomeInfoResponse
import com.example.dive_pre.utils.TokenManager
import android.util.Log
import android.widget.Toast

class HomeActivity : AppCompatActivity() {

    private lateinit var vb: ActivityHomeBinding
    private lateinit var autoCardBinding: ViewCardAutoBinding
    private lateinit var locationCard: ViewCardLocationBinding
    private lateinit var settingsCard: ViewCardSettingBinding
    private lateinit var guideCard: ViewCardGuideBinding

    // API에서 가져올 데이터
    private var protectorName = "보호자"
    private var careeName = "피보호자"
    private var isAutoOn: Boolean? = null
    private var inSafeZone: Boolean? = null
    private var radiusMeters: Int? = null
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

        // API 데이터 로드
        loadHomeData()
        
        // 초기 렌더
        renderAutoCard()
        renderLocationCard()
        renderSettingsCard()
        renderGuideCard()

        // 리스너
        autoCardBinding.toggleGroup.setOnClickListener {
            toggleSafeZone()
        }

        // 위치 카드: 전체 클릭으로 상태 토글(데모)
        locationCard.root.setOnClickListener {
            inSafeZone = !inSafeZone!!
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
            val uri = Uri.parse("geo:$targetLat,$targetLng?q=$targetLat,$targetLng($careeName)")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        vb.btnMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }
    
    private fun loadHomeData() {
        lifecycleScope.launch {
            try {
                val accessToken = TokenManager.getAccessToken(this@HomeActivity)
                if (accessToken != null) {
                    val homeInfo = ApiService.getHomeInfo(accessToken)
                    updateDataFromApi(homeInfo)
                    updateUI()
                } else {
                    Toast.makeText(this@HomeActivity, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "홈 데이터 로드 실패", e)
                Toast.makeText(this@HomeActivity, "데이터를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateDataFromApi(homeInfo: HomeInfoResponse) {
        protectorName = homeInfo.protector_name
        careeName = homeInfo.caree_name
        isAutoOn = homeInfo.safe_zone_active
        inSafeZone = homeInfo.caree_in_safe_zone
        radiusMeters = homeInfo.safe_zone_radius_meters
    }
    
    private fun updateUI() {
        // 프로필 업데이트
        vb.tvProfileName.text = "${protectorName}(보호자)"
        
        renderAutoCard()
        renderLocationCard()
        renderSettingsCard()
        renderGuideCard()
    }
    
    private fun toggleSafeZone() {
        lifecycleScope.launch {
            try {
                val accessToken = TokenManager.getAccessToken(this@HomeActivity)
                if (accessToken != null) {
                    // 토글 API 호출
                    val toggleResponse = ApiService.toggleSafeZone(accessToken)
                    
                    if (toggleResponse.success) {
                        // 성공 시 홈 데이터 새로고침
                        loadHomeData()
                        Toast.makeText(this@HomeActivity, toggleResponse.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@HomeActivity, "설정 변경에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "SafeZone 토글 실패", e)
                Toast.makeText(this@HomeActivity, "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun renderAutoCard() = with(autoCardBinding) {
        if (isAutoOn == null) {
            // SafeZone이 설정되지 않은 상태
            vb.cardAuto.setBackgroundResource(R.drawable.bg_card_auto_off)
            
            ivToggleOuter.setBackgroundResource(R.drawable.bg_toggle_outer_off)
            tvToggle.setBackgroundResource(R.drawable.bg_toggle_inner_off)
            tvToggle.text = "OFF"
            
            tvStatus.text = "설정되지않음"
            val dotColor = Color.parseColor("#BDBDBD")
            ViewCompat.setBackgroundTintList(ivDot, ColorStateList.valueOf(dotColor))
        } else {
            // SafeZone이 설정된 상태
            vb.cardAuto.setBackgroundResource(
                if (isAutoOn == true) R.drawable.bg_card_auto_on else R.drawable.bg_card_auto_off
            )

            ivToggleOuter.setBackgroundResource(
                if (isAutoOn == true) R.drawable.bg_toggle_outer_on else R.drawable.bg_toggle_outer_off
            )
            tvToggle.setBackgroundResource(
                if (isAutoOn == true) R.drawable.bg_toggle_inner_on else R.drawable.bg_toggle_inner_off
            )
            tvToggle.text = if (isAutoOn == true) "ON" else "OFF"

            tvStatus.text = if (isAutoOn == true) "활성화" else "비활성화"

            val dotColor = if (isAutoOn == true) Color.parseColor("#CFEFF4") else Color.parseColor("#BDBDBD")
            ViewCompat.setBackgroundTintList(ivDot, ColorStateList.valueOf(dotColor))
        }
    }


    private fun renderLocationCard() = with(locationCard) {
        tvTitle.text = "$careeName 님 위치"

        if (inSafeZone == null) {
            // SafeZone이 설정되지 않은 상태
            val dotColor = "#BDBDBD"
            ViewCompat.setBackgroundTintList(ivDot, ColorStateList.valueOf(Color.parseColor(dotColor)))
            tvStatus.text = "설정되지않음"
        } else {
            // SafeZone이 설정된 상태
            val dotColor = if (inSafeZone == true) "#C4B5FD" else "#FCA5A5"   // 보라/빨강
            ViewCompat.setBackgroundTintList(ivDot, ColorStateList.valueOf(Color.parseColor(dotColor)))
            tvStatus.text = if (inSafeZone == true) "현재 안전 구역 내에 위치" else "안전 구역 밖에 있음"
        }
    }


    private fun renderSettingsCard() = with(settingsCard) {
        tvTitle.text = "설정"
        ViewCompat.setBackgroundTintList(ivDot, ColorStateList.valueOf(Color.parseColor("#FDBA74")))
        
        if (radiusMeters == null) {
            tvDesc.text = "설정되지않음"
        } else {
            tvDesc.text = "반경 ${radiusMeters}m"
        }
    }

    private fun applyGuidePlacement() {
        val root = vb.homeRoot
        val cs = ConstraintSet().apply { clone(root) }

        // 기존 TOP 제약을 모두 정리
        cs.clear(R.id.cardGuide, ConstraintSet.TOP)
        cs.clear(R.id.cardLocation, ConstraintSet.TOP)
        cs.clear(R.id.cardSettings, ConstraintSet.TOP)

        val gap = resources.getDimensionPixelSize(R.dimen.home_gap)

        if (inSafeZone == null || inSafeZone == true) {
            // ✅ 안전 또는 설정되지않음: 가운데 2개(위치/설정)가 'cardAuto' 바로 아래, 그 아래가 길안내
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
        tvTitle.text = "$careeName 님 위치로 바로 길안내"

        if (inSafeZone == null) {
            // SafeZone이 설정되지 않은 상태
            vb.cardGuide.setBackgroundResource(R.drawable.bg_card_guide)
            alertRow.visibility = View.GONE
        } else if (inSafeZone == true) {
            // 연두 배경, 알림 숨김
            vb.cardGuide.setBackgroundResource(R.drawable.bg_card_guide)
            alertRow.visibility = View.GONE
        } else {
            // 빨강 배경, 알림 표시
            vb.cardGuide.setBackgroundResource(R.drawable.bg_card_guide_alert)
            alertRow.visibility = View.VISIBLE
            tvAlert.text = "$careeName 님이 안전 구역을 벗어났습니다"

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
