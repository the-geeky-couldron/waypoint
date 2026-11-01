# Artwork and map-preview sources

Waypoint's 27 portrait world illustrations in `app/src/main/res/drawable-nodpi/art_*` are original images generated for this project with `gpt-image-2` and selected for legibility in the mobile gallery. The prompts are in [`artwork-prompts.jsonl`](artwork-prompts.jsonl). They describe environments, lighting, and palettes inspired by game genres without requesting publisher art, logos, characters, or copied locations. The onboarding stickers and illustrations were made for Waypoint in the same manner.

The first onboarding Earth image is adapted from NASA Earth Observatory's [Blue Marble western hemisphere](https://science.nasa.gov/earth/earth-observatory/the-blue-marble-true-color-global-imagery-at-1km-resolution/) image by Reto Stöckli and Robert Simmon. The rotating globe and its country geometry are rendered in Waypoint from geographic data; the globes and motion clips in `tools/waypoint-motion/` are Waypoint compositions.

The 27 world-card map previews in `app/src/main/res/drawable-nodpi/preview_*` were captured with MapLibre from each Waypoint map style over a real Indian location. The locations are recorded in `WorldPreviewPlaces.kt`. Each style uses [OpenFreeMap](https://openfreemap.org/) tiles, [OpenMapTiles](https://openmaptiles.org/) schema, and [OpenStreetMap](https://www.openstreetmap.org/copyright) geographic data. Attribution is shown above the card gallery, with each card identifying its city or landmark.

Referral choices use the recognizable Instagram, Reddit, YouTube, Google, GitHub, and Telegram vector marks from [Simple Icons](https://simpleicons.org/); the Friend mark is a Material icon. These marks identify the named services and do not imply affiliation.

Game names identify the source of stylistic inspiration. Waypoint is independent of those games and their publishers.
