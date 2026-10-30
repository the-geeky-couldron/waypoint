package com.waypoint.app

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MapRepositoryTest {
    private lateinit var server:MockWebServer
    private lateinit var repository:MapRepository
    @Before fun setup(){server=MockWebServer();server.start();repository=MapRepository(server.url("/").toString(),server.url("/").toString(),server.url("/").toString())}
    @After fun close(){server.shutdown()}
    private fun response(body:String,code:Int=200){server.enqueue(MockResponse().setResponseCode(code).setBody(body).addHeader("Content-Type","application/json"))}
    private val feature="""{"properties":{"osm_type":"N","osm_id":1,"name":"River Cafe","street":"Strand Road","city":"Town"},"geometry":{"type":"Point","coordinates":[88.3,22.8]}}"""
    private val route="""{"code":"Ok","routes":[{"distance":111.0,"duration":60.0,"geometry":{"coordinates":[[0.0,0.0],[0.001,0.0]]},"legs":[{"steps":[{"name":"Main Street","distance":111.0,"duration":60.0,"maneuver":{"type":"depart","location":[0.0,0.0]}},{"name":"","distance":0.0,"duration":0.0,"maneuver":{"type":"arrive","location":[0.001,0.0]}}]}]}]}"""
    @Test fun searchEncodesQueryAndKeepsCoordinateOrder()=runBlocking {
        response("""{"features":[$feature]}""")
        val places=repository.search("Cafe & Park",Coordinate(22.9,88.4))
        assertEquals(Coordinate(22.8,88.3),places.single().point)
        val request=server.takeRequest().requestUrl!!
        assertEquals("Cafe & Park",request.queryParameter("q"));assertEquals("22.9",request.queryParameter("lat"));assertEquals("88.4",request.queryParameter("lon"))
    }
    @Test fun duplicateSearchResultsAreRemoved()=runBlocking {
        response("""{"features":[$feature,$feature]}""");assertEquals(1,repository.search("Cafe",Coordinate(0.0,0.0)).size)
    }
    @Test fun namelessSearchUsesStreet()=runBlocking {
        response("""{"features":[${feature.replace("River Cafe","")}]}""");assertEquals("Strand Road",repository.search("Strand",Coordinate(0.0,0.0)).single().name)
    }
    @Test fun reverseEmptyResponseHasUsefulFallback()=runBlocking {
        response("""{"features":[]}""");assertEquals("Your location",repository.reverse(Coordinate(0.0,0.0)))
    }
    @Test fun rateLimitIsActionable()=runBlocking {
        response("{}",429);val e=runCatching{repository.search("Cafe",Coordinate(0.0,0.0))}.exceptionOrNull()
        assertTrue(e?.message?.contains("busy")==true)
    }
    @Test fun serverErrorIsActionable()=runBlocking {
        response("{}",503);val e=runCatching{repository.search("Cafe",Coordinate(0.0,0.0))}.exceptionOrNull()
        assertTrue(e?.message?.contains("503")==true)
    }
    @Test fun routeParsesGeometryAndManeuvers()=runBlocking {
        response(route);val r=repository.route(Coordinate(0.0,0.0),Coordinate(0.0,0.001))
        assertEquals(2,r.points.size);assertEquals(111.0,r.meters,0.1);assertEquals("arrive",r.steps.last().type)
        assertEquals(Coordinate(0.0,0.001),r.points.last());assertTrue(server.takeRequest().path!!.contains("0.0,0.0;0.001,0.0"))
    }
    @Test fun noRouteIsNotTreatedAsSuccess()=runBlocking {
        response("""{"code":"NoRoute"}""");val e=runCatching{repository.route(Coordinate(0.0,0.0),Coordinate(0.0,0.001))}.exceptionOrNull()
        assertTrue(e?.message?.contains("No driving route")==true)
    }
    @Test fun distantRoadSnapIsRejected()=runBlocking {
        response(route);val e=runCatching{repository.route(Coordinate(3.0,3.0),Coordinate(0.0,0.001))}.exceptionOrNull()
        assertTrue(e?.message?.contains("nearby road access")==true)
    }
    @Test fun malformedResponseFailsInsteadOfShowingFakeRoute()=runBlocking {
        response("not json");assertTrue(runCatching{repository.route(Coordinate(0.0,0.0),Coordinate(0.0,0.001))}.isFailure)
    }
    @Test fun motorcycleUsesDedicatedCosting()=runBlocking {
        response(route);val r=repository.route(Coordinate(0.0,0.0),Coordinate(0.0,0.001),TravelMode.MOTORCYCLE)
        val payload=org.json.JSONObject(server.takeRequest().requestUrl!!.queryParameter("json")!!)
        assertEquals("motorcycle",payload.getString("costing"));assertEquals("geojson",payload.getString("shape_format"));assertEquals(TravelMode.MOTORCYCLE,r.mode)
    }
    @Test fun walkingUsesPedestrianCosting()=runBlocking {
        response(route);val r=repository.route(Coordinate(0.0,0.0),Coordinate(0.0,0.001),TravelMode.WALK)
        val payload=org.json.JSONObject(server.takeRequest().requestUrl!!.queryParameter("json")!!)
        assertEquals("pedestrian",payload.getString("costing"));assertEquals(TravelMode.WALK,r.mode)
    }
}
