package com.example.dive_pre.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FCMUtils {
    private const val TAG = "FCMUtils"
    
    suspend fun getFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM token retrieved: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve FCM token", e)
            null
        }
    }
}