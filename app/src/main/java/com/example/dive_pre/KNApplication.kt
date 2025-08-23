package com.example.dive_pre

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.vectormap.KakaoMapSdk
import com.kakaomobility.knsdk.KNSDK
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

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
        printHashKey() // 해시 키값 로그 출력
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

    /**
     * 앱의 해시 키값을 로그에 출력합니다
     */
    private fun printHashKey() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // API 28 이상에서는 GET_SIGNING_CERTIFICATES 사용
                val info: PackageInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                info.signingInfo?.apkContentsSigners?.forEach { cert ->
                    val md: MessageDigest = MessageDigest.getInstance("SHA")
                    md.update(cert.toByteArray())
                    val hashKey = android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT)
                    Log.d("HashKey", "해시 키값 (API 28+): $hashKey")
                }
            } else {
                // API 28 미만에서는 GET_SIGNATURES 사용
                val info: PackageInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                info.signatures?.forEach { signature ->
                    val md: MessageDigest = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val hashKey = android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT)
                    Log.d("HashKey", "해시 키값 (API < 28): $hashKey")
                }
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("HashKey", "해시 알고리즘을 찾을 수 없습니다", e)
        } catch (e: Exception) {
            Log.e("HashKey", "해시 키값을 가져오는 중 오류가 발생했습니다", e)
        }
    }
}

