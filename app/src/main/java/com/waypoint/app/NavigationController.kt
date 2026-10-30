package com.waypoint.app

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.GnssStatus
import android.os.Handler
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

val PreviewCenter = Coordinate(34.0495,-118.2505)

data class NavState(
    val world: World = World.LOS_SANTOS,
    val mode: TravelMode = TravelMode.CAR,
    val fix: Coordinate? = null,
    val accuracy: Float = 0f,
    val bearing: Float = 0f,
    val speed: Float = 0f,
    val headingStatus: String = "Waiting for heading",
    val headingReliable: Boolean = false,
    val headingSource: HeadingSource = HeadingSource.NONE,
    val sensorDescription: String = "Checking phone sensors",
    val locationProvider: String = "Not connected",
    val satellitesUsed: Int? = null,
    val satellitesVisible: Int? = null,
    val gpsStatus: String = "GPS not connected",
    val gpsReady: Boolean = false,
    val city: String = "Los Angeles · map preview",
    val selected: Place? = null,
    val route: RoadRoute? = null,
    val progress: RouteProgress? = null,
    val navigating: Boolean = false,
    val arrived: Boolean = false,
    val routing: Boolean = false,
    val routeError: String? = null,
    val query: String = "",
    val searchResults: List<Place> = emptyList(),
    val searching: Boolean = false,
    val searchError: String? = null,
    val voice: Boolean = true,
    val imperial: Boolean = false,
    val saved: List<Place> = emptyList(),
    val recent: List<Place> = emptyList(),
    val message: String? = null,
    val cameraRequest: Int = 0,
    val tilted: Boolean = true,
    val carNight: Boolean = false,
    val carDayWorld: World = World.HORIZON,
    val carNightWorld: World = World.LOS_SANTOS,
)

class WaypointApplication : Application() {
    override fun onCreate() { super.onCreate();MapGlyphs.install(this) }
    val navigation by lazy { NavigationController(this) }
    val updater by lazy { AppUpdater(this) }
}

