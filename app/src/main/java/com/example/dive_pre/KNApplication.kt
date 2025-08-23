package com.example.dive_pre

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.vectormap.KakaoMapSdk
import com.kakaomobility.knsdk.KNSDK

class KNApplication : Application() {
    companion object {
        lateinit var knsdk: KNSDK
    }
    override fun onCreate() {
        super.onCreate()
        initialize()
        createNotificationChannel()
        getFCMToken()
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

    /**
     * 알림 채널을 생성합니다 (Android O 이상)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "fcm_default_channel",
                "FCM 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Firebase Cloud Messaging 알림"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * FCM 토큰을 가져와서 콘솔에 출력합니다
     */
    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("FCM", "FCM Registration Token: $token")
        }
    }
}

