package com.example.dive_pre.api

import com.kakao.vectormap.LatLng

// 백엔드 응답 형태에 맞춰 필요 필드만 남겨도 됩니다.
data class RouteResponse(
    val path: List<List<Double>>? = null,          // [[lon, lat], ...]
    val coordinates: List<List<Double>>? = null,   // [[lon, lat], ...]
    val points: List<Point>? = null,               // [{lat,lng}, ...]
    val polyline: String? = null                   // encoded polyline
) {
    data class Point(val lat: Double, val lng: Double)
}

/** 다양한 포맷을 LatLng 리스트로 표준화 */
fun RouteResponse.toLatLngs(): List<LatLng> {
    points?.let { return it.map { p -> LatLng.from(p.lat, p.lng) } }
    coordinates?.let { return it.map { p -> LatLng.from(p[1], p[0]) } }
    path?.let { return it.map { p -> LatLng.from(p[1], p[0]) } }
    polyline?.let { return decodeEncodedPolyline(it).map { LatLng.from(it.first, it.second) } }
    return emptyList()
}

/** Google Encoded Polyline Decoder (간단 구현) */
private fun decodeEncodedPolyline(encoded: String): List<Pair<Double, Double>> {
    val poly = ArrayList<Pair<Double, Double>>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lng += dlng

        poly.add(Pair(lat / 1E5, lng / 1E5))
    }
    return poly
}
