# Waypoint launcher icon and destination search — scoped extension

## Authority and boundary

The user requested an original bold navigation icon inspired by the supplied dark-map sample, with Android Material You themed-icon support. Existing PRODUCT.md and DESIGN.md remain authoritative for the app. This extension adds the launcher identity and documents the implemented search behavior; all game palettes, map compositions and typography remain in place.

## Launcher expression

A faceted arrow points northeast, connected visually to a bent route and origin dot. Open compass brackets frame the mark. Original abstract blocks and roads form a quieter background. The four reusable launcher colors are recorded in DESIGN.md; the full vector details remain in the drawable resources. A 108dp canvas and centered 0.88 foreground/monochrome scale preserve adaptive-mask room. The monochrome silhouette keeps the arrow, route and brackets, leaving color choice to supported launchers.

Sources: `app/src/main/res/drawable/ic_launcher_background.xml`, `ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml` and `app/src/main/res/mipmap-anydpi/ic_launcher.xml`.

## Search expression and behavior

`SearchScreen` and `PlaceRow` in `MainActivity.kt` keep native fields and list behavior, the library background, selected-world shapes and place-icon accent. Search submits through a visible Search places button or the keyboard Search action. Rows expose the returned name, address and provider, with two name lines and three address lines before ellipsis. The stored address is not replaced by a shortened display label.

Photon supplies place search; Android geocoding supplements it when needed, with bounded query variants, local ranking and confirmed spelling aliases. Aliases change the query, not the provider coordinates. A Plus-Code-only Android feature name is shown as “Address result for …” and attributed to “Android geocoder · address match.” It is not evidence that a queried junction or landmark name was verified. Provider coverage can still omit a place; no complete named-place database is promised.

Sources: `PlaceSearch.kt`, `AndroidPlaceSearch.kt`, `MapRepository.kt` and `MainActivity.kt` under `app/src/main/java/com/waypoint/app/`.

## Evidence and limits

The native phone capture `.impeccable/review/navigation-022/phone-icon.png` shows normal, wallpaper-derived light and wallpaper-derived dark variants at 112dp and 48dp. `LauncherIconTest.kt` renders Android AdaptiveIconDrawable layers using dynamic color schemes and checks the declared adaptive resource and monochrome layer. This is native drawable rendering evidence, not proof of every launcher’s themed-icon setting or mask. The scoped reviewer returned ship for the icon and reviewed the corrected address-result wording. The documenter inspected the capture and source; it did not repeat runtime search or launcher tests.

Existing fidelity, clipping and device-behavior limits in DESIGN.md remain review matters. No unrelated defects or design changes are promoted into the system by this extension.
