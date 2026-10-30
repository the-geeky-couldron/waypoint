package com.waypoint.app

import android.app.Application
import android.location.Location
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

/** Drives production location handling in isolation. Never replaces the phone's GPS provider. */
class NavigationJourneyTest {
    private val inst=InstrumentationRegistry.getInstrumentation()
    private val a=Coordinate(51.5000,-.1200)
    private val b=Coordinate(51.5000,-.1100)
    private val c=Coordinate(51.5100,-.1100)
    private fun road(points:List<Coordinate>)=RoadRoute(points,listOf(Maneuver(points.first(),"depart","straight","Demo Road",1000.0,90.0),Maneuver(points.last(),"arrive","","",0.0,0.0)),2000.0,180.0,TravelMode.MOTORCYCLE)
    private class FakeRepository(val routeFrom:(Coordinate)->RoadRoute):NavigationRepository {
        var calls=0;var failure=false
        override suspend fun search(query:String,near:Coordinate)=emptyList<Place>()
        override suspend fun reverse(point:Coordinate)="Test area"
        override suspend fun route(from:Coordinate,to:Coordinate,mode:TravelMode):RoadRoute {
            calls++;delay(200)
            if(failure)throw java.io.IOException("Simulated network outage")
            return routeFrom(from)
        }
    }
    @Suppress("UNCHECKED_CAST")
    private fun state(controller:NavigationController)=field(controller,"_state") as MutableStateFlow<NavState>
    private fun field(controller:NavigationController,name:String)=controller.javaClass.getDeclaredField(name).apply{isAccessible=true}.get(controller)
    private fun set(controller:NavigationController,name:String,value:Any){controller.javaClass.getDeclaredField(name).apply{isAccessible=true}.set(controller,value)}
    private fun fix(controller:NavigationController,point:Coordinate,ageMs:Long=0,accuracy:Float=4f) {
        inst.runOnMainSync {
            val location=Location("gps").apply { latitude=point.lat;longitude=point.lon;this.accuracy=accuracy;speed=8f;bearing=90f;elapsedRealtimeNanos=SystemClock.elapsedRealtimeNanos()-ageMs*1_000_000;time=System.currentTimeMillis()-ageMs }
            controller.javaClass.getDeclaredMethod("receiveLocation",Location::class.java).apply{isAccessible=true}.invoke(controller,location)
        }
    }
    private fun fixture(block:(NavigationController,FakeRepository)->Unit) {
        val app=inst.targetContext.applicationContext as Application
        val prefs=app.getSharedPreferences("journey-test",0);prefs.edit().clear().putBoolean("voice",false).commit()
        val repository=FakeRepository { from -> road(listOf(from,Coordinate(from.lat,-.1080),c)) }
        lateinit var controller:NavigationController
        inst.runOnMainSync {
            controller=NavigationController(app,repository,"journey-test",null)
            val route=road(listOf(a,b,c));set(controller,"engine",NavigationEngine(route));set(controller,"routeOrigin",a)
            set(controller,"listening",true);set(controller,"platformListening",true)
            state(controller).value=NavState(fix=a,accuracy=4f,gpsReady=true,navigating=true,route=route,selected=Place("demo","Demo destination","Synthetic test",c),mode=TravelMode.MOTORCYCLE,voice=false)
        }
        try { block(controller,repository) } finally {
            inst.runOnMainSync {
                set(controller,"listening",false)
                (field(controller,"scope") as CoroutineScope).cancel()
                (field(controller,"tts") as android.speech.tts.TextToSpeech).shutdown()
            }
            prefs.edit().clear().commit()
        }
    }
    @Test fun movementMissedTurnAndReplacementRouteReachState()=fixture { controller,repository ->
        fix(controller,Coordinate(a.lat,-.1180));val first=controller.state.value
        fix(controller,Coordinate(a.lat,-.1160));val moved=controller.state.value
        assertNotEquals(first.fix,moved.fix);assertTrue(moved.progress!!.traveled>first.progress!!.traveled)
        val oldRoute=moved.route
        for(i in 0..3) { fix(controller,Coordinate(a.lat-.0005-i*.0001,-.1150));if(i<3)SystemClock.sleep(1050) }
        assertTrue(controller.state.value.routing)
        SystemClock.sleep(500)
        val rerouted=controller.state.value
        assertEquals(1,repository.calls);assertNotEquals(oldRoute,rerouted.route)
        assertEquals(rerouted.fix,rerouted.route!!.points.first());assertTrue(rerouted.navigating)
        assertEquals(TravelMode.MOTORCYCLE,rerouted.route.mode);assertFalse(rerouted.routing);assertFalse(rerouted.offRoute)
        val next=Coordinate(rerouted.fix!!.lat,-.1130);fix(controller,next)
        assertEquals(next,controller.state.value.fix)
        assertTrue(controller.state.value.progress!!.traveled>50)
    }
    @Test fun outdatedAndInaccurateFixesCannotTriggerRouteReplacement()=fixture { controller,repository ->
        fix(controller,Coordinate(a.lat,-.1170));val accepted=controller.state.value.fix
        fix(controller,Coordinate(a.lat+.1,-.1170),ageMs=20000)
        assertEquals(accepted,controller.state.value.fix)
        repeat(4) { fix(controller,Coordinate(a.lat+.002,-.1170),accuracy=80f);SystemClock.sleep(50) }
        assertFalse(controller.state.value.gpsReady);assertEquals(0,repository.calls)
    }
    @Test fun failedRerouteRetainsDestinationAndRetriesAfterBackoff()=fixture { controller,repository ->
        repository.failure=true
        for(i in 0..3) { fix(controller,Coordinate(a.lat-.001,-.1180));if(i<3)SystemClock.sleep(1050) }
        SystemClock.sleep(500)
        assertNotNull(controller.state.value.routeError);assertEquals("demo",controller.state.value.selected!!.id)
        repeat(3) { fix(controller,Coordinate(a.lat-.001,-.1180));SystemClock.sleep(100) }
        assertEquals(1,repository.calls)
        repository.failure=false
        SystemClock.sleep(8000);fix(controller,Coordinate(a.lat-.001,-.1180));SystemClock.sleep(500)
        assertEquals(2,repository.calls);assertNull(controller.state.value.routeError);assertTrue(controller.state.value.navigating)
    }
}
