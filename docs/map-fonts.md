# Map lettering

Waypoint renders road, locality and POI text with bundled MapLibre SDF glyphs.
`MapGlyphs.kt` resolves the generated ranges locally; `generate_styles.py` and
`World.mapFontName()` use the same seven named stacks. Unknown ranges fall back
to the map provider's Noto Sans coverage. Run `node scripts/generate_glyphs.cjs`
after changing a font; generator provenance is in `tools/glyph-generation/`.

| Role | Typeface in Waypoint | Use |
|---|---|---|
| Red Dead inspired Western map and HUD | Kirsty regular / bold | Game identified main family; local map glyphs use regular. |
| Red Dead inspired Western titles | Chinese Rocks regular | Display face. |
| GTA inspired titles | Pricedown Black | Los Santos, San Andreas and Liberty titles only. |
| Cyberpunk technical map and HUD | Rajdhani | Identified main family. |
| Fallout Pip-Boy HUD | Roboto Condensed | Main interface family. |
| Pixel map | Monocraft | Open pixel interpretation. |
| Other fantasy / antique maps | Libre Baskerville | Readable substitute. |
| Neutral geographic labels | Noto Sans | Broad script coverage and fallback. |

The game font files and source URLs are recorded in
`third_party/game_fonts/README.md`. The project owner states that they hold
redistribution rights for the game typefaces. The public download pages alone
grant narrower rights, so those pages are source records, not the legal basis
for redistribution. The remaining 27-world font treatments are adaptations;
this document does not claim that every game map uses an exact original face.

Typeface identification: [Cyberpunk](https://www.gamefontlibrary.com/games/cyberpunk-2077),
[Red Dead Redemption 2](https://www.gamefontlibrary.com/games/red-dead-redemption-2-),
[Fallout interface](https://scrivener07.github.io/BGS-Handbook/wiki/fo4/interface/reference/assets/fonts),
[GTA fonts](https://gtamods.com/wiki/Fonts_%28GTA_V%29).
