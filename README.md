# OPNsense Manager

A native Android app to manage device-level firewall blocking on your [OPNsense](https://opnsense.org/) firewall — no web browser needed.

## Features

- **Device list** — auto-discovered from DHCP leases with online/offline status
- **One-tap block/unblock** — instantly adds/removes devices from a firewall alias
- **Blocked-only filter** — filter the list to show only blocked devices
- **Active connections** — tap any device to see its live pf state table (protocol, source, destination, bytes)
- **Rename devices** — save friendly names stored locally on your phone
- **Biometric lock** — optional fingerprint/face unlock to protect app access
- **Secure credentials** — API key and secret are AES-256 encrypted on-device and masked in the UI

## Requirements

- Android 8.0+ (API 26)
- OPNsense firewall accessible on your local network
- OPNsense API key & secret ([how to create one](https://docs.opnsense.org/development/how-tos/api.html))

## Setup

1. Install the APK (see [Releases](../../releases))
2. Open the app — enter your **Firewall URL**, **API Key**, and **API Secret**
3. Tap **Test & Connect** — you'll land on the device list

## How Blocking Works

The app creates a firewall **alias** named `android_app_blocked` on first block action, and a single **block rule** targeting that alias. Blocking a device adds its IP to the alias; unblocking removes it. Changes are applied via the OPNsense REST API immediately.

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Networking | Retrofit 2 + OkHttp 4 + Moshi |
| Local DB | Room |
| Credentials | EncryptedSharedPreferences + Android Keystore |
| Biometrics | AndroidX Biometric |
| Min SDK | API 26 (Android 8.0) |

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK → releases/OpnsenseManager-x.y.z.apk
./gradlew buildRelease
```

## Contributing

PRs welcome. Open an issue first for major changes.

## License

MIT
