# README promotional surface

THESIS: Make Waypoint tangible through app-store-style posters made from the real native app, then offer a clear Android download.
OWN-WORLD: Dark street-map backdrops, original lime/ivory navigation emblem, huge Barlow Condensed headlines, six distinct atlas palettes, precise phone frames. Licensed fonts and actual London demo screens only.
STORY: Product promise and download; six featured worlds; real navigation capabilities; adaptive identity; install; open-source build; honest limits and project links.
FIRST VIEWPORT: Original branded hero plus a clear APK download. Mobile receives a dedicated portrait composition, not a shrunken desktop strip.
FORM: GitHub Markdown with responsive picture elements. Paired posters on desktop become stacked full-width posters below 640px. Large style names, smaller game names, uniform positions across all six posters. No fake app controls or gameplay claims.

Concept-selection applicability: this is a precisely specified refinement of the existing README within Waypoint's established identity. The user supplied the app-store poster composition to follow. The skill's narrow-request exception applies; no concept-selection roll or seed was used, and none is retrospectively invented.

The user’s supplied app-store screenshot is a composition reference, not an approved pixel-exact comp or an asset source. Code-led refinement of the existing README. Native screenshot source: docs/images/{los-santos,old-west,night-city,rivia,hyrule,pixel}.jpg. New art: scripts/create_readme_promo.py and docs/images/promo/. Keep private trip captures out of public docs. Android Auto remains disabled and must not be advertised.

## Implemented surface

`scripts/create_readme_promo.py` composes actual fixed London demonstration captures into a dark map backdrop with the original Waypoint icon, Barlow Condensed headlines and Noto Sans supporting text. Six 720×1320 world posters use a shared headline, style-name and smaller game-name hierarchy. The README pairs them at 1460×1320 on wide screens and stacks them at 720×2660 for viewports up to 640px. The hero has separate 1600×980 and 900×1450 compositions. Phone frames, rounded screenshot masks and alpha-composited soft shadows belong to these promotional images. They do not change the native app's panel shapes, depth or per-world layouts.

`README.md` supplies descriptive alt text, links to the full source captures and a text download link alongside the image CTA. Artwork provenance, font licenses and reproduction instructions are recorded in `docs/images/promo/README.md`. Captures show public demonstration geography, with device status and navigation bars removed. Future public repository and release examples must use neutral public landmarks or fictional fixtures; exclude personal queries, routes, device details, notifications, home/work locations and private screenshots.

This extension introduces no native tokens, components or system-wide visual rules. `DESIGN.md` and `.impeccable/design.json` retain the existing Android design system unchanged. Poster dimensions, headline scales, accent choices and framing remain local to this surface and its generator.
