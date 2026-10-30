# Bundled map glyph generator

Official MapLibre font-maker FreeType/SDF WebAssembly runtime, retrieved 2026-10-30:

- https://maplibre.org/font-maker/sdfglyph.js (saved as `sdfglyph.cjs`)
- https://maplibre.org/font-maker/sdfglyph.wasm
- Source and license: https://github.com/maplibre/font-maker

SHA-256:

- JS: `2d5cce9e20511e3e1798a696b496cfb4fb9c55cd9cceaa73fd77849e1c3d7a64`
- WASM: `de2986e66201499de76f21bb3a649e1f27d7d68af21cb2616dc3a7b25bade691`

Run `node scripts/generate_glyphs.cjs` from the project root. The generated protobuf
ranges are packaged in Android assets. Font source licenses are in `docs/licenses/`.
The loader resolves these ranges locally and retains the tile provider's Noto Sans
coverage for unbundled scripts. This tool is for regeneration; it is not executed
inside the Android app.
