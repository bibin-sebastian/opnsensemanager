# OPNsense Android App — Design Spec
**Date:** 2026-04-05

---

## Overview

A native Android app that interfaces with a local OPNsense firewall via its REST API. The primary use case is managing network access per device — blocking/unblocking individual devices and scheduling access windows — in a clean, user-friendly UI. Designed to be open-source and work with any OPNsense instance (no hardcoded values).

---

## Architecture

### Layers

```
UI Layer         — Jetpack Compose + Material 3
ViewModel Layer  — StateFlow, coroutines, Hilt DI
Repository Layer — OPNsenseRepository + LocalRepository
Data Layer       — Retrofit/OkHttp (REST API) + Room (local DB)
```

### OPNsense Integration

- **Auth:** API key + secret stored encrypted in Android Keystore, sent as `Authorization: Basic base64(key:secret)` on every request.
- **Firewall URL:** User-configurable (e.g. `https://192.168.10.1`). Stored in encrypted SharedPreferences. No hardcoded values.
- **Self-signed TLS:** OkHttp configured to trust OPNsense's self-signed cert (user-acknowledged on first connection).
- **Device discovery:** `GET /api/dhcpv4/leases/searchLease` — returns hostname, IP, MAC, online status.
- **Blocking mechanism:**
  - Create a persistent firewall Alias (type: host) named `android_app_blocked` via `/api/firewall/alias/`.
  - Add/remove device IPs from that alias to block/unblock.
  - A single firewall Filter rule blocks all traffic from this alias (created once on first block action).
  - Apply changes via `/api/firewall/alias/reconfigure` and `/api/firewall/filter/apply`.
- **Schedules:** OPNsense supports schedule objects via `/api/firewall/schedule/`. A schedule is attached to the filter rule for time-based blocking.

### Local Database (Room)

| Table | Fields |
|---|---|
| `device_alias` | mac, friendly_name, group_name (nullable) |
| `schedule` | id, device_mac, start_time, end_time, days_of_week |
| `app_settings` | key, value (encrypted URL, API key ref) |

---

## Screens & Navigation

### Navigation Structure

Bottom navigation bar with two tabs: **Devices** and **Settings**.

### 1. Onboarding (first launch only)
- Input: Firewall URL, API Key, API Secret
- "Test Connection" button — verifies credentials and fetches a DHCP lease
- On success: navigate to Device List and never show again

### 2. Device List (Home)
- Pull-to-refresh
- Each row: device icon, friendly name (or hostname fallback), IP, online indicator dot, block toggle
- Blocked devices shown with red tint
- FAB or swipe-to-reveal for quick schedule access
- Tapping a row opens Device Detail

### 3. Device Detail (full-screen bottom sheet)
- Rename device (saved locally in Room)
- Block toggle (immediate apply)
- Schedule block: time range picker + day-of-week selector
- Assign to group (nice-to-have, can be added later)

### 4. Settings
- Firewall URL (editable)
- API Key + Secret (editable, masked)
- "Test Connection" button
- App version / open-source link

---

## Build & Release

### APK Output to `releases/` folder

A Gradle task copies the built APK into `releases/` at the project root:

```kotlin
// In app/build.gradle.kts
tasks.register<Copy>("buildRelease") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release"))
    into(rootProject.layout.projectDirectory.dir("releases"))
    include("*.apk")
    rename { "OpnsenseManager-${android.defaultConfig.versionName}.apk" }
}
```

Run with: `./gradlew buildRelease`

Output: `releases/OpnsenseManager-x.y.z.apk`

---

## Tech Stack

| Concern | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Networking | Retrofit 2 + OkHttp 4 + Moshi |
| Local DB | Room |
| Async | Kotlin Coroutines + Flow |
| Credential storage | Android Keystore + EncryptedSharedPreferences |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 |

---

## Error Handling

- Network unreachable (not on local network) → banner: "Cannot reach firewall"
- Auth failure (403) → redirect to Settings with message
- Self-signed cert warning → one-time dialog on first connection
- Partial apply failure → show error toast, revert toggle state in UI

---

## Nice-to-Haves (out of scope for v1)

- Device grouping (block/unblock an entire group)
- Category/service blocks (DNS-based or by destination IP range)
- iOS port

---

## Verification

1. Run app on emulator or device connected to the same local network as OPNsense.
2. Complete onboarding with real API key — verify device list populates from DHCP leases.
3. Toggle block on a device — verify the IP appears in the OPNsense alias and traffic is blocked.
4. Set a schedule — verify OPNsense schedule object is created and linked to the filter rule.
5. Run `./gradlew buildRelease` — verify APK appears in `releases/` with correct version name.
6. Change firewall URL in Settings → reconnect — verify no hardcoded URLs remain.
