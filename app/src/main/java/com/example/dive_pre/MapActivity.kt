package com.example.dive_pre

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.PolylineOptions
import android.graphics.Color

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.map_view)

        // MapView 라이프사이클 + 준비 콜백
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 필요 시 정리 로직
            }

            override fun onMapError(error: Exception) {
                error.printStackTrace() // 인증 실패/네트워크 등
                Log.e("KakaoMap", "Map error", error)
                Log.i("KakaoMap", "KEY_LEN=" + BuildConfig.KAKAO_NATIVE_MAP_APP_KEY.length)
                Log.i("KakaoMap", "KEY_HEAD=" + BuildConfig.KAKAO_NATIVE_MAP_APP_KEY.take(4)) // 앞 4글자만
            }
        }, object : KakaoMapReadyCallback() {

            // 시작 위치/줌: 강남역 인근 (원하시는 좌표로 바꾸세요)
            override fun getPosition(): LatLng = LatLng.from(35.2340, 129.0793)
            override fun getZoomLevel(): Int = 15

            override fun onMapReady(kakaoMap: KakaoMap) {
                // ---- 좌표 ----
                val startPnu = LatLng.from(35.2340, 129.0793)     // 부산대학교
                val destPnuStation = LatLng.from(35.2314, 129.0849) // 부산대역(임시 좌표, 필요시 교체)

                // ---- 카메라: 두 지점의 중간으로 이동 ----
                val mid = LatLng.from(
                    (startPnu.latitude + destPnuStation.latitude) / 2.0,
                    (startPnu.longitude + destPnuStation.longitude) / 2.0
                )
                kakaoMap.moveCamera(
                    CameraUpdateFactory.newCenterPosition(mid, 16)
                )

                // ---- Label(마커) 레이어 준비 ----
                val labelManager = kakaoMap.labelManager ?: return
                val labelLayer = labelManager.layer ?: return

                // 텍스트 스타일 (크기/색)
                fun textStyles(size: Int, color: Int): LabelStyles =
                    LabelStyles.from(
                        LabelStyle.from(LabelTextStyle.from(size, color))
                    )

                // 아이콘 스타일 (리소스 아이콘 그대로 사용)
                fun iconStyles(iconResId: Int): LabelStyles =
                    LabelStyles.from(
                        LabelStyle.from(iconResId)
                    )

                // 편의 함수: 아이콘 + 텍스트 한 쌍을 같은 위치에 추가
                fun addPinWithText(pos: LatLng, iconRes: Int, text: String) {
                    // 아이콘
                    val iconOptions = LabelOptions
                        .from(pos)
                        .setStyles(iconStyles(iconRes))
                        .setRank(100)

                    // 텍스트 (아이콘 위에 보이도록 rank 더 높게)
                    val textOptions = LabelOptions
                        .from(pos)
                        .setStyles(textStyles(22, android.graphics.Color.BLACK))
                        .setTexts(LabelTextBuilder().setTexts(text))
                        .setRank(101)

                    labelLayer.addLabel(iconOptions)
                    // 텍스트를 아이콘 위로 약간 올리기
                    labelLayer.addLabel(textOptions)?.changePixelOffset(0f, -24f)
                }

                // ---- 출발/도착 마커 추가 ----
                addPinWithText(startPnu, R.drawable.ic_pin_caree, "부산대(출발)")
                addPinWithText(destPnuStation, R.drawable.ic_pin_protector, "부산대역(도착)")

                // ---- Polyline(직선 경로) 그리기 ----
                val shapeManager = kakaoMap.shapeManager ?: return
// 버전에 따라 `layer` 또는 `getLayer()`가 있어. 둘 중 되는 걸 써줘.
                val shapeLayer = shapeManager.layer ?: shapeManager.getLayer() ?: return

// 두 점을 MapPoints로 변환
                val mapPoints = MapPoints.fromLatLng(listOf(startPnu, destPnuStation))

// 옵션: 두께/색 한 번에 지정 (width: Float, color: Int)
                val polylineOptions = PolylineOptions.from(
                    mapPoints,
                    8f,
                    Color.parseColor("#1565C0") // 파란색
                )

// 경로 추가
                shapeLayer.addPolyline(polylineOptions)

            }






        })
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }
}
