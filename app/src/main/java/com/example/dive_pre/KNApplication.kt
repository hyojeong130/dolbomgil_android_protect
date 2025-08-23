package com.example.dive_pre

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk
import com.kakaomobility.knsdk.KNSDK

class KNApplication : Application() {
    companion object {
        lateinit var knsdk: KNSDK
    }
    override fun onCreate() {
        super.onCreate()
        initialize()
        initializeMap()
    }

    fun initializeMap(){
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_MAP_APP_KEY)
    }

    /**
     * 길찾기 SDK의 초기화 및 파일이 저장될 경로를 설정합니다
     */
    fun initialize() {
        knsdk = KNSDK.apply {
            //  파일 경로: data/data/com.kakaomobility.knsample/files/KNSample
            install(this@KNApplication, "$filesDir/KNSample")
        }
    }
}

