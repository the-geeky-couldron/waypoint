package com.waypoint.app

import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Point
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class MapFollowingTest {
    private fun findMap(v:View):MapView? {
        if(v is MapView)return v
        if(v is ViewGroup)for(i in 0 until v.childCount)findMap(v.getChildAt(i))?.let{return it}
        return null
    }
    @Test fun idleMovementAndNavigationPanFollowLivePosition() {
        val inst=InstrumentationRegistry.getInstrumentation()
        val device=UiDevice.getInstance(inst)
        val start=Coordinate(51.5081,-.128)
        val model=mutableStateOf(NavState(fix=start,gpsReady=true,headingReliable=true,headingSource=HeadingSource.COURSE,bearing=90f,accuracy=4f,tilted=false))
        var map:MapLibreMap?=null
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                (activity.application as WaypointApplication).navigation.setVisible(false)
                activity.setContent { WorldMap(model.value,{},{},Modifier.fillMaxSize()) }
            }
            SystemClock.sleep(5000)
            scenario.onActivity { activity -> findMap(activity.window.decorView)!!.getMapAsync{map=it} }
            SystemClock.sleep(1000)
            assertNotNull(map)
            val moved=Coordinate(51.5081,-.126)
            inst.runOnMainSync { model.value=model.value.copy(fix=moved) }
            SystemClock.sleep(180)
            inst.runOnMainSync {
                val target=map!!.cameraPosition.target!!
                val positions=map!!.style!!.getSourceAs<GeoJsonSource>("position")!!.querySourceFeatures(null)
                assertTrue("Pointer and camera drifted during motion",positions.any { val p=it.geometry() as? Point;p!=null && distance(Coordinate(target.latitude,target.longitude),Coordinate(p.latitude(),p.longitude()))<8 })
            }
            SystemClock.sleep(1120)
            inst.runOnMainSync {
                val target=map!!.cameraPosition.target!!
                assertTrue("Idle camera did not follow GPS",distance(moved,Coordinate(target.latitude,target.longitude))<10)
                val positions=map!!.style!!.getSourceAs<GeoJsonSource>("position")!!.querySourceFeatures(null)
                assertTrue("Native pointer did not move",positions.any { val p=it.geometry() as? Point;p!=null && distance(moved,Coordinate(p.latitude(),p.longitude()))<3 })
            }
            val end=Coordinate(51.5081,-.118)
            val road=RoadRoute(listOf(start,moved,end),listOf(Maneuver(start,"depart","straight","Demo Street",800.0,100.0),Maneuver(end,"arrive","","",0.0,0.0)),800.0,100.0)
            inst.runOnMainSync { model.value=model.value.copy(navigating=true,selected=Place("demo","Demo destination","London",end),route=road,progress=NavigationEngine(road).update(moved),cameraRequest=1) }
            SystemClock.sleep(1500)
            // A real touch gesture exercises the native MapLibre listener, not the policy alone.
            device.swipe(device.displayWidth/2,device.displayHeight/2,device.displayWidth/4,device.displayHeight/2,30)
            SystemClock.sleep(1200)
            val pan= arrayOfNulls<Coordinate>(1)
            inst.runOnMainSync { val p=map!!.cameraPosition.target!!;pan[0]=Coordinate(p.latitude,p.longitude) }
            assertTrue("Gesture did not pan the map",distance(moved,pan[0]!!)>20)
            SystemClock.sleep(8500)
            inst.runOnMainSync { val p=map!!.cameraPosition.target!!;assertTrue("Following did not resume",distance(moved,Coordinate(p.latitude,p.longitude))<10) }
            val output=File(inst.targetContext.getExternalFilesDir(null),"navigation-qa").apply{mkdirs()}
            device.takeScreenshot(File(output,"following-route.png"))
            inst.runOnMainSync { model.value=model.value.copy(world=World.RIVIA) }
            SystemClock.sleep(2500)
            inst.runOnMainSync { model.value=model.value.copy(fix=Coordinate(51.5081,-.125)) }
            SystemClock.sleep(1000)
        }
    }
}
