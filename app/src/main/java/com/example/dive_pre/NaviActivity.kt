package com.example.dive_pre

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.guidance.knguidance.*
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.common.KNLocation
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.example.dive_pre.api.ApiService
import com.example.dive_pre.utils.TokenManager
import com.example.dive_pre.BuildConfig
import com.kakaomobility.knsdk.KNLanguageType

// FloatPoint 클래스 정의
data class FloatPoint(val x: Float, val y: Float)


class NaviActivity :
    AppCompatActivity(),
    KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate,
    KNGuidance_RouteGuideDelegate,
    KNGuidance_SafetyGuideDelegate,
    KNGuidance_VoiceGuideDelegate,
    KNGuidance_CitsGuideDelegate {

    private lateinit var naviView: KNNaviView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navi)

        naviView = findViewById(R.id.navi_view)

        // 상태바 투명 옵션(선택)
        window?.apply {
            statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        initializeKNSDK()
    }



    /** KNSDK 초기화 및 인증 */
    private fun initializeKNSDK() {
        Log.d("NaviActivity", "KNSDK 초기화 시작")
        Log.d("NaviActivity", "사용할 앱 키: ${BuildConfig.KAKAO_NATIVE_APP_KEY}")
        Log.d("NaviActivity", "클라이언트 버전: ${BuildConfig.VERSION_NAME}")
        
        try {
            KNApplication.knsdk.apply {
                initializeWithAppKey(
                    aAppKey = BuildConfig.KAKAO_NATIVE_APP_KEY,
                    aClientVersion = BuildConfig.VERSION_NAME,
                    aUserKey = "naviUser_${System.currentTimeMillis()}",  // 고유한 사용자 키 생성
                    aLangType = KNLanguageType.KNLanguageType_KOREAN,
                    aCompletion = { error ->
                        runOnUiThread {
                            if (error != null) {
                                Log.e("NaviActivity", "KNSDK 인증 실패: $error")
                                Log.e("NaviActivity", "오류 코드: ${error.code}")
                                Log.e("NaviActivity", "오류 메시지: ${error.msg}")
                                Toast.makeText(this@NaviActivity, "내비게이션 인증에 실패하였습니다: ${error.msg}", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Log.d("NaviActivity", "KNSDK 인증 성공, 경로 요청 시작")
                                Toast.makeText(this@NaviActivity, "내비게이션 인증 성공", Toast.LENGTH_SHORT).show()
                                requestRoute()
                            }
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("NaviActivity", "KNSDK 초기화 중 예외 발생: $e")
            Toast.makeText(this@NaviActivity, "KNSDK 초기화 실패: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /** 경로 요청 */
    private fun requestRoute() {
        lifecycleScope.launch {
            try {
                val accessToken = TokenManager.getAccessToken(this@NaviActivity)
                if (accessToken != null) {
                    Log.d("NaviActivity", "API에서 경로 정보 가져오는 중...")
                    val routeResponse = ApiService.getDetailedNavigationRoute(accessToken)
                    
                    if (routeResponse.routes.isNotEmpty()) {
                        val route = routeResponse.routes[0]
                        Log.d("NaviActivity", "출발지: (${route.summary.origin.x}, ${route.summary.origin.y})")
                        Log.d("NaviActivity", "목적지: (${route.summary.destination.x}, ${route.summary.destination.y})")
                        
                        // API 응답 좌표(WGS84)를 KATEC 좌표로 변환 (KNSDK 내장 함수 사용)
                        val startKatec = KNApplication.knsdk.convertWGS84ToKATEC(route.summary.origin.x, route.summary.origin.y)
                        val goalKatec = KNApplication.knsdk.convertWGS84ToKATEC(route.summary.destination.x, route.summary.destination.y)
                        
                        val startPoi = KNPOI("출발지", startKatec.x.toInt(), startKatec.y.toInt(), "출발지")
                        val goalPoi = KNPOI("목적지", goalKatec.x.toInt(), goalKatec.y.toInt(), "목적지")
                        
                        Log.d("NaviActivity", "변환된 KATEC 좌표:")
                        Log.d("NaviActivity", "  출발지: WGS84(${route.summary.origin.x}, ${route.summary.origin.y}) → KATEC(${startKatec.x}, ${startKatec.y})")
                        Log.d("NaviActivity", "  목적지: WGS84(${route.summary.destination.x}, ${route.summary.destination.y}) → KATEC(${goalKatec.x}, ${goalKatec.y})")
                        
                        makeTripWithCoordinates(startPoi, goalPoi)
                    } else {
                        Toast.makeText(this@NaviActivity, "경로 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@NaviActivity, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("NaviActivity", "경로 요청 실패", e)
                Toast.makeText(this@NaviActivity, "경로 요청에 실패했습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /** Trip 생성 */
    private fun makeTripWithCoordinates(startPoi: KNPOI, goalPoi: KNPOI) {
        KNApplication.knsdk.makeTripWithStart(
            aStart = startPoi,
            aGoal = goalPoi,
            aVias = null
        ) { error: KNError?, trip: KNTrip? ->
            
            runOnUiThread {
                if (error != null || trip == null) {
                    Log.e("NaviActivity", "경로 검색 실패: ${error?.code} - ${error?.msg}")
                    Toast.makeText(
                        this,
                        "경로 검색 실패: ${error?.msg ?: "unknown"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@runOnUiThread
                }
                startGuide(trip)
            }
        }
    }


    /** 주행 시작(지도 연결) */
    private fun startGuide(trip: KNTrip) {
        val guidance = KNApplication.knsdk.sharedGuidance()
        if (guidance == null) {
            Toast.makeText(this, "Guidance 객체 없음", Toast.LENGTH_SHORT).show()
            return
        }

        // 델리게이트 연결
        guidance.apply {
            guideStateDelegate    = this@NaviActivity
            locationGuideDelegate = this@NaviActivity
            routeGuideDelegate    = this@NaviActivity
            safetyGuideDelegate   = this@NaviActivity
            voiceGuideDelegate    = this@NaviActivity
            citsGuideDelegate     = this@NaviActivity
        }

        // 반드시 UI 스레드에서 뷰 초기화
        naviView.initWithGuidance(
            guidance,
            trip,
            KNRoutePriority.KNRoutePriority_Recommand,
            0 /* 옵션 */
        )
    }

    // === 라이프사이클 전달 (중요) ===
    override fun onResume() { 
        super.onResume() 
        if (::naviView.isInitialized) {
            // NaviView 라이프사이클 처리가 필요한 경우 여기에 추가
        }
    }
    
    override fun onPause() { 
        if (::naviView.isInitialized) {
            // NaviView 라이프사이클 처리가 필요한 경우 여기에 추가
        }
        super.onPause() 
    }

    // === 아래는 델리게이트들: 내부 view 로 전달 ===
    override fun guidanceCheckingRouteChange(g: KNGuidance) { naviView.guidanceCheckingRouteChange(g) }
    override fun guidanceDidUpdateRoutes(g: KNGuidance, routes: List<KNRoute>, info: KNMultiRouteInfo?) {
        naviView.guidanceDidUpdateRoutes(g, routes, info)
    }
    override fun guidanceGuideEnded(g: KNGuidance) { naviView.guidanceGuideEnded(g) }
    override fun guidanceGuideStarted(g: KNGuidance) { naviView.guidanceGuideStarted(g) }
    override fun guidanceOutOfRoute(g: KNGuidance) { naviView.guidanceOutOfRoute(g) }
    override fun guidanceRouteChanged(g: KNGuidance, fr: KNRoute, fl: KNLocation, to: KNRoute, tl: KNLocation, r: KNGuideRouteChangeReason) {
        naviView.guidanceRouteChanged(g)
    }
    override fun guidanceRouteUnchanged(g: KNGuidance) { naviView.guidanceRouteUnchanged(g) }
    override fun guidanceRouteUnchangedWithError(g: KNGuidance, e: KNError) { naviView.guidanceRouteUnchangedWithError(g, e) }
    override fun guidanceDidUpdateLocation(g: KNGuidance, l: KNGuide_Location) { naviView.guidanceDidUpdateLocation(g, l) }
    override fun guidanceDidUpdateRouteGuide(g: KNGuidance, r: KNGuide_Route) { naviView.guidanceDidUpdateRouteGuide(g, r) }
    override fun guidanceDidUpdateAroundSafeties(g: KNGuidance, s: List<KNSafety>?) { naviView.guidanceDidUpdateAroundSafeties(g, s) }
    override fun guidanceDidUpdateSafetyGuide(g: KNGuidance, s: KNGuide_Safety?) { naviView.guidanceDidUpdateSafetyGuide(g, s) }
    override fun didFinishPlayVoiceGuide(g: KNGuidance, v: KNGuide_Voice) { naviView.didFinishPlayVoiceGuide(g, v) }
    override fun shouldPlayVoiceGuide(g: KNGuidance, v: KNGuide_Voice, d: MutableList<ByteArray>) = naviView.shouldPlayVoiceGuide(g, v, d)
    override fun willPlayVoiceGuide(g: KNGuidance, v: KNGuide_Voice) { naviView.willPlayVoiceGuide(g, v) }
    override fun didUpdateCitsGuide(g: KNGuidance, c: KNGuide_Cits) { naviView.didUpdateCitsGuide(g, c) }

}
