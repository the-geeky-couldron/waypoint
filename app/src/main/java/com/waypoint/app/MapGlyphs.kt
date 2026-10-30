package com.waypoint.app

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.maplibre.android.module.http.HttpRequestUtil

/** Custom map glyphs use the same fonts as their HUD, with provider coverage for other scripts. */
internal object MapGlyphs {
    private val names=setOf("Waypoint Condensed","Waypoint Technical","Waypoint Pixel","Waypoint Terminal","Waypoint Atlas","Waypoint Classic")
    fun install(context:Context) {
        org.maplibre.android.MapLibre.getInstance(context)
        HttpRequestUtil.setOkHttpClient(OkHttpClient.Builder().addInterceptor { chain ->
            val request=chain.request();val url=request.url;val parts=url.pathSegments
            if(url.host!="tiles.openfreemap.org" || parts.size!=3 || parts[0]!="fonts" || parts[1] !in names) return@addInterceptor chain.proceed(request)
            val range=parts[2]
            if(!range.matches(Regex("[0-9]+-[0-9]+\\.pbf")))return@addInterceptor chain.proceed(request)
            val bytes=try {context.assets.open("glyphs/${parts[1]}/$range").use{it.readBytes()}} catch(_:java.io.FileNotFoundException){null}
            if(bytes!=null) Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("Bundled font")
                .header("Cache-Control","public, max-age=31536000").body(bytes.toResponseBody("application/x-protobuf".toMediaType())).build()
            else chain.proceed(request.newBuilder().url(url.newBuilder().setPathSegment(1,"Noto Sans Regular").build()).build())
        }.build())
    }
}

internal fun World.mapFontName()=when(this) {
    World.NIGHT_CITY,World.BRIDGES,World.SPIDER,World.STARFIELD,World.JUST_CAUSE->"Waypoint Technical"
    World.PIXEL->"Waypoint Pixel"
    World.OLD_WEST,World.RIVIA,World.LANDS_BETWEEN,World.KINGDOM,World.HOGWARTS,World.MAFIA->"Waypoint Atlas"
    World.LOS_SANTOS,World.GREEN_TERMINAL,World.SKYRIM,World.SAN_ANDREAS,World.LIBERTY,World.HEAT,World.FAR_CRY->"Waypoint Condensed"
    else->"Waypoint Classic"
}
