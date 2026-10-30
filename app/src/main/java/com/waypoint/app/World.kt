package com.waypoint.app

import androidx.compose.ui.graphics.Color

enum class World(val title: String,val subtitle: String,val description: String,val file: String,val accent: Color,val routeColor: String,val ground: Color,val road: Color,val building: Color) {
    LOS_SANTOS("LOS SANTOS","THE CITY IS YOURS","Monochrome streets, purple routes and the three-bar radar HUD.","los_santos",Color(0xFFB6DB91),"#B786ED",Color(0xFF20221F),Color(0xFF73776C),Color(0xFF383B35)),
    OLD_WEST("OLD WEST","TAKE THE LONG WAY","Parchment roads, scarlet routes and circular core controls.","old_west",Color(0xFFE4BE82),"#B52E31",Color(0xFFDAC8A2),Color(0xFF665F4B),Color(0xFFC6B18D)),
    NIGHT_CITY("NIGHT CITY","AFTER HOURS","Cyan streets, red buildings and angular navigation panels.","night_city",Color(0xFF6DE9DD),"#F6E75C",Color(0xFF0C1019),Color(0xFF258DA3),Color(0xFF8B2029)),
    PIXEL("PIXEL WORLD","A NEW PERSPECTIVE","Pixel lettering, inventory-style controls and block-built streets.","pixel",Color(0xFFB8E088),"#E95345",Color(0xFF566943),Color(0xFFC7C4A5),Color(0xFF777D6C)),
    GREEN_TERMINAL("GREEN TERMINAL","SIGNAL FOUND","Phosphor cartography, terminal lettering and Pip-Boy-style tabs.","green_terminal",Color(0xFF9AF477),"#D5FF9B",Color(0xFF071109),Color(0xFF6BCF55),Color(0xFF1E4922)),
    SAN_ANDREAS("SAN ANDREAS","BACK TO THE STREETS","A sun-faded street atlas inspired by the classics.","san_andreas",Color(0xFFF2C877),"#DA5B3C",Color(0xFFC2C5AD),Color(0xFFF4F0D8),Color(0xFFA4A48E)),
    HEAT("MIDNIGHT RUN","TAKE THE NIGHT SHIFT","A detailed racing atlas with blue water and electric guidance.","heat",Color(0xFFFFA9E8),"#75EBF5",Color(0xFF555946),Color(0xFFD2D2B6),Color(0xFF6A6A5A)),
    RIVIA("THE CONTINENT","The Witcher 3","The Witcher 3 atlas: inked roads, parchment terrain and red guidance.","rivia",Color(0xFFD6D59A),"#9F3435",Color(0xFFB6AF8D),Color(0xFFE4D6AE),Color(0xFF93886B)),
    VICE_COAST("VICE COAST","SUNSET STREETS","Vice City’s pink lettering, pale roads and coastal greens.","vice_coast",Color(0xFFFFAFE1),"#F46AC0",Color(0xFF94909D),Color(0xFFF6F1DD),Color(0xFFB4B4B0)),
    LIBERTY("LIBERTY CITY","THE CONCRETE GRID","GTA IV’s graphite atlas, gray streets and amber guidance.","liberty",Color(0xFFFFD06D),"#EFA937",Color(0xFF222527),Color(0xFF92999C),Color(0xFF42484B)),
    CTOS("NUDLE MAPS","CONNECTED CITY","Watch Dogs 2’s daylight street atlas and bright place markers.","ctos",Color(0xFF78C9FF),"#F28D31",Color(0xFFE5E9DD),Color(0xFFFFFFFF),Color(0xFFC8CED0)),
    HYRULE("SHEIKAH SLATE","THE EXPLORER’S MAP","Breath of the Wild’s blue slate, gold accents and terrain relief.","hyrule",Color(0xFF81D9FF),"#F0CE6B",Color(0xFF4B4932),Color(0xFFA69C64),Color(0xFF5F5B3B)),
    SKYRIM("SKYRIM","THE NORTHERN ROAD","Cold relief, silver symbols and the restrained Elder Scrolls HUD.","skyrim",Color(0xFFDCE9EE),"#E9E4CE",Color(0xFF859395),Color(0xFFBDC4BE),Color(0xFF647577)),
    LANDS_BETWEEN("LANDS BETWEEN","A GOLDEN THREAD","Elden Ring’s antique atlas, gold markers and earth-toned relief.","lands_between",Color(0xFFE3CC8D),"#EAC968",Color(0xFFB1A789),Color(0xFFE0D1AA),Color(0xFF817963)),
    HORIZON("HORIZON","THE OPEN ROAD","Forza’s vivid road atlas, pink controls and terrain relief.","horizon",Color(0xFFFF92D6),"#51C4F1",Color(0xFF8A966A),Color(0xFFF5F0DD),Color(0xFFA3A18A)),
    ASSASSIN("ANIMUS","Assassin’s Creed Origins","Desert cartography, gold guidance and restrained atlas panels.","assassin",Color(0xFFEBD28C),"#e7c65d",Color(0xFFB49B6E),Color(0xFFE7D6AD),Color(0xFFA49477)),
    FAR_CRY("HOPE COUNTY","Far Cry 5","Green terrain relief, pale roads and a clear exploration HUD.","far_cry",Color(0xFFF4D57B),"#eac35b",Color(0xFF6E7A60),Color(0xFFDDD9BC),Color(0xFF818777)),
    GHOST("TSUSHIMA","Ghost of Tsushima","Washed ink terrain, paper-white land and scarlet guidance.","ghost",Color(0xFFE8E5D7),"#b84435",Color(0xFFC9CCC2),Color(0xFFEEEADE),Color(0xFFAEB4A8)),
    BRIDGES("BRIDGES","Death Stranding","Cold terrain, cyan data marks and the blue-gray route interface.","bridges",Color(0xFF9AE5DD),"#d8c87a",Color(0xFF182B37),Color(0xFF86AAB1),Color(0xFF344953)),
    HORIZON_WILDS("THE EMBRACE","Horizon Zero Dawn","Dark forest terrain, relief and fine ivory atlas controls.","horizon_wilds",Color(0xFFF0DBAC),"#f1d784",Color(0xFF5B6251),Color(0xFFC6B88C),Color(0xFF707365)),
    SPIDER("MANHATTAN","Marvel’s Spider-Man","Teal 3D city blocks, blue street lines and red HUD accents.","spider",Color(0xFFFF9E9D),"#f6d156",Color(0xFF102A38),Color(0xFF4290A4),Color(0xFF1B6173)),
    HOGWARTS("HIGHLANDS","Hogwarts Legacy","Muted green relief, gilded borders and a midnight-blue atlas HUD.","hogwarts",Color(0xFFE5CE93),"#dabd69",Color(0xFF77765F),Color(0xFFC4BEA0),Color(0xFF72765E)),
    STARFIELD("SURFACE MAP","Starfield","Deep-blue cartography, bright location marks and precise readouts.","starfield",Color(0xFFEEF1E9),"#f3c56f",Color(0xFF1B304B),Color(0xFF7196AF),Color(0xFF36546A)),
    KINGDOM("BOHEMIA","Kingdom Come: Deliverance II","Warm medieval atlas colors, woodland relief and golden routes.","kingdom",Color(0xFFE6CF94),"#dfba52",Color(0xFFB2A37D),Color(0xFFE7D4AA),Color(0xFFA39472)),
    SAINTS("STEELPORT","Saints Row: The Third","Dark city blocks, purple selections and bright GPS routes.","saints",Color(0xFFD0A6FB),"#b893f3",Color(0xFF1B1C22),Color(0xFF7C8490),Color(0xFF363740)),
    JUST_CAUSE("MEDICI","Just Cause 3","Navy terrain, turquoise coastlines and clean tactical-map controls.","just_cause",Color(0xFF87ECDF),"#f0cf6a",Color(0xFF143748),Color(0xFF6FA4AD),Color(0xFF305360)),
    MAFIA("LOST HEAVEN","Mafia: Definitive Edition","1930s street-atlas colors, golden roads and quiet cream typography.","mafia",Color(0xFFE8D29A),"#bd624c",Color(0xFFC1C1A4),Color(0xFFE8D174),Color(0xFFAAA99A)),
}

fun World.hasTerrain()=this in setOf(World.HEAT,World.RIVIA,World.HYRULE,World.SKYRIM,World.LANDS_BETWEEN,World.HORIZON,
    World.ASSASSIN,World.FAR_CRY,World.GHOST,World.BRIDGES,World.HORIZON_WILDS,World.HOGWARTS,World.STARFIELD,World.KINGDOM,World.JUST_CAUSE)
