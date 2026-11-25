package com.senac.viaja.data.model

data class MapCoordinate(val latitude: Double, val longitude: Double)

data class RouteRequest(
    val origin: MapCoordinate,
    val destination: MapCoordinate,
    val travelMode: String = "DRIVE"
)

data class RouteResponse(
    val distanceMeters: Int,
    val duration: String?,
    val encodedPolyline: String?
)

data class GeocodeResponse(
    val placeId: String?,
    val formattedAddress: String?,
    val latitude: Double,
    val longitude: Double
)