class NavigationController(private val app: Application) {
    private val prefs = app.getSharedPreferences("waypoint",Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private val repository = MapRepository()
    private val _state = MutableStateFlow(NavState(
        world = World.entries.find{it.name==prefs.getString("world",null)} ?: World.LOS_SANTOS,
        mode = TravelMode.entries.find{it.name==prefs.getString("mode",null)} ?: TravelMode.CAR,
        voice = prefs.getBoolean("voice",true), imperial = prefs.getBoolean("imperial",false),
        saved = readPlaces("saved"), recent = readPlaces("recent"),
        tilted = prefs.getBoolean("tilted",true),
        carNight = prefs.getBoolean("carNight",false),
        carDayWorld = World.entries.find{it.name==prefs.getString("carDayWorld",null)} ?: World.HORIZON,
        carNightWorld = World.entries.find{it.name==prefs.getString("carNightWorld",null)} ?: World.LOS_SANTOS
    ))
    val state = _state.asStateFlow()
    private val manager = app.getSystemService(LocationManager::class.java)
    private var visible = false
    private var carVisible = false
    private var enabled = prefs.getBoolean("locationEnabled",false)
    private var listening = false
    private val fused = LocationServices.getFusedLocationProviderClient(app)
    private val headingArbiter=HeadingArbiter()
    private var headingUiVisible=true
    private fun applyHeading(reading:HeadingReading) {
        // Lists do not need ten sensor-driven recompositions each second. The arbiter
        // still receives every sample; opening the map publishes its current reading.
        if(!headingUiVisible && !carVisible)return
        _state.update {
            if(it.headingReliable==reading.reliable && it.headingSource==reading.source && it.headingStatus==reading.status &&
                kotlin.math.abs(headingDelta(it.bearing,reading.degrees))<.3f) it
            else it.copy(bearing=reading.degrees,headingReliable=reading.reliable,headingSource=reading.source,headingStatus=reading.status)
        }
    }
    fun setHeadingUiVisible(value:Boolean) {
        headingUiVisible=value
        if(value)applyHeading(headingArbiter.current(SystemClock.elapsedRealtime()))
    }
    private val heading = HeadingSensor(app) { degrees,reliable,status ->
        applyHeading(headingArbiter.compass(degrees,reliable,status,SystemClock.elapsedRealtime()))
    }
    private val fusedCallback=object:LocationCallback() {
        override fun onLocationResult(result:LocationResult) { if(listening) result.lastLocation?.let(::receiveLocation) }
    }
    private val gnssCallback=object:GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status:GnssStatus) {
            _state.update{it.copy(satellitesUsed=(0 until status.satelliteCount).count{index->status.usedInFix(index)},satellitesVisible=status.satelliteCount)}
        }
        override fun onStopped() { _state.update{it.copy(satellitesUsed=null,satellitesVisible=null)} }
    }
    private var lastLocationNanos=0L
    private var lastFixElapsed = 0L
    private var lastGpsElapsed = 0L
    private var lastReroute = 0L
    private var offRouteCount = 0
    private var routeJob: Job? = null
    private var searchJob: Job? = null
    private var routeOrigin: Coordinate? = null
    private var engine: NavigationEngine? = null
    private var spoken = mutableSetOf<String>()
    private var cityLookupPending = false
    private var lastCityAttempt = -60000L
    private var cityPoint: Coordinate? = null
    private var ttsReady = false
    private lateinit var tts: TextToSpeech
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) = receiveLocation(location)
        override fun onProviderDisabled(provider: String) { if(provider == LocationManager.GPS_PROVIDER) _state.update{it.copy(gpsReady=false,gpsStatus="Turn on device location")} }
        override fun onProviderEnabled(provider: String) { _state.update{it.copy(gpsStatus="Finding GPS…")} }
        @Deprecated("Deprecated in Android") override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }
    init {
        tts = TextToSpeech(app) { result ->
            ttsReady = result == TextToSpeech.SUCCESS
            if(ttsReady) {
                val supported = tts.setLanguage(Locale.US)
                ttsReady = supported != TextToSpeech.LANG_MISSING_DATA && supported != TextToSpeech.LANG_NOT_SUPPORTED
                tts.setSpeechRate(0.95f)
            }
        }
        scope.launch {
            while(isActive) {
                delay(1000)
                if(listening)applyHeading(headingArbiter.current(SystemClock.elapsedRealtime()))
                if(listening && lastFixElapsed>0 && SystemClock.elapsedRealtime()-lastFixElapsed>15000) {
                    _state.update{it.copy(gpsReady=false,gpsStatus="GPS signal lost · waiting for a fix",speed=0f,headingReliable=false)}
                }
            }
        }
    }
    fun setDisplayRotation(rotation:Int) { heading.displayRotation=rotation }
    fun hasLocationPermission() = ContextCompat.checkSelfPermission(app,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
    fun setCarVisible(value: Boolean) { carVisible=value; if(value) enabled=true; updateTracking() }
    fun setCarNight(value: Boolean) { _state.update{it.copy(carNight=value)}; prefs.edit().putBoolean("carNight",value).apply() }
    fun setCarWorld(world: World, night: Boolean) { _state.update{if(night)it.copy(carNightWorld=world) else it.copy(carDayWorld=world)}; prefs.edit().putString(if(night)"carNightWorld" else "carDayWorld",world.name).apply() }
    fun handleNavigationIntent(intent: Intent?) {
        val uri=intent?.data ?: return
        if(uri.scheme !in listOf("geo","google.navigation")) return
        val raw=uri.schemeSpecificPart
        val q=android.net.Uri.decode(if("q=" in raw) raw.substringAfter("q=").substringBefore("&") else raw.substringBefore("?"))
        val match=Regex("^(-?[0-9]+(?:\\.[0-9]+)?),\\s*(-?[0-9]+(?:\\.[0-9]+)?)").find(q)
        if(match!=null) {
            val lat=match.groupValues[1].toDouble();val lon=match.groupValues[2].toDouble()
            if(lat in -90.0..90.0 && lon in -180.0..180.0) select(Place("shared:$lat,$lon",q.substringAfter('(',"Shared destination").substringBefore(')'),"Shared location",Coordinate(lat,lon)))
        } else if(q.isNotBlank()) scope.launch {
            runCatching{repository.search(q,_state.value.fix ?: PreviewCenter)}.onSuccess{places->places.firstOrNull()?.let{select(it)}}.onFailure{_state.update{it.copy(message="Couldn't find the shared destination. Search by address.")}}
        }
    }
    fun clearHistory() { _state.update{it.copy(recent=emptyList(),saved=emptyList())}; prefs.edit().remove("recent").remove("saved").apply() }
    fun setVisible(value: Boolean) { visible=value; updateTracking() }
    fun enableLocation() { enabled=true; prefs.edit().putBoolean("locationEnabled",true).apply(); _state.update{it.copy(gpsStatus="Finding GPS…",cameraRequest=it.cameraRequest+1)}; updateTracking() }
    private fun updateTracking() {
        val should = hasLocationPermission() && enabled && (visible || carVisible || _state.value.navigating)
        if(should && !listening) {
            try {
                listening=true
                _state.update{it.copy(gpsStatus="Finding GPS…",sensorDescription=heading.description)}
                headingArbiter.reset()
                heading.start()
                manager.registerGnssStatusCallback(gnssCallback,Handler(app.mainLooper))
                if(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(app)==ConnectionResult.SUCCESS) {
                    _state.update{it.copy(locationProvider="Fused location · GNSS / Wi-Fi / cell")}
                    val request=LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000L)
                        .setMinUpdateIntervalMillis(500L).setMaxUpdateDelayMillis(0L)
                        .setWaitForAccurateLocation(true).build()
                    fused.requestLocationUpdates(request,fusedCallback,app.mainLooper)
                        .addOnSuccessListener{if(!listening)fused.removeLocationUpdates(fusedCallback)}
                        .addOnFailureListener{if(listening)startPlatformLocation()}
                } else startPlatformLocation()
                if(!androidx.core.location.LocationManagerCompat.isLocationEnabled(manager)) _state.update{it.copy(gpsReady=false,gpsStatus="Turn on device location")}
            } catch (_: SecurityException) { stopTracking();permissionDenied() }
        } else if(!should && listening) {
            stopTracking()
            _state.update{it.copy(gpsReady=false,headingReliable=false)}
        }
    }
    @android.annotation.SuppressLint("MissingPermission")
    private fun startPlatformLocation() {
        if(!hasLocationPermission()){stopTracking();permissionDenied();return}
        try {
            manager.removeUpdates(locationListener)
            for(provider in listOf(LocationManager.GPS_PROVIDER,LocationManager.NETWORK_PROVIDER)) {
                if(manager.allProviders.contains(provider)) manager.requestLocationUpdates(provider,1000L,1f,locationListener,app.mainLooper)
            }
            _state.update{it.copy(locationProvider="Android GNSS + network fallback")}
        } catch(_:SecurityException){stopTracking();permissionDenied()}
    }
    private fun stopTracking() {
        listening=false
        manager.removeUpdates(locationListener)
        fused.removeLocationUpdates(fusedCallback)
        manager.unregisterGnssStatusCallback(gnssCallback)
        heading.stop()
    }
    fun permissionDenied() { _state.update{it.copy(gpsReady=false,gpsStatus="Precise location required",message="Allow precise location in Android settings to navigate.")} }
    private fun receiveLocation(location: Location) {
        if(!listening)return
        val now = SystemClock.elapsedRealtime()
        if(location.elapsedRealtimeNanos<=lastLocationNanos)return
        if((SystemClock.elapsedRealtimeNanos()-location.elapsedRealtimeNanos)/1_000_000 > 15000) return
        if(location.provider==LocationManager.NETWORK_PROVIDER && now-lastGpsElapsed<10000) return
        if(location.provider==LocationManager.GPS_PROVIDER) lastGpsElapsed=now
        lastLocationNanos=location.elapsedRealtimeNanos
        heading.updateLocation(location)
        val fix = Coordinate(location.latitude,location.longitude)
        val first = _state.value.fix==null
        lastFixElapsed=now
        val reliable = location.hasAccuracy() && location.accuracy <= 60f
        _state.update{it.copy(fix=fix,accuracy=location.accuracy,gpsReady=reliable,
            gpsStatus=if(reliable) "GPS connected" else "Improving GPS accuracy…",
            speed=if(location.hasSpeed()) location.speed else 0f,
            city=if(first)"Current area" else it.city,
            cameraRequest=it.cameraRequest+if(first) 1 else 0)}
        applyHeading(headingArbiter.location(
            bearing=if(location.hasBearing())location.bearing else null,
            speed=if(location.hasSpeed())location.speed else 0f,
            bearingAccuracy=if(location.hasBearingAccuracy())location.bearingAccuracyDegrees else null,
            speedAccuracy=if(location.hasSpeedAccuracy())location.speedAccuracyMetersPerSecond else null,
            fixReliable=reliable,mode=_state.value.mode,now=now))
        if(reliable && !cityLookupPending && now-lastCityAttempt>=60000 && (cityPoint==null || distance(cityPoint!!,fix)>5000)) {
            cityLookupPending=true;lastCityAttempt=now
            _state.update{it.copy(city="Current area")}
            scope.launch {
                try { val label=repository.reverse(fix);cityPoint=fix;_state.update{it.copy(city=label.ifBlank{"Current area"})} }
                catch(_: Exception) { /* Keep the geographic-neutral label; a later fix retries. */ }
                finally { cityLookupPending=false }
            }
        }
        if(!reliable) return
        val s = _state.value
        if(s.navigating) {
            val progress = engine?.update(fix, maxOf(500.0,location.speed*20.0)) ?: return
            _state.update{it.copy(progress=progress)}
            if(progress.offRoute > maxOf(55.0,location.accuracy*1.5)) offRouteCount++ else offRouteCount=0
            if(offRouteCount>=3 && now-lastReroute>15000 && !s.routing) {
                lastReroute=now; speak("Rerouting. Finding a new route."); requestRoute(true); return
            }
            if(engine?.hasArrived(fix,progress)==true) {
                speak("You have arrived at your destination.")
                _state.update{it.copy(navigating=false,arrived=true)}
                app.stopService(Intent(app,NavigationService::class.java)); updateTracking(); return
            }
            if(progress.offRoute < 50 && !s.routing) {
                val step=s.route?.steps?.getOrNull(progress.nextStep) ?: return
                val threshold = if(progress.toTurn<=35) "now" else if(progress.toTurn<=180) "soon" else ""
                val key="${progress.nextStep}:$threshold"
                if(threshold.isNotEmpty() && spoken.add(key)) speak(if(threshold=="now") step.instruction else "In ${formatDistance(progress.toTurn,s.imperial)}, ${step.instruction}")
            }
        } else if(s.selected!=null && s.route==null && !s.routing && s.routeError==null && !s.arrived) requestRoute()
    }
    fun select(place: Place) {
        stopNavigation(clear=false)
        _state.update{it.copy(selected=place,route=null,progress=null,routeError=null,arrived=false,cameraRequest=it.cameraRequest+1)}
        if(_state.value.gpsReady) requestRoute()
    }
    fun clearDestination() { stopNavigation(); _state.update{it.copy(selected=null,route=null,progress=null,arrived=false,routeError=null)} }
    fun requestRoute(reroute: Boolean = false) {
        val s = _state.value; val origin = s.fix ?: return; val target = s.selected ?: return
        if(!s.gpsReady) { _state.update{it.copy(message="Waiting for an accurate GPS fix.")}; return }
        routeJob?.cancel()
        _state.update{it.copy(routing=true,routeError=null)}
        routeJob = scope.launch {
            try {
                val route = repository.route(origin,target.point,s.mode)
                ensureActive()
                routeOrigin=origin; engine=NavigationEngine(route); spoken.clear(); offRouteCount=0
                _state.update{it.copy(route=route,progress=engine!!.update(it.fix ?: origin),routing=false,routeError=null,cameraRequest=it.cameraRequest+if(reroute) 0 else 1)}
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { _state.update{it.copy(routing=false,routeError=e.message ?: "Couldn't load the route. Check your connection.")} }
        }
    }
    fun startNavigation() {
        val s = _state.value
        if(!s.gpsReady || s.fix==null || !hasLocationPermission()) { _state.update{it.copy(message="Enable precise GPS and wait for a location fix.")}; return }
        if(s.route==null || s.routing || s.route.mode!=s.mode) return
        if((routeOrigin?.let{distance(it,s.fix)} ?: 0.0)>100.0) { requestRoute(); _state.update{it.copy(message="Updating the route from your current position. Start when it's ready.")}; return }
        if(s.routeError!=null) { requestRoute(); return }
        engine=NavigationEngine(s.route); spoken.clear()
        _state.update{it.copy(navigating=true,arrived=false,cameraRequest=it.cameraRequest+1)}
        try { ContextCompat.startForegroundService(app,Intent(app,NavigationService::class.java)) }
        catch(_: Exception) { _state.update{it.copy(navigating=false,message="Android couldn't start navigation. Keep the app open and try again.")}; return }
        s.selected?.let { place ->
            val recent = (listOf(place)+s.recent.filter{it.id!=place.id}).take(12)
            _state.update{it.copy(recent=recent)}; writePlaces("recent",recent)
        }
        speak("Starting navigation. ${s.route.steps.first().instruction}.")
        if(s.voice && !ttsReady) _state.update{it.copy(message="Voice unavailable. Install an English text-to-speech voice in Android settings. Visual guidance is active.")}
    }
    fun stopNavigation(clear: Boolean = true) {
        routeJob?.cancel(); routeJob=null; tts.stop()
        _state.update{it.copy(navigating=false,routing=false,arrived=if(clear) false else it.arrived)}
        app.stopService(Intent(app,NavigationService::class.java)); updateTracking()
    }
    fun serviceStopped() { if(_state.value.navigating) { _state.update{it.copy(navigating=false,message="Navigation stopped.")}; tts.stop(); updateTracking() } }
    fun search(query: String) {
        searchJob?.cancel(); _state.update{it.copy(query=query,searching=query.length>=2,searchResults=emptyList(),searchError=null)}
        if(query.trim().length<2) { _state.update{it.copy(searching=false)}; return }
        searchJob=scope.launch {
            delay(500)
            try {
                val places=repository.search(query,_state.value.fix ?: PreviewCenter); ensureActive()
                _state.update{it.copy(searchResults=places,searching=false)}
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { _state.update{it.copy(searching=false,searchError=e.message ?: "Search unavailable. Try again.")} }
        }
    }
    fun changeMode(mode:TravelMode) {
        if(_state.value.navigating || _state.value.mode==mode)return
        routeJob?.cancel()
        _state.update{it.copy(mode=mode,route=null,progress=null,routing=false,routeError=null)}
        prefs.edit().putString("mode",mode.name).apply()
        if(_state.value.selected!=null && _state.value.gpsReady)requestRoute()
    }
    fun changeWorld(world: World) { _state.update{it.copy(world=world)}; prefs.edit().putString("world",world.name).apply() }
    fun toggleVoice() { _state.update{it.copy(voice=!it.voice)}; prefs.edit().putBoolean("voice",_state.value.voice).apply(); if(!_state.value.voice) tts.stop() }
    fun toggleUnits() { _state.update{it.copy(imperial=!it.imperial)}; prefs.edit().putBoolean("imperial",_state.value.imperial).apply() }
    fun toggleTilt() { _state.update{it.copy(tilted=!it.tilted,cameraRequest=it.cameraRequest+1)};prefs.edit().putBoolean("tilted",_state.value.tilted).apply() }
    fun recenter() { _state.update{it.copy(cameraRequest=it.cameraRequest+1)} }
    fun dismissMessage() { _state.update{it.copy(message=null)} }
    fun toggleSaved(place: Place) {
        val existing=_state.value.saved
        val next=if(existing.any{it.id==place.id}) existing.filter{it.id!=place.id} else existing+place
        _state.update{it.copy(saved=next)}; writePlaces("saved",next)
    }
    private fun speak(text: String) { if(_state.value.voice && ttsReady) tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"guidance") }
    private fun readPlaces(key: String): List<Place> = runCatching {
        val a=JSONArray(prefs.getString(key,"[]")); (0 until a.length()).map {
            val p=a.getJSONObject(it); Place(p.getString("id"),p.getString("name"),p.getString("address"),Coordinate(p.getDouble("lat"),p.getDouble("lon")))
        }
    }.getOrDefault(emptyList())
    private fun writePlaces(key: String, places: List<Place>) {
        val a=JSONArray(); places.forEach { a.put(JSONObject().put("id",it.id).put("name",it.name).put("address",it.address).put("lat",it.point.lat).put("lon",it.point.lon)) }
        prefs.edit().putString(key,a.toString()).apply()
    }
}
