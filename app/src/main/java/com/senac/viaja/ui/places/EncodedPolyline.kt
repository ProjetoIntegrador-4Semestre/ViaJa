package com.senac.viaja.ui.places

import com.senac.viaja.data.model.MapCoordinate

internal fun decodePolyline(encoded: String): List<MapCoordinate> {
    val points = mutableListOf<MapCoordinate>()
    var index = 0
    var latitude = 0
    var longitude = 0
    while (index < encoded.length) {
        val latitudeResult = decodeValue(encoded, index)
        index = latitudeResult.nextIndex
        latitude += latitudeResult.value
        val longitudeResult = decodeValue(encoded, index)
        index = longitudeResult.nextIndex
        longitude += longitudeResult.value
        points += MapCoordinate(latitude / 100000.0, longitude / 100000.0)
    }
    return points
}

private fun decodeValue(encoded: String, startIndex: Int): DecodedValue {
    var index = startIndex
    var result = 0
    var shift = 0
    var value: Int
    do {
        require(index < encoded.length) { "Invalid encoded polyline" }
        value = encoded[index++].code - 63
        result = result or ((value and 0x1f) shl shift)
        shift += 5
    } while (value >= 0x20)
    val delta = if (result and 1 != 0) (result shr 1).inv() else result shr 1
    return DecodedValue(delta, index)
}

private data class DecodedValue(val value: Int, val nextIndex: Int)
