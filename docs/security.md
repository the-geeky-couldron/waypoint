# Security review and hardening

Waypoint 0.2.1 includes a source audit of the Android app and release path, followed by remediation. The baseline reviewed was `2d63dbd57e2622ffa133983dfe04607f9ca43739`. Review covered destination intents, local storage, exported components, location input, provider responses, update downloads and GitHub Actions.

The audit used independent source reviewers and candidate verification. **No exploit was demonstrated on an Android runtime.** The required isolated execution environment was unavailable, so cross-app delivery, process failure and data access hypotheses remain validation leads rather than confirmed vulnerabilities. Normal development builds and regression checks are reported separately in [validation](validation.md).

## Changes in this release

| Boundary | Source concern | Remediation |
| --- | --- | --- |
| Shared destination → active guidance | An incoming link could select a new destination and stop the current trip. | Active guidance rejects external destination proposals. Only supported VIEW links with bounded, validated data are accepted. |
| Delayed address search → current selection | A stale lookup could overwrite a newer choice or a started trip. | Imports retain a cancellable job and generation. Selection, clearing, searching and guidance transitions invalidate obsolete work; publication checks cancellation and current state. |
| Experimental car service → app state | Host authentication was not consistently bound to later calls; malformed or out-of-order protocol input reached unchecked library paths. | Android Auto is disabled, not exported and no longer advertised. No settings toggle can enable it. Phone navigation stays available. |
| Provider response → navigation | Byte size, parser allocation, numeric fields and geometry complexity needed stronger local bounds. | Decompressed byte limits, strict JSON syntax, nesting/value limits, finite geographic/numeric validation, collection/work limits and cancellable calls. Route preparation runs off the main thread. |
| Sensor fix → heading/progress | Invalid coordinates, accuracy, timing or altitude could contaminate calculations. | Invalid fixes are rejected; speed is sanitized and declination altitude is bounded. This does not establish an unauthorized sensor-injection path. |
| Build input → signed APK | Wrapper verification and cache policy relied on defaults. | Gradle 8.14.6, pinned distribution checksum, URL validation, explicit read-only cache policy outside main pushes and no Gradle cache in signing jobs. |
| Generator input → committed assets | Regeneration could silently consume changed remote input. | Base-style and bundled glyph-generator hashes are verified before regeneration. CI builds committed styles instead of fetching a new base. |

Disabling the Android Auto component contains the reviewed paths; it does **not** fix the underlying library or prove every possible car-host interaction safe. Re-enabling requires isolated cross-UID tests, caller/session ownership checks, malformed-input handling, lifecycle validation and actual head-unit testing. Updating the library alone is insufficient evidence.

## Controls already present

- HTTPS-only app network policy; no WebView/native JavaScript bridge.
- Private saved-place storage, with backup and device transfer disabled.
- A non-exported update FileProvider restricted to its update cache.
- Updates restricted to this repository's stable releases and GitHub download hosts.
- Download size and SHA-256 verification, followed by package, version and signer checks.
- Android installation permission and confirmation remain under the user's control.
- No GitHub token, app account, analytics backend or recorded location history.
- Commit-pinned Actions and signing secrets restricted to the release job.

## What remains unverified

The review does not certify MapLibre's native tile, font, image or terrain decoders; Android framework internals; hosted provider behavior; compromised maintainers; signing-key custody; or every transitive dependency. Updater key rotation intentionally fails closed until a signing-lineage policy is designed. Dependency verification and repository tag/environment protection remain maintenance opportunities, not demonstrated takeover paths.

Android cross-app exploit fixtures were not run. Do not infer a tested attack, severity or guaranteed exploitability from a source trace. Functional unit tests, package configuration checks and ordinary updater UI checks are different evidence. See the current [validation record](validation.md) for completed checks and device limitations.

No single audit makes software “bulletproof.” Report new concerns through the [private reporting process](../SECURITY.md).

## Maintenance references

Gradle's [release checksums](https://gradle.org/release-checksums/) and [wrapper verification guidance](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:verification) support the distribution pin. Gradle 8.14.6 addresses the upstream [repository failover](https://github.com/gradle/gradle/security/advisories/GHSA-j5m7-59rp-24f5), [client/daemon](https://github.com/gradle/gradle/security/advisories/GHSA-xwqc-3h47-hg64) and [worker/daemon](https://github.com/gradle/gradle/security/advisories/GHSA-mvvg-497x-hmj8) advisories; this is dependency maintenance, not proof those issues were exploited here.

The [AndroidX Car release notes](https://developer.android.com/jetpack/androidx/releases/car-app) document upstream security fixes. The source remains on 1.8.0-rc01 while the experimental component is disabled. Android's [deep-link security guidance](https://developer.android.com/privacy-and-security/risks/unsafe-use-of-deeplinks) describes the external-input boundary.
