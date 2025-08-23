package com.example.dive_pre.api

import android.os.Build
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

data class LocationBothResponse(
    val protector_location: LocationInfo,
    val caree_location: LocationInfo
)

data class LocationInfo(
    val position_id: Int,
    val latitude: Double,
    val longitude: Double,
    val accuracy_meters: Double,
    val battery_level: Int,
    val is_inside_safe_zone: Boolean?,
    val recorded_at: String
)

data class DetailedNavigationResponse(
    val trans_id: String,
    val routes: List<DetailedRoute>
)

data class DetailedRoute(
    val result_code: Int,
    val result_msg: String,
    val summary: DetailedSummary,
    val sections: List<DetailedSection>,
    val distance: Int,
    val duration: Int
)

data class RoutePoint(
    val name: String,
    val x: Double,
    val y: Double
)

data class RouteBound(
    val min_x: Double,
    val min_y: Double,
    val max_x: Double,
    val max_y: Double
)

data class RouteFare(
    val taxi: Int,
    val toll: Int
)

data class DetailedSummary(
    val origin: RoutePoint,
    val destination: RoutePoint,
    val waypoints: List<RoutePoint>,
    val priority: String,
    val bound: RouteBound,
    val fare: RouteFare,
    val distance: Int,
    val duration: Int
)

data class DetailedSection(
    val distance: Int,
    val duration: Int,
    val bound: RouteBound,
    val roads: List<Road>,
    val guides: List<Guide>
)

data class Road(
    val name: String,
    val distance: Int,
    val duration: Int,
    val traffic_speed: Int,
    val traffic_state: Int,
    val vertexes: List<List<Double>>
)

data class Guide(
    val name: String,
    val x: Double,
    val y: Double,
    val distance: Int,
    val duration: Int,
    val type: Int,
    val guidance: String,
    val road_index: Int
)

object ApiService {
    private val BASE_URL = com.example.dive_pre.BuildConfig.API_BASE_URL
    private const val LOGIN_ENDPOINT = "/api/user/login"
    private const val CAREE_STATUS_ENDPOINT = "/api/user/caree-status"
    private const val HOME_INFO_ENDPOINT = "/api/home/info"
    private const val SAFEZONE_TOGGLE_ENDPOINT = "/api/safezone/toggle"
    private const val LOCATION_BOTH_ENDPOINT = "/api/location/both"
    private const val DETAILED_NAVIGATION_ENDPOINT = "/navigation/route/protector-to-caree"
    
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
    
    suspend fun getBothLocations(accessToken: String): LocationBothResponse {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            
            val request = Request.Builder()
                .url("$BASE_URL$LOCATION_BOTH_ENDPOINT")
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
            gson.fromJson(responseBody, LocationBothResponse::class.java)
        }
    }
    
    suspend fun getDetailedNavigationRoute(accessToken: String): DetailedNavigationResponse {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            
            val url = "$BASE_URL$DETAILED_NAVIGATION_ENDPOINT" +
                "?priority=RECOMMEND" +
                "&summary=false" +
                "&alternatives=false" +
                "&road_details=false" +
                "&car_fuel=GASOLINE" +
                "&car_hipass=false"
            
            val request = Request.Builder()
                .url(url)
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
            gson.fromJson(responseBody, DetailedNavigationResponse::class.java)
        }
    }
    
    private fun getDeviceType(): String {
        return "android"
    }
}