package com.waypoint.app

import android.animation.ValueAnimator
import android.os.SystemClock
import kotlinx.coroutines.delay
import android.view.animation.LinearInterpolator
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.*
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.style.layers.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.*
import org.maplibre.android.style.expressions.Expression.*

/** Physical pixels, measured from the native map's edges to the uncovered viewport. */
data class MapViewportInsets(val left:Int,val top:Int,val right:Int,val bottom:Int)

@Composable
fun WorldMap(state: NavState, onPin: (Place)->Unit, onMapError: (String?)->Unit, modifier: Modifier=Modifier,active:Boolean=true,routeInsets:MapViewportInsets?=null,onBearing:(Float)->Unit={}) {
    val context=LocalContext.current
    val owner=LocalLifecycleOwner.current
    val currentState by rememberUpdatedState(state)
    val currentActive by rememberUpdatedState(active)
    val currentPin by rememberUpdatedState(onPin)
    val currentError by rememberUpdatedState(onMapError)
    val currentBearing by rememberUpdatedState(onBearing)
    val mapView=remember { MapLibre.getInstance(context); MapView(context).apply { onCreate(null) } }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleReady by remember { mutableIntStateOf(0) }
    var loadedWorld by remember { mutableStateOf<World?>(null) }
    val followPolicy=remember{MapFollowPolicy()}
    var followTick by remember { mutableIntStateOf(0) }
    var cameraFix by remember { mutableStateOf<Coordinate?>(null) }
    var cameraBearing by remember { mutableFloatStateOf(0f) }
    val renderedBearing=remember{floatArrayOf(0f)}
    val pointerAnimator=remember{arrayOfNulls<ValueAnimator>(1)}
    val positionAnimator=remember{arrayOfNulls<ValueAnimator>(1)}
    val renderedPosition=remember{arrayOfNulls<Coordinate>(1)}
    val styleEpoch=remember{intArrayOf(0)}
    val renderLifecycle=remember(mapView){MapRenderLifecycle(mapView)}
    DisposableEffect(mapView,owner) {
        val observer=LifecycleEventObserver { _,_ -> renderLifecycle.update(owner.lifecycle.currentState,currentActive) }
        owner.lifecycle.addObserver(observer)
        renderLifecycle.update(owner.lifecycle.currentState,currentActive)
        onDispose {styleEpoch[0]++;positionAnimator[0]?.cancel();pointerAnimator[0]?.cancel();owner.lifecycle.removeObserver(observer);renderLifecycle.destroy()}
    }
    LaunchedEffect(active) {renderLifecycle.update(owner.lifecycle.currentState,active)}
    AndroidView(factory={mapView.apply {
        addOnDidFailLoadingMapListener { currentError("Map couldn't load. Check your internet connection.") }
        getMapAsync { m ->
            map=m; m.uiSettings.apply {
                isCompassEnabled=false; isLogoEnabled=false; isAttributionEnabled=false
                setAllGesturesEnabled(true)
            }
            m.cameraPosition=CameraPosition.Builder().target(PreviewCenter.latLng()).zoom(15.2).tilt(35.0).bearing(-22.0).build()
            m.addOnCameraMoveListener { currentBearing(m.cameraPosition.bearing.toFloat()) }
            m.addOnCameraMoveStartedListener { reason -> if(reason==MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                followPolicy.gestureStarted(SystemClock.elapsedRealtime());followTick++
            } }
            m.addOnCameraIdleListener { if(followPolicy.gestureEnded(SystemClock.elapsedRealtime()))followTick++ }
            m.addOnMapLongClickListener { point ->
                if(!currentState.navigating) currentPin(Place("pin:${point.latitude},${point.longitude}","Dropped pin","%.5f, %.5f".format(point.latitude,point.longitude),Coordinate(point.latitude,point.longitude)))
                true
            }
            m.addOnMapClickListener { point ->
                val features=m.queryRenderedFeatures(m.projection.toScreenLocation(point))
                val named=features.firstOrNull { f -> f.hasProperty("name") && f.getStringProperty("name").isNotBlank() && f.geometry() is Point }
                if(named!=null && !currentState.navigating) {
                    val p=named.geometry() as Point
                    currentPin(Place("map:${p.latitude()},${p.longitude()}",named.getStringProperty("name"),"Selected on map",Coordinate(p.latitude(),p.longitude())))
                    true
                } else false
            }
        }
    }},modifier=modifier,update={it.visibility=if(active)android.view.View.VISIBLE else android.view.View.INVISIBLE})
    LaunchedEffect(map,state.world,active) {
        val m=map ?: return@LaunchedEffect
        if(!active || styleReady>0 && loadedWorld==state.world)return@LaunchedEffect
        // Cancel synchronously before setStyle destroys native layer handles.
        styleEpoch[0]++;positionAnimator[0]?.cancel();pointerAnimator[0]?.cancel();pointerAnimator[0]=null
        styleReady=0; currentError(null)
        val requestedWorld=state.world
        val requestedEpoch=styleEpoch[0]
        val json=context.assets.open("styles/${state.world.file}.json").bufferedReader().use{it.readText()}
        m.setStyle(Style.Builder().fromJson(json)) { style ->
            if(currentState.world!=requestedWorld || styleEpoch[0]!=requestedEpoch)return@setStyle
            val world=requestedWorld
            val art=world.mapArt()
            style.addImage("nav-arrow",mapPlayer(world))
            style.addImage("position-uncertain",mapUnknownPosition(world))
            style.addImage("destination-marker",mapDestination(world))
            for(kind in listOf("place","food","cafe","stay","fuel","parking"))style.addImage("poi-$kind",mapPoi(context,kind,world))
            style.addImage("pixel-grass",BitmapFactory.decodeResource(context.resources,R.drawable.pixel_grass))
            style.addImage("pixel-stone",BitmapFactory.decodeResource(context.resources,R.drawable.pixel_stone))
            style.addImage("pixel-water",BitmapFactory.decodeResource(context.resources,R.drawable.pixel_water))
            style.addImage("paper-fibre",BitmapFactory.decodeResource(context.resources,R.drawable.paper_fibre))
            style.addLayer(SymbolLayer("waypoint-places","openmaptiles").withSourceLayer("poi").apply{minZoom=14.8f}.withFilter(any(
                eq(get("class"),literal("food")),eq(get("class"),literal("cafe")),eq(get("class"),literal("lodging")),eq(get("subclass"),literal("fuel")),eq(get("subclass"),literal("parking")),eq(get("class"),literal("hospital"))
            )).withProperties(
                iconImage(switchCase(eq(get("subclass"),literal("fuel")),literal("poi-fuel"),eq(get("subclass"),literal("parking")),literal("poi-parking"),match(get("class"),literal("poi-place"),stop("food",literal("poi-food")),stop("cafe",literal("poi-cafe")),stop("lodging",literal("poi-stay"))))),
                iconSize(art.poiScale),iconAllowOverlap(false),textFont(arrayOf(world.mapFontName())),textField(step(zoom(),literal(""),stop(16.2,coalesce(get("name:en"),get("name"))))),textSize(10f),textColor(if(art.lightLabels)"#EEEEDF" else "#282E26"),textHaloColor(if(art.lightLabels)"#18201C" else "#E3DABF"),textHaloWidth(1.5f),textOffset(arrayOf(0f,2f))
            ))
            style.addSource(GeoJsonSource("route",FeatureCollection.fromFeatures(emptyArray<Feature>())))
            style.addLayer(LineLayer("route-casing","route").withProperties(lineColor("#161716"),lineWidth(art.routeWidth+3f),lineJoin(Property.LINE_JOIN_ROUND),lineCap(Property.LINE_CAP_ROUND)))
            style.addLayer(LineLayer("route-line","route").withProperties(lineColor(currentState.world.routeColor),lineWidth(art.routeWidth),lineJoin(Property.LINE_JOIN_ROUND),lineCap(Property.LINE_CAP_ROUND)))
            style.addSource(GeoJsonSource("destination",FeatureCollection.fromFeatures(emptyArray<Feature>())))
            style.addLayer(SymbolLayer("destination-dot","destination").withProperties(iconImage("destination-marker"),iconSize(.65f),iconAllowOverlap(true),iconIgnorePlacement(true)))
            style.addSource(GeoJsonSource("position",FeatureCollection.fromFeatures(emptyArray<Feature>())))
            style.addSource(GeoJsonSource("accuracy",FeatureCollection.fromFeatures(emptyArray<Feature>())))
            style.addLayer(FillLayer("position-accuracy","accuracy").withProperties(fillColor("#9CCBD8"),fillOpacity(.16f),fillOutlineColor("#9CCBD8")))
            style.addLayer(SymbolLayer("position-arrow","position").withProperties(iconImage("nav-arrow"),iconSize(0.55f),iconAllowOverlap(true),iconIgnorePlacement(true),iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),iconRotate(renderedBearing[0])))
            loadedWorld=requestedWorld;styleReady=requestedEpoch
        }
    }
    LaunchedEffect(active,styleReady,state.mode,state.navigating,state.offRoute,state.routing) {
        if(!active || styleReady==0)return@LaunchedEffect
        val style=map?.style ?: return@LaunchedEffect
        // Retail Cyberpunk walking radar uses yellow dotted guidance; driving is solid.
        val dotted=(state.world==World.NIGHT_CITY && state.mode==TravelMode.WALK) || (state.world==World.SAINTS && state.navigating)
        val routeColor=if(state.world==World.SAINTS && state.navigating)"#86D955" else state.world.routeColor
        style.getLayerAs<LineLayer>("route-line")?.setProperties(lineColor(routeColor),lineOpacity(if(state.offRoute || state.routing && state.navigating).45f else 1f),lineDasharray(if(dotted)arrayOf(.3f,1.8f) else arrayOf(1f,0f)))
        style.getLayerAs<LineLayer>("route-casing")?.setProperties(lineOpacity(if(dotted)0f else 1f))
    }
    LaunchedEffect(active,styleReady,state.route,state.selected,state.navigating,state.progress?.traveled) {
        if(!active || styleReady==0) return@LaunchedEffect
        val style=map?.style ?: return@LaunchedEffect
        style.getSourceAs<GeoJsonSource>("route")?.setFeature(state.route?.let{Feature.fromGeometry(LineString.fromLngLats((if(state.navigating)it.remainingPoints(state.progress) else it.points).map{p->Point.fromLngLat(p.lon,p.lat)}))})
        style.getSourceAs<GeoJsonSource>("destination")?.setFeature(state.selected?.let{Feature.fromGeometry(Point.fromLngLat(it.point.lon,it.point.lat))})
    }
    LaunchedEffect(active,styleReady,state.bearing,state.headingReliable) {
        pointerAnimator[0]?.cancel()
        if(!active || styleReady==0)return@LaunchedEffect
        val layer=map?.style?.getLayerAs<SymbolLayer>("position-arrow") ?: return@LaunchedEffect
        layer.setProperties(iconImage(if(state.headingReliable)"nav-arrow" else "position-uncertain"))
        if(state.headingReliable) {
            val from=renderedBearing[0]
            val epoch=styleEpoch[0]
            pointerAnimator[0]=ValueAnimator.ofFloat(from,from+headingDelta(from,state.bearing)).apply {
                duration=120;interpolator=LinearInterpolator()
                addUpdateListener { if(styleEpoch[0]==epoch && styleReady>0) {renderedBearing[0]=((it.animatedValue as Float)+360)%360;layer.setProperties(iconRotate(renderedBearing[0]))} }
                start()
            }
        }
    }
    LaunchedEffect(active,styleReady,state.fix,state.accuracy) {
        positionAnimator[0]?.cancel()
        if(!active || styleReady==0) return@LaunchedEffect
        val style=map?.style ?: return@LaunchedEffect
        val positionSource=style.getSourceAs<GeoJsonSource>("position") ?: return@LaunchedEffect
        val target=state.fix
        val from=renderedPosition[0]
        val epoch=styleEpoch[0]
        val bearingFrom=map?.cameraPosition?.bearing?.toFloat() ?: 0f
        val bearingTo=if(state.navigating && state.headingSource==HeadingSource.COURSE)state.bearing else bearingFrom
        fun render(point:Coordinate?,fraction:Float=1f) {
            if(styleEpoch[0]!=epoch || styleReady==0)return
            renderedPosition[0]=point
            positionSource.setFeature(point?.let{Feature.fromGeometry(Point.fromLngLat(it.lon,it.lat))})
            if(point!=null && followPolicy.shouldFollow(currentState.navigating,currentState.selected!=null,SystemClock.elapsedRealtime())) {
                map?.let { m ->
                    // The source and camera share this frame's coordinate: the user stays
                    // centred while the streets move, including during interpolation.
                    m.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder(m.cameraPosition).target(point.latLng())
                        .bearing((bearingFrom+headingDelta(bearingFrom,bearingTo)*fraction).toDouble())
                        .zoom(if(currentState.navigating)17.2 else m.cameraPosition.zoom)
                        .tilt(if(currentState.tilted)if(currentState.navigating)50.0 else m.cameraPosition.tilt else 0.0).build()))
                    cameraFix=point;cameraBearing=m.cameraPosition.bearing.toFloat()
                }
            }
        }
        if(target!=null && from!=null && distance(from,target)<150) {
            positionAnimator[0]=ValueAnimator.ofFloat(0f,1f).apply {
                duration=500;interpolator=LinearInterpolator()
                addUpdateListener { val fraction=it.animatedValue as Float;render(Coordinate(from.lat+(target.lat-from.lat)*fraction,from.lon+(target.lon-from.lon)*fraction),fraction) }
                start()
            }
        } else render(target)
        val polygon=state.fix?.let{accuracyBoundary(it,state.accuracy.toDouble())}.orEmpty()
        style.getSourceAs<GeoJsonSource>("accuracy")?.setFeature(if(polygon.isEmpty())null else Feature.fromGeometry(Polygon.fromLngLats(listOf(polygon.map{Point.fromLngLat(it.lon,it.lat)}))))
    }
    // A timer also restores following while stationary, without waiting for a sensor event.
    LaunchedEffect(active,state.navigating) {
        while(active && state.navigating) { delay(1000);followTick++ }
    }
    LaunchedEffect(active,styleReady,state.fix,state.bearing,state.headingSource,state.navigating,state.tilted,state.selected,followTick) {
        if(!active || styleReady==0)return@LaunchedEffect
        val m=map ?: return@LaunchedEffect
        val following=followPolicy.shouldFollow(state.navigating,state.selected!=null,SystemClock.elapsedRealtime())
        val desiredCameraBearing=if(state.navigating && state.headingSource==HeadingSource.COURSE)state.bearing else m.cameraPosition.bearing.toFloat()
        if(positionAnimator[0]?.isRunning==true)return@LaunchedEffect
        if(following && state.fix!=null && (cameraFix==null || distance(cameraFix!!,state.fix)>1.5 ||
            distance(Coordinate(m.cameraPosition.target!!.latitude,m.cameraPosition.target!!.longitude),state.fix)>20 ||
            kotlin.math.abs(headingDelta(cameraBearing,desiredCameraBearing))>3f)) {
            cameraFix=state.fix;cameraBearing=desiredCameraBearing
            m.easeCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder(m.cameraPosition).target(state.fix.latLng())
                .zoom(if(state.navigating)17.2 else m.cameraPosition.zoom).bearing(desiredCameraBearing.toDouble())
                .tilt(if(state.tilted)if(state.navigating)50.0 else m.cameraPosition.tilt else 0.0).build()),500)
        }
    }
    LaunchedEffect(active,state.cameraRequest,styleReady,routeInsets) {
        val m=map ?: return@LaunchedEffect
        if(!active || styleReady==0) return@LaunchedEffect
        followPolicy.resume();cameraFix=null;followTick++
        if(state.route!=null && !state.navigating) {
            val insets=routeInsets ?: return@LaunchedEffect
            val points=state.route.points.map{it.latLng()}+listOfNotNull(state.fix?.latLng(),state.selected?.point?.latLng())
            val bounds=LatLngBounds.Builder().includes(points).build()
            // An overview is north-up and flat. Inheriting a recreated map's preview tilt
            // produces an invalid fit with the narrow landscape viewport.
            m.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,0.0,0.0,insets.left,insets.top,insets.right,insets.bottom),700)
        } else {
            val center=if(state.navigating) state.fix else state.selected?.point ?: state.fix
            if(center!=null) m.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().target(center.latLng()).zoom(if(state.navigating)17.2 else state.world.mapArt().zoom).bearing(if(state.navigating)state.bearing.toDouble() else 0.0).tilt(if(state.tilted)state.world.mapArt().tilt else 0.0).build()),700)
            else m.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().target(PreviewCenter.latLng()).zoom(state.world.mapArt().zoom).bearing(0.0).tilt(if(state.tilted)state.world.mapArt().tilt else 0.0).build()))
        }
    }
}
private fun Coordinate.latLng()=LatLng(lat,lon)
// Native MapLibre cannot accept a Feature whose geometry is null.
private fun GeoJsonSource.setFeature(feature: Feature?) {
    if(feature==null) setGeoJson(FeatureCollection.fromFeatures(emptyArray<Feature>())) else setGeoJson(feature)
}

/** Stop hidden GL rendering without losing the selected camera or native style. */
private class MapRenderLifecycle(private val view:MapView) {
    private var started=false
    private var resumed=false
    fun update(state:Lifecycle.State,active:Boolean) {
        val wantStart=active && state.isAtLeast(Lifecycle.State.STARTED)
        val wantResume=active && state.isAtLeast(Lifecycle.State.RESUMED)
        if(resumed && !wantResume){view.onPause();resumed=false}
        if(started && !wantStart){view.onStop();started=false}
        if(!started && wantStart){view.onStart();started=true}
        if(!resumed && wantResume){view.onResume();resumed=true}
    }
    fun destroy(){update(Lifecycle.State.DESTROYED,false);view.onDestroy()}
}
