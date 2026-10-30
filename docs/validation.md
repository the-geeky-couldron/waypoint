# Verification record — 30 October 2026

Test device: connected Samsung SM-S948B, 1080 × 2340, density 450, Android 17 development build. Native Kotlin/Compose app, MapLibre Native. USB installation was performed alongside the reference app. Its package/data/purchases were not changed.

## Automated checks

`./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`

- 45 tests pass: 10 route/progress, 4 heading math, 13 heading arbitration/projection/accuracy, 12 repository/service, and 6 catalogue/style regression tests.
- Route tests exercise sparse geometry, turn progression, jitter, off-route movement, crossing roads, looping near the destination, actual arrival, distance units and maneuver instructions.
- Repository tests use a local HTTP test server: query encoding, longitude/latitude ordering, duplicate results, missing names, empty reverse lookup, rate limiting, server errors, valid geometry/maneuvers, missing routes, remote road snaps, malformed JSON, dedicated motorcycle and pedestrian costing.
- Android lint: zero errors. Dependency-version and deprecated-API warnings remain; no claim of a warning-free report.
- All 27 style JSON files pass the official MapLibre style validator (zero errors), have unique layer IDs, valid source references, valid top-level camera expressions, and retain the connected road classes.

## Actual phone interactions

- Install/start and repeated app restarts; GPS opt-in is retained.
- Precise permission granted through Android's system dialog; actual position fixes reported approximately ±3–5 m during stationary tests.
- Verified Android registered Waypoint's hardware rotation-vector listener at a requested 20 ms sample interval. Settings exposes provider, sensors, compass confidence, satellite count and position accuracy.
- Search for a nearby named destination, select a result, render a road route and inspect turns.
- Car route: approximately 3.9 km / 7 min. Motorcycle: approximately 3.7 km / 16 min. Walking: approximately 3.6 km / 45 min. These are one test's estimates, not fixed demonstration data. Motorcycle/walking used Valhalla's actual dedicated profiles, with no car fallback.
- Start navigation, display next turn, ETA, remaining distance and stationary speed; end navigation.
- Navigation service remained present while the Activity was backgrounded. This is not a long-duration battery/endurance test.
- Save the selected destination and find it on Saved.
- The device loop exercised all offered atlas transitions while location and orientation listeners ran. A separate stationary route test confirmed that preview and active-guidance world changes preserve the selected destination and route. Large-text (1.3×) guidance, landscape and credits-dialog interactions were captured. The final optimized build is verified separately below.

## Issues fixed during development

- A per-theme width transformation wrapped zoom interpolation in multiplication. Native MapLibre discarded those road layers. The generator now scales interpolation output stops while preserving top-level camera expressions. Native street rendering was recaptured and confirmed; two regression tests and the official full-style validator cover the failure.
- Theme switching could leave a ValueAnimator pointing at a destroyed native SymbolLayer, causing a native crash. Animators now cancel synchronously before style replacement and disposal, with epoch guards on callbacks.
- A repeated readiness value could skip theme camera updates. Each style completion now uses a monotonic generation so camera/source effects rerun.
- Reverse-geocoding failure no longer leaves the demo city label on a live GPS map. A neutral Current area label and bounded retries replace the one-shot lookup.

- Null GeoJSON geometry caused a MapLibre native startup crash. Empty sources now use an empty FeatureCollection. No recurrence was observed after that fix in the subsequent test sessions.
- Custom POI labels requested unavailable default glyph fonts. They now use named bundled SDF stacks, with provider Noto fallback for other script ranges. MapLibre initializes before the HTTP glyph resolver.
- Excessive POI labels obscured the street network. Relevant categories are filtered and names reveal at closer zoom.
- GPS opt-in and 2D/3D preference now survive restarts.
- Compass heading wraps across north correctly; source/camera work was separated to avoid re-uploading routes for every sensor update.
- Light HUD panels now use their own dark text/secondary colors. Long fantasy titles use a smaller atlas font size. Native radio/tab semantics expose selection.
- Search/route errors provide recovery; changing transport mode invalidates the previous route.

## Limits of this verification

No physical road test, tunnel/urban-canyon test, long drive, audible-speaker verification, battery benchmark, or Android Auto head-unit/DHU test was performed. Reroute/arrival logic is unit tested, not observed during a real journey. An actual tablet was not available. Landscape and text scaling use the connected phone. The integration is a personal prototype, not production navigation certification.

The maps re-create visual design rules from game screenshots. Real-world geography and original assets mean they are not pixel-perfect replicas of proprietary, hand-authored game maps. Terrain shading uses real elevation data; no fictional relief is invented. Public routing/search services have availability and rate limits; there is no live traffic or offline routing.

Native captures in `.impeccable/review/` contain the user's local geography and are ignored by Git and excluded from the source archive. Reference screenshots in `.tooling/` are likewise excluded.

## UI and performance changes

- Labelled map tools replace icon-only actions; style/game name hierarchy is uniform in the launcher and Worlds library. Bohemia and Surface Map compasses are positioned in content flow to avoid overlapping lower controls.
- A native welcome screen offers world selection and Open map. Map rendering is deferred until entry; active navigation and external destination links retain their own entry behavior.
- Preview thumbnails batch their geometry into cached paths. The native map renderer and its camera/marker work pause behind lists and sheets. Sensor readings remain current in the arbiter while hidden screens avoid heading-driven state emissions.
- All 27 HUD palette foreground, accent and secondary pairs were checked against their alpha-composited panels over black and white underlays; minimum tested ratio was 6.14:1. This does not measure every label drawn directly over terrain.
- R8/resource shrinking is enabled for the private release build. The existing development signing identity is retained so updating does not erase saved places. Release APKs are private artifacts, not a public-store release.

