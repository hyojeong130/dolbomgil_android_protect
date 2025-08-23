package com.example.dive_pre.utils

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "app_preferences"
    private const val ACCESS_TOKEN_KEY = "access_token"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveAccessToken(context: Context, token: String) {
        getPreferences(context)
            .edit()
            .putString(ACCESS_TOKEN_KEY, token)
            .apply()
    }
    
    fun getAccessToken(context: Context): String? {
        return getPreferences(context).getString(ACCESS_TOKEN_KEY, null)
    }
    
    fun clearAccessToken(context: Context) {
        getPreferences(context)
            .edit()
            .remove(ACCESS_TOKEN_KEY)
            .apply()
    }
    
    fun isLoggedIn(context: Context): Boolean {
        return getAccessToken(context) != null
    }
}