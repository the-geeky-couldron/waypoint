package com.waypoint.app

/** Monotonic sample times prevent a batched callback from pretending to be sustained deviation. */
class ReroutePolicy {
    private var since: Long? = null
    private var samples = 0
    private var nextAttempt = 0L
    private var failures = 0
    data class Decision(val offRoute: Boolean, val request: Boolean)
    fun reset() { since = null; samples = 0; nextAttempt = 0; failures = 0 }
    fun observe(progress: RouteProgress, accuracy: Float, speed: Float, mode: TravelMode, now: Long, busy: Boolean): Decision {
        val trustworthy = accuracy.isFinite() && accuracy in 0f..35f
        val threshold = maxOf(if(mode == TravelMode.WALK) 15.0 else 20.0, accuracy * 2.0)
        val wrongWay = speed > 1.5f && progress.traveled - progress.projectedAlong > maxOf(25.0, accuracy * 2.5)
        val away = trustworthy && (progress.offRoute > threshold || wrongWay)
        if(!away) { since = null; samples = 0; return Decision(false,false) }
        if(since == null) since = now
        samples++
        return Decision(true, !busy && samples >= 3 && now - since!! >= 2500 && now >= nextAttempt)
    }
    fun requested(now: Long) { nextAttempt = now + 8000 }
    fun succeeded() { since = null; samples = 0; failures = 0 }
    fun failed(now: Long) { failures = (failures + 1).coerceAtMost(3); nextAttempt = now + (8000L shl (failures - 1)) }
}

/** A deliberate pan pauses guidance briefly; a stray gesture cannot disable following forever. */
class MapFollowPolicy {
    private var gestureAt: Long? = null
    private var gesturing = false
    fun gestureStarted(now: Long) { gesturing = true; gestureAt = now }
    fun gestureEnded(now: Long):Boolean { val ended=gesturing;gesturing=false;if(ended)gestureAt=now;return ended }
    fun resume() { gestureAt = null; gesturing = false }
    fun shouldFollow(navigating: Boolean, hasDestination: Boolean, now: Long): Boolean {
        if(gesturing) return false
        if(gestureAt != null && navigating && now - gestureAt!! >= 8000) resume()
        return gestureAt == null && (navigating || !hasDestination)
    }
}
