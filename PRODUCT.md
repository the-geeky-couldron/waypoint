# Waypoint

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Implementation choice: Kotlin, Jetpack Compose, Material 3, MapLibre Native. User explicitly requests native Android.

## Product Purpose

Real-time GPS navigation with visual themes inspired by GTA V, Red Dead Redemption, and other games. The user explicitly clarified this is navigation, not a playable map.

## Users

The user navigating real streets on an Android phone. This began as a private hobby app. The user now explicitly requests publication to the GitHub account signed in through Helium, GitHub Actions Android builds, an in-app updater and a marketing README. The user has chosen public open-source distribution at https://github.com/the-geeky-couldron/waypoint and completed GitHub CLI authorization through Helium. GitHub Actions builds the APK and publishes signed, versioned releases.

## Capabilities and Constraints

Sensor-assisted real-time navigation: fused GNSS/network positioning, gyroscope/accelerometer/compass heading, true-north correction, accuracy/calibration feedback. Destination search, car, motorcycle and walking routes, spoken turn instructions, rerouting, route progress, arrival, and saved destinations. Visual gamification only: no quests, missions, XP, rewards, or invented gameplay. Android Auto is shown in a reference but is not an explicit requested integration. First version uses online OSM services; no traffic-aware routing or offline routing is promised. Location and destination are sent to map/geocoding/routing providers only as necessary. Saved places and preferences remain on the device.

## Brand Commitments

Actual game HUD samples recorded in docs/visual-sourcebook.md refine the visual direction. Six supplied screenshots establish full-screen themed maps, bold game-inspired typography, high-contrast place symbols and visible route lines. Twenty-seven game-inspired themes adapt researched cartographic/HUD rules using original graphics, without copying game assets. Working app name: Waypoint.

## Product Principles

- Real navigation is the core function.
- The game inspiration changes cartography and visual tone, not road rules.
- GPS state, route progress, and permission problems remain visible.
- Native Android touch targets, Back behavior and accessibility remain intact.

## Confirmed UI refinements

The launch surface is a native animated welcome screen with Open map and world selection. Only game variants are offered. In every launcher/library preview, the map-style name is large and the game name is smaller, with uniform type sizes. Map actions have visible explanatory labels. Distinct per-game placement remains pinned. Theme changes remain available during route preview and active navigation without losing the route.

The release extension adds manual update checks and downloads in Settings. Stable GitHub release APKs must pass checksum, package, version and signing-identity checks before Android's installer opens. Installation is user-confirmed and disabled during navigation. The prepared token-free update channel requires public GitHub Releases. Public documentation screenshots use explicit demonstration coordinates, never the user's GPS position.
