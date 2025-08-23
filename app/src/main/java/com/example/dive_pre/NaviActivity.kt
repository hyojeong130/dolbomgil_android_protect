package com.example.dive_pre

// Kakao Mobility KNSDK 관련 클래스들
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_CitsGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_GuideStateDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_LocationGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_RouteGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_SafetyGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_VoiceGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuideRouteChangeReason
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.common.KNLocation
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.view.KNNaviView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.dive_pre.api.ApiService
import com.example.dive_pre.api.LocationBothResponse
import com.example.dive_pre.api.DetailedNavigationResponse
import com.example.dive_pre.utils.TokenManager
import android.widget.Toast
import com.kakaomobility.knsdk.KNLanguageType


class NaviActivity : AppCompatActivity(), KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate, KNGuidance_RouteGuideDelegate,
    KNGuidance_SafetyGuideDelegate, KNGuidance_VoiceGuideDelegate, KNGuidance_CitsGuideDelegate {
    lateinit var naviView: KNNaviView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // status bar 영역까지 사용하기 위한 옵션
        window?.apply {
            statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        
        setContentView(R.layout.activity_navi)

        try {
            naviView = findViewById(R.id.navi_view)
            Log.d("NaviActivity", "NaviView found successfully")
        } catch (e: Exception) {
            Log.e("NaviActivity", "Error finding NaviView", e)
            finish()
            return
        }
        
        // ViewTreeObserver를 사용하여 뷰가 완전히 그려진 후 실행
        naviView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                naviView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                Log.d("NaviActivity", "NaviView layout completed, starting KNSDK authentication")
                naviView.postDelayed({
                    initializeKNSDK()
                }, 1000)
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        // NaviView가 준비되지 않았다면 다시 시도
        if (!::naviView.isInitialized) {
            naviView = findViewById(R.id.navi_view)
        }
    }
    
    /**
     * KNSDK를 초기화하고 인증합니다.
     */
    private fun initializeKNSDK() {
        Log.d("NaviActivity", "KNSDK 초기화 시작")
        
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
                            Toast.makeText(this@NaviActivity, "내비게이션 인증에 실패하였습니다", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Log.d("NaviActivity", "KNSDK 인증 성공, API 호출 시작")
                            Toast.makeText(this@NaviActivity, "내비게이션 인증 성공", Toast.LENGTH_SHORT).show()
                            requestRouteFromApi()
                        }
                    }
                }
            )
        }
    }

    /**
     * API에서 위치 정보를 받아서 주행 경로를 요청합니다.
     */
    fun requestRouteFromApi() {
        lifecycleScope.launch {
            try {
                val accessToken = TokenManager.getAccessToken(this@NaviActivity)
                if (accessToken != null) {
                    Log.d("NaviActivity", "상세 경로 API 호출 시작")
                    val detailedRouteResponse = ApiService.getDetailedNavigationRoute(accessToken)
                    
                    Log.d("NaviActivity", "상세 경로 응답 받음: ${detailedRouteResponse.routes.size}개 경로")
                    
                    // 상세 경로 데이터로 내비게이션 시작
                    startNavigationWithDetailedRoute(detailedRouteResponse)
                } else {
                    Toast.makeText(this@NaviActivity, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("NaviActivity", "상세 경로 로드 실패", e)
                Toast.makeText(this@NaviActivity, "경로 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    /**
     * 상세 경로 데이터로 내비게이션을 시작합니다.
     */
    private fun startNavigationWithDetailedRoute(routeResponse: DetailedNavigationResponse) {
        Thread {
            try {
                if (routeResponse.routes.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this@NaviActivity, "경로 데이터가 없습니다", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }
                
                val route = routeResponse.routes[0]
                if (route.result_code != 0) {
                    runOnUiThread {
                        Toast.makeText(this@NaviActivity, route.result_msg, Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }
                
                Log.d("NaviActivity", "=== 상세 경로 정보 ===")
                Log.d("NaviActivity", "총 거리: ${route.distance}m")
                Log.d("NaviActivity", "예상 시간: ${route.duration}초")
                Log.d("NaviActivity", "섹션 수: ${route.sections.size}")
                
                route.sections.forEachIndexed { sectionIndex, section ->
                    Log.d("NaviActivity", "섹션 $sectionIndex: ${section.roads.size}개 도로, ${section.guides.size}개 가이드")
                    section.guides.forEach { guide ->
                        Log.d("NaviActivity", "  가이드: ${guide.guidance} at (${guide.x}, ${guide.y})")
                    }
                }
                
                // 출발지와 목적지 좌표 추출
                val origin = route.summary.origin
                val destination = route.summary.destination
                
                Log.d("NaviActivity", "출발지: (${origin.x}, ${origin.y})")
                Log.d("NaviActivity", "목적지: (${destination.x}, ${destination.y})")
                
                // WGS84 좌표를 카카오 좌표계로 변환
                val startX = ((origin.x - 125.0) * 200000 + 200000).toInt()
                val startY = ((origin.y - 33.0) * 200000 + 400000).toInt()
                val endX = ((destination.x - 125.0) * 200000 + 200000).toInt()
                val endY = ((destination.y - 33.0) * 200000 + 400000).toInt()
                
                Log.d("NaviActivity", "변환된 좌표 - Start: $startX, $startY | End: $endX, $endY")
                
                // KNPOI 생성
                val start = KNPOI("출발지", startX, startY, "출발지")
                val goal = KNPOI("목적지", endX, endY, "목적지")
                
                Log.d("NaviActivity", "Trip 생성 시작...")
                
                // 경로 생성 (상세 경로 데이터를 가지고 있지만 일단 기본 방식 사용)
                KNApplication.knsdk.makeTripWithStart(
                    aStart = start,
                    aGoal = goal,
                    aVias = null
                ) { aError, aTrip ->
                    
                    // 상세한 응답 로그 출력
                    Log.d("NaviActivity", "=== Trip 응답 시작 ===")
                    Log.d("NaviActivity", "Error: $aError")
                    Log.d("NaviActivity", "Trip: $aTrip")
                    
                    if (aError != null) {
                        Log.e("NaviActivity", "Error Code: ${aError.code}")
                        Log.e("NaviActivity", "Error Message: ${aError.msg}")
                        Log.e("NaviActivity", "Error TagMsg: ${aError.tagMsg}")
                        Log.e("NaviActivity", "Error Extra: ${aError.extra}")
                    }
                    
                    Log.d("NaviActivity", "=== Trip 응답 끝 ===")
                    
                    // 경로 요청이 성공하면 aError는 Null이 됩니다.
                    if (aError == null && aTrip != null) {
                        Log.d("NaviActivity", "Trip 생성 성공, 가이드 시작")
                        startGuide(aTrip)
                    } else {
                        Log.e("NaviActivity", "Trip 생성 실패")
                        runOnUiThread {
                            val errorMsg = aError?.msg ?: "알 수 없는 오류"
                            Toast.makeText(this@NaviActivity, "경로 생성에 실패했습니다: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NaviActivity", "startNavigationWithDetailedRoute 예외", e)
            }
        }.start()
    }
    
    /**
     * 주행 경로를 요청합니다. (기존 방식, 백업용)
     */
    fun requestRouteWithCoordinates(startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        Thread {
            try {
                Log.d("NaviActivity", "원본 좌표 - Start: $startLat, $startLng | End: $endLat, $endLng")

                // WGS84 좌표를 카카오 좌표계로 변환
                val startX = ((startLng - 125.0) * 200000 + 200000).toInt()
                val startY = ((startLat - 33.0) * 200000 + 400000).toInt()
                val endX = ((endLng - 125.0) * 200000 + 200000).toInt()
                val endY = ((endLat - 33.0) * 200000 + 400000).toInt()

                Log.d("NaviActivity", "변환된 좌표 - Start: $startX, $startY | End: $endX, $endY")

                // 출발지 설정    
                val start = KNPOI("출발지", startX, startY, "출발지")

                // 목적지 설정
                val goal = KNPOI("목적지", endX, endY, "목적지")

                Log.d("NaviActivity", "Trip 생성 시작...")
                
                // 경로 생성
                KNApplication.knsdk.makeTripWithStart(
                    aStart = start,
                    aGoal = goal,
                    aVias = null
                ) { aError, aTrip ->

                    // 상세한 응답 로그 출력
                    Log.d("NaviActivity", "=== Trip 응답 시작 ===")
                    Log.d("NaviActivity", "Error: $aError")
                    Log.d("NaviActivity", "Trip: $aTrip")
                    
                    if (aError != null) {
                        Log.e("NaviActivity", "Error Code: ${aError.code}")
                        Log.e("NaviActivity", "Error Message: ${aError.msg}")
                        Log.e("NaviActivity", "Error TagMsg: ${aError.tagMsg}")
                        Log.e("NaviActivity", "Error Extra: ${aError.extra}")
                    }
                    
                    
                    Log.d("NaviActivity", "=== Trip 응답 끝 ===")
                    
                    // 경로 요청이 성공하면 aError는 Null이 됩니다.
                    if (aError == null && aTrip != null) {
                        Log.d("NaviActivity", "Trip 생성 성공, 가이드 시작")
                        startGuide(aTrip)
                    } else {
                        Log.e("NaviActivity", "Trip 생성 실패")
                        runOnUiThread {
                            val errorMsg = aError?.msg ?: "알 수 없는 오류"
                            Toast.makeText(this@NaviActivity, "경로 생성에 실패했습니다: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NaviActivity", "requestRouteWithCoordinates 예외", e)
            }
        }.start()
    }

    fun startGuide(trip: KNTrip?) {
        runOnUiThread {
            try {
                // NaviView가 준비될 때까지 기다림
                if (!::naviView.isInitialized) {
                    Log.e("NaviActivity", "NaviView not initialized")
                    return@runOnUiThread
                }
                
                KNApplication.knsdk.sharedGuidance()?.apply {
                    Log.d("NaviActivity", "Setting up guidance delegates...")
                    // guidance delegate 등록
                    guideStateDelegate = this@NaviActivity
                    locationGuideDelegate = this@NaviActivity
                    routeGuideDelegate = this@NaviActivity
                    safetyGuideDelegate = this@NaviActivity
                    voiceGuideDelegate = this@NaviActivity
                    citsGuideDelegate = this@NaviActivity

                    Log.d("NaviActivity", "Initializing NaviView with guidance...")
                    naviView.initWithGuidance(
                        this,
                        trip,
                        KNRoutePriority.KNRoutePriority_Recommand,
                        0
                    )
                    Log.d("NaviActivity", "NaviView initialization completed")
                } ?: run {
                    Log.e("NaviActivity", "SharedGuidance is null!")
                }
            } catch (e: Exception) {
                Log.e("NaviActivity", "Error initializing navigation", e)
                Toast.makeText(this@NaviActivity, "내비게이션 초기화 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun guidanceCheckingRouteChange(aGuidance: KNGuidance) {
        naviView.guidanceCheckingRouteChange(aGuidance)
    }

    override fun guidanceDidUpdateRoutes(
        aGuidance: KNGuidance,
        aRoutes: List<KNRoute>,
        aMultiRouteInfo: KNMultiRouteInfo?
    ) {
        naviView.guidanceDidUpdateRoutes(aGuidance, aRoutes, aMultiRouteInfo)
    }

    override fun guidanceGuideEnded(aGuidance: KNGuidance) {
        naviView.guidanceGuideEnded(aGuidance)
    }

    override fun guidanceGuideStarted(aGuidance: KNGuidance) {
        naviView.guidanceGuideStarted(aGuidance)
    }

    override fun guidanceOutOfRoute(aGuidance: KNGuidance) {
        naviView.guidanceOutOfRoute(aGuidance)
    }

    override fun guidanceRouteChanged(
        aGuidance: KNGuidance,
        aFromRoute: KNRoute,
        aFromLocation: KNLocation,
        aToRoute: KNRoute,
        aToLocation: KNLocation,
        aChangeReason: KNGuideRouteChangeReason
    ) {
        naviView.guidanceRouteChanged(aGuidance)
    }

    override fun guidanceRouteUnchanged(aGuidance: KNGuidance) {
        naviView.guidanceRouteUnchanged(aGuidance)
    }

    override fun guidanceRouteUnchangedWithError(aGuidnace: KNGuidance, aError: KNError) {
        naviView.guidanceRouteUnchangedWithError(aGuidnace, aError)
    }

    override fun guidanceDidUpdateLocation(
        aGuidance: KNGuidance,
        aLocationGuide: KNGuide_Location
    ) {
        naviView.guidanceDidUpdateLocation(aGuidance, aLocationGuide)
    }

    override fun guidanceDidUpdateRouteGuide(aGuidance: KNGuidance, aRouteGuide: KNGuide_Route) {
        naviView.guidanceDidUpdateRouteGuide(aGuidance, aRouteGuide)
    }

    override fun guidanceDidUpdateAroundSafeties(
        aGuidance: KNGuidance,
        aSafeties: List<KNSafety>?
    ) {
        naviView.guidanceDidUpdateAroundSafeties(aGuidance, aSafeties)
    }

    override fun guidanceDidUpdateSafetyGuide(
        aGuidance: KNGuidance,
        aSafetyGuide: KNGuide_Safety?
    ) {
        naviView.guidanceDidUpdateSafetyGuide(aGuidance, aSafetyGuide)
    }

    override fun didFinishPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        naviView.didFinishPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun shouldPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice,
        aNewData: MutableList<ByteArray>
    ): Boolean {
        return naviView.shouldPlayVoiceGuide(aGuidance, aVoiceGuide, aNewData)
    }

    override fun willPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        naviView.willPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun didUpdateCitsGuide(aGuidance: KNGuidance, aCitsGuide: KNGuide_Cits) {
        naviView.didUpdateCitsGuide(aGuidance, aCitsGuide)
    }

}
