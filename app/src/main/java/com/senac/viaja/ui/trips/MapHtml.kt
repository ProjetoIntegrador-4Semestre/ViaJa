package com.senac.viaja.ui.trips
import com.senac.viaja.data.model.MapStop
internal object MapHtml {
    fun valid(points:List<MapStop>)=points.filter{
        it.lat!=null&&it.lng!=null&&it.lat.isFinite()&&it.lng.isFinite()&&it.lat in -90.0..90.0&&it.lng in -180.0..180.0
    }
    fun render(points:List<MapStop>):String{
        val json=com.google.gson.Gson().toJson(valid(points).map{
            mapOf("lat" to it.lat,"lng" to it.lng,"name" to it.name)
        })
        return """<!doctype html><meta name="viewport" content="width=device-width"><meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline' https://unpkg.com; script-src 'unsafe-inline' https://unpkg.com; img-src data: https://unpkg.com https://tile.openstreetmap.org; connect-src https://tile.openstreetmap.org;"><link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"><div id="map" style="height:100vh"></div><script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script><script>const points=$json;const map=L.map('map');L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:18,attribution:'© OpenStreetMap contributors'}).addTo(map);points.forEach(p=>{const label=document.createElement('span');label.textContent=p.name;L.marker([p.lat,p.lng]).addTo(map).bindTooltip(label)});map.fitBounds(points.map(p=>[p.lat,p.lng]),{maxZoom:14,padding:[20,20]});</script>"""
    }
}
