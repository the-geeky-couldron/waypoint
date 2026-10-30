# Map lettering

Waypoint now renders native road, locality and POI text with bundled SDF fonts,
not only Compose overlay fonts. `MapGlyphs.kt` resolves the generated ranges locally;
`generate_styles.py` and `World.mapFontName()` agree on the six named stacks.
Unknown ranges fall back to the map provider's Noto Sans coverage.

| Role | Bundled font | Fidelity |
|---|---|---|
| Cyberpunk technical labels | Rajdhani | Identified main face; lettering size, weight and placement remain a mobile adaptation. |
| Fallout Pip-Boy and condensed map labels | Roboto Condensed | Fallout's main interface family; the terminal's separate Share Tech Mono face is not substituted across the Pip-Boy. |
| Pixel labels | Monocraft | Open-source pixel interpretation, not a proprietary extracted game font. |
| Serif atlas labels | Libre Baskerville | Readable substitute for unavailable game-specific serif faces. |
| Neutral map labels | Noto Sans | Geographic-label coverage and readable fallback. |
| Western / fantasy / Vice titles | Rye / Cinzel / Yellowtail | Explicit substitutes, not original RDR, Skyrim or GTA font files. |

Sources checked: [Cyberpunk font roles](https://www.gamefontlibrary.com/games/cyberpunk-2077),
[Fallout font configuration](https://scrivener07.github.io/BGS-Handbook/wiki/fo4/interface/reference/assets/fonts),
[SkyUI font configuration](https://github.com/schlangster/skyui/blob/master/build/fontconfig.txt),
[RDR2 font roles](https://www.gamefontlibrary.com/games/red-dead-redemption-2-).
The latter identifies Kirsty and Hapna separately from the Chinese Rocks display face.
Those proprietary fonts are not bundled. A game's logo face is not evidence for its
road labels or body text. No 1:1 font-reproduction claim is made.

Fonts come from their upstream open-font projects / Google Fonts, with notices in
`docs/licenses/`. Regenerate using `node scripts/generate_glyphs.cjs`; the official
MapLibre generator and its provenance are in `tools/glyph-generation/`.
