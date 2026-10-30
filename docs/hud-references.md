# Earlier reference research (superseded)

The user rejected the palette-based implementation associated with this document. The revised source authority, per-game composition and caveats are in [visual-sourcebook.md](visual-sourcebook.md). The following is historical evidence, not an approval or current fidelity claim.

# HUD visual references

The user requested actual game HUD research and a polished personal Android navigation app on 30 October 2026. These are reference images and documentation, not assets included in the APK. The implementation redraws controls in Compose and renders real OpenStreetMap geography with MapLibre.

| Game | Sample / source | Details carried into Waypoint |
|---|---|---|
| Grand Theft Auto V | [Official Xbox game manual: Heads Up Display](https://dlassets-ssl.xboxlive.com/public/content/4f0a3089-ba2c-4f3d-9e38-102a41cbd885/GameManual/bffef18e-3f19-4190-a9b1-75402359b13b/en-CA/index.html), [gameplay screenshot](https://img.atwiki.jp/gtav/attach/2601/16015/E-CESP.jpg) | Rectangular dark panels, monochrome radar cartography, triangular player marker, thin green/blue/yellow bars. In this app those bars are explicitly GPS, route completion and voice status. |
| Red Dead Redemption 2 | [Core-meter guide and gameplay](https://www.millenium.org/guide/315735.html), [screenshot](https://static1.millenium.org/articles/5/31/57/35/%40/751953-2018-10-26-181521-copier-article_m-1.jpg) | Warm paper, dark ink, scarlet route, circular black controls with ivory rings and western place lettering. |
| Cyberpunk 2077 | [Driving screenshot and article](https://www.universal-sci.com/sci-gaming/2020/12/28/cyberpunk-2077-gameplay-beyond-the-hype), [HUD analysis](https://prototypr.io/post/a-ux-analysis-of-cyberpunk-2077s-hud) | Clipped panel corners, red panel strokes, cyan information, yellow route instructions, dark map and magenta buildings. |
| Fallout 4 | [Companion guide with Pip-Boy map screenshots](https://www.pcgamer.com/fallout-4-companions-guide/), [map screenshot](https://cdn.mos.cms.futurecdn.net/2b7c6fb797a06991dac614e4dad5808e.png) | Phosphor-green map, green outlined controls, bracket-like selected tabs and terminal typography. No distracting scanline animation. |
| Minecraft | [Vanilla Survival HUD sample](https://minecraft.fandom.com/wiki/Gameplay), [screenshot](https://static.wikia.nocookie.net/minecraft_gamepedia/images/6/60/Survival.png/revision/latest/scale-to-width-down/1200?cb=20220702034959) | Square inventory-style navigation slots, pixel lettering, green GPS status strip and blocklike 3D geography. |

The additional San Andreas, racing, fantasy and classic themes extend these drawing rules. They have not been audited for pixel-level fidelity to a specific game version. None is a game asset port or a pixel-perfect reproduction. Controls retain 48–52 dp touch targets and actual navigation semantics. The top-down map remains full screen because this is a navigation app, rather than a game with a minimap overlay.

Local downloaded references are kept in the ignored `.tooling/hud-references/` folder. They are not shipped with the app or covered by its MIT license. Font licenses are in `docs/licenses/`. Nothing was uploaded or published.

## Expanded map-screen research

| Theme | Original map-screen reference | Visual translation |
|---|---|---|
| Vice Coast | [Original Vice City PS2 map](https://anotherretroworld.wordpress.com/2016/11/02/gta-vice-city-sur-ps2/) | Pink script title, green parkland, cream roads, gray-violet coastal water; flat atlas. |
| Liberty City | [GTA IV map screenshots](https://steamcommunity.com/sharedfiles/filedetails/?id=147215606) | Graphite land and water, gray road network, amber selection and guidance. |
| Nudle Maps | [Watch Dogs 2 map screen](https://www.tweaktown.com/news/54941/watch-dogs-2-first-impressions/index.html) | Pale daylight panels, green land, blue water, white roads, orange route. |
| Sheikah Slate | [Breath of the Wild map marker screen](https://zeldawiki.wiki/wiki/Marker) | Dark blue slate, cyan cartography, gold panel strokes, serif atlas title and actual terrain relief. |
| Skyrim | [Original Skyrim map screenshot](https://gameboyworld.com/do-ore-veins-respawn-in-skyrim/) | Cool gray relief, silver controls, dark restrained panels; no block extrusion. |
| Lands Between | [Elden Ring map screenshot](https://www.windowscentral.com/elden-ring-best-weapons) | Desaturated antique terrain, gold guidance, warm serif lettering and circular controls. |
| Horizon | [Forza Horizon 5 world-map UI designer portfolio](https://www.behance.net/gallery/131465877/Forza-Horizon-5-World-Map-UI) | White information panels, pink selection, blue route, colored land and elevation relief. |

There are now 16 themes including Classic. Real street geometry and public elevation tiles cannot reproduce fictional terrain, hand-painted textures or proprietary game icon sets exactly. These are re-creations of visual rules, not exact replicas; that distinction was explained during implementation. The original map samples are retained locally for comparison.
