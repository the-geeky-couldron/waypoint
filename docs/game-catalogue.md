# Game map catalogue

27 game map themes. Game names indicate visual references, not official assets. Each uses real GPS streets and a dedicated native composition, cartographic profile, marker family, type and material rules. See [visual-sourcebook.md](visual-sourcebook.md) for evidence and limits.

## Expanded games

- **ANIMUS** — Assassin’s Creed Origins. Desert cartography, gold guidance and restrained atlas panels.
- **HOPE COUNTY** — Far Cry 5. Green terrain relief, pale roads and a clear exploration HUD.
- **TSUSHIMA** — Ghost of Tsushima. Washed ink terrain, paper-white land and scarlet guidance.
- **BRIDGES** — Death Stranding. Cold terrain, cyan data marks and the blue-gray route interface.
- **THE EMBRACE** — Horizon Zero Dawn. Dark forest terrain, relief and fine ivory atlas controls.
- **MANHATTAN** — Marvel’s Spider-Man. Teal 3D city blocks, blue street lines and red HUD accents.
- **HIGHLANDS** — Hogwarts Legacy. Muted green relief, gilded borders and a midnight-blue atlas HUD.
- **SURFACE MAP** — Starfield. Deep-blue cartography, bright location marks and precise readouts.
- **BOHEMIA** — Kingdom Come: Deliverance II. Warm medieval atlas colors, woodland relief and golden routes.
- **STEELPORT** — Saints Row: The Third. Dark city blocks, purple selections and bright GPS routes.
- **MEDICI** — Just Cause 3. Navy terrain, turquoise coastlines and clean tactical-map controls.
- **LOST HEAVEN** — Mafia: Definitive Edition. 1930s street-atlas colors, golden roads and quiet cream typography.

All 27 styles are listed in README.md. Search the Worlds library using a theme title or game name. To add a style, extend World.kt, the explicit atlas dispatcher and renderer, Cartography.kt, the HUD tokens and scripts/cartography_profiles.py; generate the style assets and verify native rendering.

The Witcher 3 appears as **THE CONTINENT**, with **The Witcher 3** below it. Every world uses the same large-style/small-game hierarchy. The ordinary Classic style has been removed, including car day/night defaults.