## Final private release verification

The optimized release installed on the phone passed all 45 unit tests and Android lint with zero errors (35 warnings). The 27 styles also pass the official validator. Four connected-device test flows passed across the final targeted runs:

- `AtlasSmokeTest`: all 27 idle worlds with live location/sensor updates, captured individually.
- `WelcomeUiTest`: launch, Witcher search/selection, entry and Back, 1.3× text, and 2× text with animations disabled.
- `NavigationUiTest`: actual GPS route, retained destination/route across world changes in preview and guidance, 1.3× text, landscape, end-trip, idle Western map and map credits.
- `LibraryScrollTest`: ten flings through the catalogue with the native map covered.

The final navigation and scrolling run reports `OK (2 tests)`; the welcome flow passed separately and the 27-world loop passed before the isolated picker/footer corrections. Those corrections do not change idle atlas composition. Early test-harness failures caused by release dependency stripping, rotation timing, and edge gestures were corrected; failed runs are not counted as passes. Test-owned font, animation and orientation settings were restored.

Final scrolling measurement: 920 rendered frames, 36 missed deadlines (3.91%), median 6 ms, 95th percentile 10 ms, 99th percentile 18 ms. The earlier debug capture measured 24.78% and a 27 ms 95th percentile. This compares a debug build with an optimized build and test gestures differ slightly at the screen edge; it is not a controlled benchmark isolating one optimization, nor a promise of zero stutter. No app crash was present in the device crash buffer during the successful release checks from 18:29 onward.

Visual corrections include single-line Locate/Settings captions, non-wrapping End trip/Steps labels at 1.3×, a backed route toolbar, and clipping thumbnail drawing to its preview bounds so descriptions retain their contrast. The installed APK hash was checked against the built release file. Private device captures stay outside the source archive.

The finish review identified one remaining material defect: route endpoints could sit behind preview controls. The correction uses measured overlay bounds and a flat, north-up overview camera, including the actual position and selected destination. A final navigation run passed after recapturing GTA, Witcher and Cyberpunk previews, plus Witcher at 1.3× text in portrait and landscape. The compact panel retains the complete Start navigation action; Save/Saved remains a single line. These changes preserve the reviewed idle atlas compositions.

The independent verdict pass returned **ship** for this scored route-preview fix: all three refreshed previews show the complete route and both endpoints clear of controls; Witcher's 1.3× portrait and landscape previews retain a fully visible Start navigation action. No regression was observed in the refreshed large-text or landscape guidance captures. This bounded verdict does not certify the whole application or replace real-road validation.

## GitHub release extension (0.2.0)

The new updater and release preparation pass 60 unit tests (the existing 45 plus 15 release-parser cases), with zero failures, and Android lint with zero errors (37 warnings). All 27 map styles still validate. Device tests captured six worlds at explicit demonstration coordinates, the welcome/library, and the updater. `UpdateUiTest` exercised Settings, the installed version, a manual live GitHub check, the not-yet-published release error and retry control, then Back. These checks passed; private status bars were cropped out of published README images.

The GitHub Actions workflow and public update channel are prepared locally. Repository creation, encrypted signing-secret setup, a live Actions run, release download and installation testing are pending the user's repository-visibility answer and GitHub CLI authorization. Download/ready states are currently source-reviewed, not a claimed end-to-end release-update pass.

The independent UI review accepted the captured README and native updater idle/error layouts. Its single material fix was stale private-only product context; the verdict pass marked that documentation fix **ship**. This verdict does not certify the still-pending GitHub release or download/installation path. The locally built 0.2.0 APK is installed on the connected phone, with its installed SHA-256 verified against the packaged file.

## Security hardening and release preparation (0.2.1)

Normal development verification after remediation:

- Debug and optimized release builds succeed with Gradle 8.14.6 and AndroidX Car 1.8.0-rc01.
- Both variants pass **82 unit tests**, with zero failures, errors or skips. Added cases cover destination parsing/import generations, strict JSON nesting and value limits, invalid provider fields, redirect rejection, oversized known-length/chunked/gzip responses, geometry limits and request cancellation.
- Android lint reports **0 errors and 35 warnings** for each variant. Warnings remain; this is not a warning-free build claim.
- All **27 map styles** pass the official style validator. Version metadata passes the release-version check.
- Debug instrumentation APK compiles. New read-only installed-package tests check the disabled/private car component, private update provider, backup and cleartext policy.
- Packaged debug/release manifest checks pass and are now CI gates. The car service is disabled and non-exported; the release is not debuggable. This verifies build output, not cross-UID exploit behavior.
- The README was rendered through GitHub's Markdown renderer and visually inspected in Helium at desktop width and in a 390px iframe. Its hero, paired screenshot gallery and text wrapping were reviewed. The full gallery images remain linked at original size.

The full security audit uses a frozen source snapshot of the earlier baseline; normal remediation builds above are separate development checks. No adversarial Binder/deep-link test was run on a personal phone or deployed service. The audit's required isolated execution controls were unavailable; see [security notes](security.md).

At release preparation, the previously connected phone is absent from ADB. The final in-app download/cancel/installer-handoff smoke test and installation of the GitHub-built APK therefore remain pending until it reconnects. Earlier device checks above remain historical evidence, not new 0.2.1 device results.
