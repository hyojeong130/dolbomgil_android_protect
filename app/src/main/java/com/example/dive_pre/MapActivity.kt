package com.example.dive_pre

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dive_pre.api.ApiService
import com.example.dive_pre.api.toLatLngs
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

    private fun fetchAndDrawRoute(kakaoMap: KakaoMap) {
        val token = MyTokenStore.token
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
            Log.w("Route", "토큰 없음 → 호출 중단")
            return
        }

        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    ApiService.getProtectorToCareeRoute(
                        accessToken = token,
                        priority = "RECOMMEND",
                        summary = true,
                        alternatives = false,
                        roadDetails = false
                    )
                }
            }.onSuccess { resp ->
                val latLngs = resp.toLatLngs()
                Log.i("Route", "좌표 개수 = ${latLngs.size}")
                if (latLngs.isEmpty()) {
                    Toast.makeText(this@MapActivity, "경로 없음", Toast.LENGTH_SHORT).show()
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

                moveCameraToFit(kakaoMap, latLngs, padding = 0.15)
            }.onFailure { e ->
                Log.e("Route", "❌ API 실패", e)
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
object MyTokenStore { var token: String? = null }
