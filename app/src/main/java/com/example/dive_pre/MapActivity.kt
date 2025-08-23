package com.example.dive_pre

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dive_pre.api.ApiService
import com.example.dive_pre.api.RouteResponse
import com.example.dive_pre.api.toLatLngs
import com.example.dive_pre.utils.TokenManager
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // 저장된 access_token을 MyTokenStore에 복원
        restoreAccessToken()

        mapView = findViewById(R.id.map_view)
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}
            override fun onMapError(error: Exception) {
                Log.e("KakaoMap", "Map init error", error)
                Toast.makeText(this@MapActivity, "지도 초기화 실패", Toast.LENGTH_SHORT).show()
            }
        }, object : KakaoMapReadyCallback() {
            override fun getPosition(): LatLng = LatLng.from(35.2340, 129.0793) // 초기 보기
            override fun getZoomLevel(): Int = 15

            override fun onMapReady(kakaoMap: KakaoMap) {
                Log.d("MapActivity", "onMapReady ✅")
                fetchAndDrawRoute(kakaoMap)
            }
        })
    }

    /**
     * SharedPreferences에 저장된 access_token을 MyTokenStore에 복원합니다
     */
    private fun restoreAccessToken() {
        val savedToken = TokenManager.getAccessToken(this)
        if (savedToken != null) {
            MyTokenStore.setToken(savedToken)
            Log.d("MapActivity", "저장된 access_token 복원 완료")
        } else {
            Log.w("MapActivity", "저장된 access_token이 없습니다")
        }
    }

    private fun fetchAndDrawRoute(kakaoMap: KakaoMap) {
        val token = MyTokenStore.getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
            Log.w("Route", "토큰 없음 → 호출 중단")
            return
        }

        Log.d("Route", "=== 경로 조회 시작 ===")
        Log.d("Route", "토큰: ${token.take(10)}...")
        Log.d("Route", "API 호출 파라미터: priority=RECOMMEND, summary=false, alternatives=false, road_details=false")

        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    Log.d("Route", "API 호출 중...")
                    ApiService.getProtectorToCareeRoute(
                        accessToken = token,
                        priority = "RECOMMEND",
                        summary = false,  // curl과 동일하게 false
                        alternatives = false,
                        roadDetails = false,  // curl과 동일하게 false
                        carFuel = "GASOLINE",  // curl과 동일하게 GASOLINE
                        carHipass = false      // curl과 동일하게 false
                    )
                }
            }.onSuccess { resp ->
                Log.d("Route", "=== API 응답 성공 ===")
                Log.d("Route", "trans_id: ${resp.trans_id}")
                Log.d("Route", "routes 개수: ${resp.routes.size}")
                
                // 경로 정보 로그 출력
                resp.routes.firstOrNull()?.let { route ->
                    Log.d("Route", "=== 경로 정보 ===")
                    Log.d("Route", "결과: ${route.result_msg}")
                    Log.d("Route", "거리: ${route.distance}m")
                    Log.d("Route", "시간: ${route.duration}초")
                    Log.d("Route", "출발지: (${route.summary.origin.y}, ${route.summary.origin.x})")
                    Log.d("Route", "목적지: (${route.summary.destination.y}, ${route.summary.destination.x})")
                    Log.d("Route", "좌표 개수: ${resp.toLatLngs().size}")
                    Log.d("Route", "=================")
                }
                
                val latLngs = resp.toLatLngs()
                Log.i("Route", "좌표 개수 = ${latLngs.size}")
                
                if (latLngs.isEmpty()) {
                    Toast.makeText(this@MapActivity, "경로 좌표가 없습니다", Toast.LENGTH_SHORT).show()
                    Log.w("Route", "경로 좌표가 비어있음")
                    return@onSuccess
                }

                val shapeLayer = kakaoMap.shapeManager?.layer
                if (shapeLayer == null) {
                    Log.w("Route", "shapeLayer is null")
                    return@onSuccess
                }

                val mapPoints = MapPoints.fromLatLng(latLngs)
                val routeLine = PolylineOptions.from(mapPoints, 10f, Color.parseColor("#1565C0"))
                shapeLayer.addPolyline(routeLine)

                Log.d("Route", "경로 그리기 완료: ${latLngs.size}개 좌표")
                moveCameraToFit(kakaoMap, latLngs, padding = 0.15)
            }.onFailure { e ->
                Log.e("Route", "❌ API 실패", e)
                Log.e("Route", "에러 메시지: ${e.message}")
                Toast.makeText(this@MapActivity, "경로 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** LatLng 리스트의 bounding box로 카메라 중앙/줌을 대략 맞춤 */
    private fun moveCameraToFit(kakaoMap: KakaoMap, latLngs: List<LatLng>, padding: Double = 0.1) {
        if (latLngs.isEmpty()) return

        val minLat = latLngs.minOf { it.latitude }
        val maxLat = latLngs.maxOf { it.latitude }
        val minLng = latLngs.minOf { it.longitude }
        val maxLng = latLngs.maxOf { it.longitude }

        val center = LatLng.from((minLat + maxLat) / 2.0, (minLng + maxLng) / 2.0)

        val dLat = (maxLat - minLat).coerceAtLeast(1e-5)
        val dLng = (maxLng - minLng).coerceAtLeast(1e-5)
        val paddedLat = dLat * (1.0 + padding * 2)
        val paddedLng = dLng * (1.0 + padding * 2)
        val maxSpan = maxOf(paddedLat, paddedLng)

        val zoom = when {
            maxSpan > 30      -> 5
            maxSpan > 10      -> 7
            maxSpan > 5       -> 9
            maxSpan > 2       -> 11
            maxSpan > 0.8     -> 12
            maxSpan > 0.3     -> 13
            maxSpan > 0.12    -> 14
            maxSpan > 0.05    -> 15
            maxSpan > 0.02    -> 16
            else              -> 17
        }
        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(center, zoom))
    }

    override fun onResume() { super.onResume(); mapView.resume() }
    override fun onPause() { mapView.pause(); super.onPause() }
}

/** 로그인 후 받은 access_token을 여기 저장/복원해서 사용 */
object MyTokenStore { 
    private var _token: String? = null 
    
    /**
     * 토큰을 저장합니다
     */
    fun setToken(newToken: String) {
        _token = newToken
        Log.d("MyTokenStore", "토큰 저장 완료")
    }
    
    /**
     * 저장된 토큰을 반환합니다
     */
    fun getToken(): String? = _token
    
    /**
     * 토큰이 유효한지 확인합니다
     */
    fun hasValidToken(): Boolean = !_token.isNullOrBlank()
}
