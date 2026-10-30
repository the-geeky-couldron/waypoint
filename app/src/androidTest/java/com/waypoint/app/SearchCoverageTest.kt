package com.waypoint.app

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import android.os.SystemClock
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Explicit live-provider check, excluded from offline CI. Coordinates are public landmarks. */
class SearchCoverageTest {
    @Test fun publicDemoQueriesFindStationAndLandmark()=runBlocking<Unit> {
        val inst=InstrumentationRegistry.getInstrumentation()
        val repository=MapRepository()
        val search=PlaceSearch(PlaceSearchProvider(repository::search),AndroidPlaceSearch(inst.targetContext))
        // Public London landmarks only; private regression queries stay outside the repository.
        val near=Coordinate(51.5074,-.1278)
        val station=search.search("Charing Cross station",near,expanded=true)
        assertTrue("Station not returned: ${station.note}",station.places.any{distance(it.point,Coordinate(51.508,-.125))<300})
        val junction=search.search("Trafalgar Square",near,expanded=true)
        assertTrue("Landmark not returned: ${junction.note}",junction.places.any{distance(it.point,Coordinate(51.508,-.128))<300})
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                (activity.application as WaypointApplication).navigation.setVisible(false)
                activity.setContent { MaterialTheme(colorScheme=darkColorScheme(primary=World.LOS_SANTOS.accent,onPrimary=Ink,background=Ink,surface=Ink,onSurface=Cream,onSurfaceVariant=Muted)) {
                    Surface(color=Ink,contentColor=Cream) { SearchScreen(NavState(query="Trafalgar Square",searchResults=junction.places,searchNote=junction.note,fix=near),{},{},{},{}) }
                } }
            }
            SystemClock.sleep(1000)
            val output=File(inst.targetContext.getExternalFilesDir(null),"navigation-qa").apply{mkdirs()}
            UiDevice.getInstance(inst).takeScreenshot(File(output,"search-alias.png"))
        }
    }
}
