package com.example.dive_pre.api

import android.os.Build
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class LoginRequest(
    val user_id: String,
    val password: String,
    val fcm_token: String,
    val device_type: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val access_token: String,
    val token_type: String,
    val user: LoginUserData
)

data class LoginUserData(
    val user_id: String,
    val name: String,
    val phone_number: String
)

data class CareeStatusResponse(
    val has_registered_caree: Boolean
)

data class HomeInfoResponse(
    val protector_name: String,
    val caree_name: String,
    val safe_zone_active: Boolean?,
    val caree_in_safe_zone: Boolean?,
    val safe_zone_radius_meters: Int?
)

data class SafeZoneToggleResponse(
    val success: Boolean,
    val message: String,
    val safe_zone_active: Boolean
)

object ApiService {
    private val BASE_URL = com.example.dive_pre.BuildConfig.API_BASE_URL
    private const val LOGIN_ENDPOINT = "/api/user/login"
    private const val CAREE_STATUS_ENDPOINT = "/api/user/caree-status"
    private const val HOME_INFO_ENDPOINT = "/api/home/info"
    private const val SAFEZONE_TOGGLE_ENDPOINT = "/api/safezone/toggle"
    
    suspend fun login(
        userId: String, 
        password: String, 
        fcmToken: String
    ): LoginResponse {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            
            val deviceType = getDeviceType()
            
            val requestBody = JSONObject().apply {
                put("user_id", userId)
                put("password", password)
                put("fcm_token", fcmToken)
                put("device_type", deviceType)
            }.toString()
            
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBody.toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("$BASE_URL$LOGIN_ENDPOINT")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() 
                ?: throw Exception("응답이 비어있습니다")
            
            if (!response.isSuccessful) {
                throw Exception("HTTP 오류: ${response.code}")
            }
            
            val gson = Gson()
            gson.fromJson(responseBody, LoginResponse::class.java)
        }
    }
    
    suspend fun checkCareeStatus(accessToken: String): CareeStatusResponse {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            
            val request = Request.Builder()
                .url("$BASE_URL$CAREE_STATUS_ENDPOINT")
                .get()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() 
                ?: throw Exception("응답이 비어있습니다")
            
            if (!response.isSuccessful) {
                throw Exception("HTTP 오류: ${response.code}")
            }
            
            val gson = Gson()
            gson.fromJson(responseBody, CareeStatusResponse::class.java)
        }
    }
    
    suspend fun getHomeInfo(accessToken: String): HomeInfoResponse {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            
            val request = Request.Builder()
                .url("$BASE_URL$HOME_INFO_ENDPOINT")
                .get()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() 
                ?: throw Exception("응답이 비어있습니다")
            
            if (!response.isSuccessful) {
                throw Exception("HTTP 오류: ${response.code}")
            }
            
            val gson = Gson()
            gson.fromJson(responseBody, HomeInfoResponse::class.java)
        }
    }
    
    suspend fun toggleSafeZone(accessToken: String): SafeZoneToggleResponse {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            
            val request = Request.Builder()
                .url("$BASE_URL$SAFEZONE_TOGGLE_ENDPOINT")
                .post("".toRequestBody())
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() 
                ?: throw Exception("응답이 비어있습니다")
            
            if (!response.isSuccessful) {
                throw Exception("HTTP 오류: ${response.code}")
            }
            
            val gson = Gson()
            gson.fromJson(responseBody, SafeZoneToggleResponse::class.java)
        }
    }
    
    private fun getDeviceType(): String {
        return "android"
    }

    suspend fun getProtectorToCareeRoute(
        accessToken: String,
        priority: String? = "RECOMMEND",   // RECOMMEND | TIME | DISTANCE
        summary: Boolean? = true,
        alternatives: Boolean? = false,
        roadDetails: Boolean? = false,
        carFuel: String? = null,           // GASOLINE | DIESEL | LPG
        carHipass: Boolean? = null
    ): RouteResponse = withContext(Dispatchers.IO) {

        val ROUTE_ENDPOINT = "/navigation/route/protector-to-caree"


        val client = OkHttpClient()
        val gson = Gson()

        // BASE_URL + ROUTE_ENDPOINT + 쿼리
        // BASE_URL + ROUTE_ENDPOINT + 쿼리
        val url = ("$BASE_URL$ROUTE_ENDPOINT").toHttpUrlOrNull()
            ?.newBuilder()
            ?.apply {
                priority?.let { this.addQueryParameter("priority", it) }
                summary?.let { this.addQueryParameter("summary", it.toString()) }
                alternatives?.let { this.addQueryParameter("alternatives", it.toString()) }
                roadDetails?.let { this.addQueryParameter("road_details", it.toString()) }
                carFuel?.let { this.addQueryParameter("car_fuel", it) }
                carHipass?.let { this.addQueryParameter("car_hipass", it.toString()) }
            }
            ?.build() ?: throw IllegalStateException("잘못된 URL: $BASE_URL$ROUTE_ENDPOINT")


        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $accessToken") // ★ 필수
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("응답이 비어있습니다")

        if (!response.isSuccessful) {
            throw Exception("HTTP 오류: ${response.code} ${response.message}\n$responseBody")
        }

        gson.fromJson(responseBody, RouteResponse::class.java)
    }

}