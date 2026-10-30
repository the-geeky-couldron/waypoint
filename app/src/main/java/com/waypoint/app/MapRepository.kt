package com.waypoint.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MapRepository(private val photonBase:String="https://photon.komoot.io", private val routingBase:String="https://router.project-osrm.org", private val valhallaBase:String="https://valhalla1.openstreetmap.de") {
    private val routeMutex=Mutex()
    private var lastRouteCall=0L
    private val client = OkHttpClient.Builder().connectTimeout(12,TimeUnit.SECONDS).readTimeout(20,TimeUnit.SECONDS).callTimeout(25,TimeUnit.SECONDS).build()
    private fun json(url: String): JSONObject = client.newCall(Request.Builder().url(url).header("User-Agent","WaypointAndroid/0.1 (personal navigation prototype)").header("X-Client-Id","waypoint-personal-android").build()).execute().use {
        check(it.isSuccessful) { if (it.code == 429) "Service is busy. Try again shortly." else "Map service unavailable (${it.code}). Try again." }
        JSONObject(it.body?.string() ?: error("Empty response"))
    }
    suspend fun search(query: String, near: Coordinate): List<Place> = withContext(Dispatchers.IO) {
        val features = json("${photonBase.trimEnd('/')}/api/?q=${URLEncoder.encode(query,"UTF-8")}&lat=${near.lat}&lon=${near.lon}&limit=8&lang=en").getJSONArray("features")
        (0 until features.length()).map { i ->
            val f = features.getJSONObject(i); val p = f.getJSONObject("properties"); val c = f.getJSONObject("geometry").getJSONArray("coordinates")
            Place("${p.optString("osm_type")}${p.optLong("osm_id")}",p.optString("name").ifBlank { p.optString("street","Dropped pin") },
                listOf(p.optString("housenumber"),p.optString("street"),p.optString("city"),p.optString("country")).filter{it.isNotBlank()}.distinct().joinToString(", "),
                Coordinate(c.getDouble(1),c.getDouble(0)),p.optString("osm_value","place"))
        }.distinctBy { it.id }
    }
    suspend fun reverse(point: Coordinate): String = withContext(Dispatchers.IO) {
        val p = json("${photonBase.trimEnd('/')}/reverse?lat=${point.lat}&lon=${point.lon}&limit=1&lang=en").getJSONArray("features").optJSONObject(0)?.optJSONObject("properties")
        p?.optString("city")?.ifBlank { p.optString("county") }?.ifBlank { "Your location" } ?: "Your location"
    }
    suspend fun route(from: Coordinate, to: Coordinate,mode:TravelMode=TravelMode.CAR): RoadRoute = withContext(Dispatchers.IO) {
        val obj = if(mode==TravelMode.CAR) json("${routingBase.trimEnd('/')}/route/v1/driving/${from.lon},${from.lat};${to.lon},${to.lat}?overview=full&geometries=geojson&steps=true")
        else routeMutex.withLock {
            val elapsed=(System.nanoTime()-lastRouteCall)/1_000_000
            if(elapsed<1100)delay(1100-elapsed)
            lastRouteCall=System.nanoTime()
            val payload=JSONObject().put("locations",JSONArray().put(JSONObject().put("lat",from.lat).put("lon",from.lon)).put(JSONObject().put("lat",to.lat).put("lon",to.lon)))
                .put("costing",mode.costing).put("format","osrm").put("shape_format","geojson")
                .put("units","kilometers").put("language","en-US")
            json("${valhallaBase.trimEnd('/')}/route?json=${URLEncoder.encode(payload.toString(),"UTF-8")}")
        }
        check(obj.optString("code") == "Ok") { "No ${mode.routeDescription} route found. Choose another destination." }
        val r = obj.getJSONArray("routes").getJSONObject(0)
        val coords = r.getJSONObject("geometry").getJSONArray("coordinates")
        val points = (0 until coords.length()).map { coords.getJSONArray(it).let { c -> Coordinate(c.getDouble(1),c.getDouble(0)) } }
        val legs = r.getJSONArray("legs")
        val steps = buildList {
            for (i in 0 until legs.length()) {
                val s = legs.getJSONObject(i).getJSONArray("steps")
                for (j in 0 until s.length()) {
                    val step = s.getJSONObject(j); val m = step.getJSONObject("maneuver"); val c = m.getJSONArray("location")
                    add(Maneuver(Coordinate(c.getDouble(1),c.getDouble(0)),m.getString("type"),m.optString("modifier"),step.optString("name").ifBlank { step.optString("ref") },step.getDouble("distance"),step.getDouble("duration"),m.optInt("exit")))
                }
            }
        }
        check(points.size>=2 && steps.isNotEmpty() && r.getDouble("distance")>0) { "That destination is too close. Choose another place." }
        check(distance(from,points.first()) < 1500 && distance(to,points.last()) < 1500) { "No nearby road access found for this location." }
        RoadRoute(points,steps,r.getDouble("distance"),r.getDouble("duration"),mode)
    }
}
