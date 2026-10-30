# Building and releasing Waypoint

The public update channel is `the-geeky-couldron/waypoint` on GitHub Releases. Android installation remains user-confirmed. This APK distribution is intended for GitHub sideloading, not Google Play in-app updates.

## What runs automatically

- Pushes to `main`, pull requests and manual runs: version validation, 27 map-style checks, debug APK, unit tests and Android lint. Reports and the debug APK are saved for 14 days.
- A pushed `v*` tag: the same checks, followed by an optimized, signed APK. GitHub publishes `Waypoint.apk` and `SHA256SUMS` only after those jobs succeed.
- Pull requests never receive the release signing secrets. Actions are pinned to commit hashes. The release job alone receives repository-content write permission.

## Signing

The release job requires four repository Actions secrets:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 contents of the signing keystore |
| `ANDROID_STORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Signing key alias |
| `ANDROID_KEY_PASSWORD` | Private-key password |

Keep an encrypted, offline backup of the keystore. Never commit it, paste it into issues, or upload it as a workflow artifact. The workflow restores it only in the runner's temporary directory and removes it after the build. Release builds fail if the signing secret is missing.

The initial project installation used a local development signing identity. Its existing identity is retained for this personal release channel so installed copies can update without losing saved places. Forks should create and retain their own release key before distributing their first APK; a fork's APK cannot replace the official release. Do not generate a fresh key on each workflow run.

Local release builds accept `WAYPOINT_KEYSTORE`, `WAYPOINT_STORE_PASSWORD`, `WAYPOINT_KEY_ALIAS` and `WAYPOINT_KEY_PASSWORD` environment variables. Without them, local builds use Android's debug signing configuration. GitHub's release workflow always requires the explicit signing secrets.

## Publish an update

1. Change `version.properties`. Use a stable `major.minor.patch` version, each component between 0 and 999. Set `VERSION_CODE = major × 1,000,000 + minor × 1,000 + patch`; always increase it.
2. Update `docs/releases/current.md` with the new release notes. Update tests/screenshots when behavior or appearance changes.
3. Run verification, commit, push, then tag the exact commit:

```sh
python3 scripts/check_version.py
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
# Example only: first change version.properties and release notes to match.
git add version.properties docs/releases/current.md
git commit -m "Release 0.2.1"
git push origin main
git tag v0.2.1
git push origin v0.2.1
```

Watch the Android workflow. Its release job verifies that the tag and version file agree. After publication, verify the APK and checksum on the release page. Never overwrite an already-published version: make a new version for corrections.

## Updater behavior

Settings offers a manual check against GitHub's latest stable release API. Drafts, prereleases and older/equal versions are ignored. No background polling, GitHub credentials, or location information is involved.

Before showing Install update, Waypoint checks:

- Stable semantic version and a higher Android version code.
- Exactly one uploaded `Waypoint.apk` asset at this repository's expected release URL.
- HTTPS downloads restricted to GitHub's release hosts.
- Declared size (maximum 150 MiB), complete download and GitHub's SHA-256 asset digest.
- Correct Android package, version name/code and the same signing identity as the installed app.

Partial files are discarded when a download fails or is cancelled. Downloads survive screen rotation while the app process lives; if Android ends the process, check and download again. Only the dedicated cache/updates directory is shared with Android's installer through a non-exported FileProvider. Android may require the user to allow installation from Waypoint. The app never toggles that setting or installs silently. Installation is disabled during active navigation.

Private GitHub repositories need authenticated release access and are not supported by this public, token-free update channel. A fork must change `ReleaseUpdate.REPOSITORY`, README links and the signing identity to its own values.
