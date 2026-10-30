package com.waypoint.app

import org.junit.Assert.*
import org.junit.Test

class HeadingMathTest {
    @Test fun crossesNorthClockwiseWithoutFullTurn(){assertEquals(2f,headingDelta(359f,1f),0.001f)}
    @Test fun crossesNorthCounterclockwiseWithoutFullTurn(){assertEquals(-2f,headingDelta(1f,359f),0.001f)}
    @Test fun smoothingDoesNotSwingSouthAtNorth(){val angle=smoothHeading(359f,1f,0.5f);assertTrue(angle<1f || angle>359f)}
    @Test fun smoothingFollowsShortestTurn(){assertEquals(95f,smoothHeading(90f,110f,0.25f),0.001f)}
}
