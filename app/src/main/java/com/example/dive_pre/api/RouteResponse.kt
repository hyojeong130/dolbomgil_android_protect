package com.example.dive_pre.api

import com.kakao.vectormap.LatLng

// curl 응답 구조에 맞춘 RouteResponse
data class RouteResponse(
    val trans_id: String,
    val routes: List<Route>
)

data class Route(
    val result_code: Int,
    val result_msg: String,
    val summary: RouteSummary,
    val sections: List<RouteSection>,
    val distance: Int,
    val duration: Int
)

data class RouteSummary(
    val origin: RoutePoint,
    val destination: RoutePoint,
    val waypoints: List<RoutePoint>,
    val priority: String,
    val bound: RouteBound,
    val fare: RouteFare,
    val distance: Int,
    val duration: Int
)

data class RoutePoint(
    val name: String,
    val x: Double,  // longitude
    val y: Double   // latitude
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

data class RouteSection(
    val distance: Int,
    val duration: Int,
    val bound: RouteBound,
    val roads: List<RouteRoad>,
    val guides: List<RouteGuide>
)

data class RouteRoad(
    val name: String,
    val distance: Int,
    val duration: Int,
    val traffic_speed: Int,
    val traffic_state: Int,
    val vertexes: List<List<Double>>  // [[lon, lat], [lon, lat], ...]
)

data class RouteGuide(
    val name: String,
    val x: Double,  // longitude
    val y: Double,  // latitude
    val distance: Int,
    val duration: Int,
    val type: Int,
    val guidance: String,
    val road_index: Int
)

/** 경로의 모든 좌표를 LatLng 리스트로 변환 */
fun RouteResponse.toLatLngs(): List<LatLng> {
    val allLatLngs = mutableListOf<LatLng>()
    
    routes.firstOrNull()?.sections?.forEach { section ->
        section.roads.forEach { road ->
            road.vertexes.forEach { vertex ->
                if (vertex.size >= 2) {
                    // vertex는 [lon, lat] 형태이므로 LatLng.from(lat, lon)으로 변환
                    allLatLngs.add(LatLng.from(vertex[1], vertex[0]))
                }
            }
        }
    }
    
    return allLatLngs
}

/** 경로 요약 정보 로그 출력 */
fun RouteResponse.logRouteInfo() {
    routes.firstOrNull()?.let { route ->
        println("=== 경로 정보 ===")
        println("결과: ${route.result_msg}")
        println("거리: ${route.distance}m")
        println("시간: ${route.duration}초")
        println("출발지: (${route.summary.origin.y}, ${route.summary.origin.x})")
        println("목적지: (${route.summary.destination.y}, ${route.summary.destination.x})")
        println("좌표 개수: ${toLatLngs().size}")
        println("=================")
    }
}
