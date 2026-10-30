package com.waypoint.app
import org.json.JSONObject
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class WorldCatalogueTest {
    @Test fun everyThemeHasAValidStyleAndNoDuplicateLayers() {
        for(world in World.entries) {
            val file=File("src/main/assets/styles/${world.file}.json")
            assertTrue("Missing ${world.title}",file.isFile)
            val style=JSONObject(file.readText());val layers=style.getJSONArray("layers");val ids=mutableSetOf<String>()
            for(i in 0 until layers.length()) {
                val layer=layers.getJSONObject(i)
                assertTrue("Duplicate layer in ${world.title}",ids.add(layer.getString("id")))
                if(layer.has("source"))assertTrue(style.getJSONObject("sources").has(layer.getString("source")))
            }
            assertEquals("Terrain flag ${world.title}",world.hasTerrain(),style.getJSONObject("sources").has("terrain"))
        }
    }
    @Test fun roadCameraExpressionsRemainValidForNativeRendering() {
        fun hasZoom(value:Any?):Boolean = value is JSONArray && (value.optString(0)=="zoom" || (0 until value.length()).any{hasZoom(value.opt(it))})
        for(world in World.entries) {
            val layers=JSONObject(File("src/main/assets/styles/${world.file}.json").readText()).getJSONArray("layers")
            for(i in 0 until layers.length()) {
                val layer=layers.getJSONObject(i)
                if(layer.optString("source-layer")!="transportation" || layer.optString("type")!="line")continue
                val width=layer.optJSONObject("paint")?.opt("line-width")
                if(!hasZoom(width))continue
                assertTrue("${world.name}/${layer.getString("id")}: zoom must be top-level",width is JSONArray)
                val expr=width as JSONArray
                val op=expr.optString(0)
                assertTrue("${world.name}/${layer.getString("id")}: invalid camera expression $op",op in listOf("interpolate","step"))
                val input=if(op=="step")1 else 2
                assertEquals("zoom",expr.getJSONArray(input).getString(0))
                for(n in 1 until expr.length())if(n!=input)assertFalse("Nested zoom expression",hasZoom(expr.opt(n)))
            }
        }
    }
    @Test fun everyThemeKeepsConnectedRoadClasses() {
        for(world in World.entries) {
            val layers=JSONObject(File("src/main/assets/styles/${world.file}.json").readText()).getJSONArray("layers")
            for(id in listOf("road_minor","road_secondary_tertiary","road_trunk_primary","road_motorway","road_service_track")) {
                val road=(0 until layers.length()).map{layers.getJSONObject(it)}.first{it.getString("id")==id}
                assertNotEquals("${world.name}/$id is hidden","none",road.optJSONObject("layout")?.optString("visibility"))
                assertTrue(road.getJSONObject("paint").has("line-width"))
            }
        }
    }
    @Test fun gameAbbreviationsFindTheirHud(){assertTrue(World.LOS_SANTOS.matchesGameQuery("GTA V"));assertTrue(World.OLD_WEST.matchesGameQuery("rdr2"));assertTrue(World.HYRULE.matchesGameQuery("BOTW"))}
    @Test fun punctuationDoesNotBreakGameSearch(){assertTrue(World.ASSASSIN.matchesGameQuery("Assassin's Creed"));assertTrue(World.SPIDER.matchesGameQuery("Spider Man"))}
    @Test fun unrelatedNamesDoNotMatch(){assertFalse(World.SKYRIM.matchesGameQuery("Forza"));assertTrue(World.SKYRIM.matchesGameQuery("  "))}
}
