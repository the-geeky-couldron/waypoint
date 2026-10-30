# Reference observations

The user identified the installed Android app **Game Maps IRL**, package
`com.retryapps.game.maps.irl`, version 2.3 (code 26).

Observed through Android Debug Bridge on the user's connected phone:
- Full-screen dark map with a white triangular position marker.
- Place/date heading.
- A bottom overlay says “This Map is Locked” and “Unlock All Maps”.
- System Back exits the app from that screen.

Other screens and paid behavior were not observable in this session. The six
supplied screenshots are additional visual references, not verified behavior
of this exact installed version. No claim of complete feature parity is made.

REA MCP tools were absent; its CLI diagnostic could not run because the installed
package's compiled runtime was missing. The observations above are ADB runtime
observations, not REA Evidence records. No proprietary code, assets, credentials,
or entitlement logic have been extracted, modified, or included in Waypoint.
Waypoint is an independent Kotlin implementation with original map palettes.
Private reference captures remain in ignored local tooling storage.

## Expanded UI inspection

The user reopened the app and authorized operating its controls. The globe button
opened a horizontal Map Select carousel with globe previews. Observed themes:
Los Santas, PipBoy, Pixel Map, Night City, San Andrias, Heat Racing, The Rivia III,
and Saint Denis II. Several show an Unlock Map action; the Android Auto screen
marked San Andrias active.

Settings exposed metric/imperial distances, Android Auto Settings, share/rating,
community/contact, privacy/terms, and Clean My Data. Android Auto Settings has
an always-night switch and day map selection. No community, purchase, rating,
contact, or data-deletion action was activated. Theme browsing did not unlock or
change third-party entitlements. Search inspection was interrupted by screen lock.
