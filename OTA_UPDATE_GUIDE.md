# Sovereign Over-The-Air (OTA) Updates Guide - Cosmo Compendium

This application receives direct Over-The-Air updates from your self-hosted sovereign VPS server.

## Overview
- **VPS Host:** `https://api.cosmowhisper.com`
- **Application Slug:** `cosmo-compendium`
- **Metadata Endpoint:** `https://api.cosmowhisper.com/updates/cosmo-compendium/version.json`
- **Binary Download:** `https://api.cosmowhisper.com/updates/cosmo-compendium/app-latest.apk`
- **Upload Endpoint:** `POST https://api.cosmowhisper.com/updates/cosmo-compendium/upload`

## In-App Update Mechanism
1. **Startup Check:** Whenever the app launches or returns to the foreground, `AppUpdateManager` queries the VPS asynchronously.
2. **Installation Prompt:** If a newer `versionCode` is hosted on the VPS, an `AlertDialog` presents the version and release notes with a one-tap "Download & Install" button.
3. **In-Settings Check:** Users can also go to Settings -> "Sovereign Over-The-Air (OTA) Updates" and tap "Check for Updates Now".

## Publishing a New Release
### Option A: From Desktop
Double-click `Publish_Cooking_App_Compendium.bat` inside `C:\Users\louis\Desktop\OTA Update Publishers`.

### Option B: From Terminal / PowerShell
```powershell
cd E:\Documents\GitHub\cookbook
.\publish-update.ps1 -Notes "Culinary enhancements, updated recipes and performance fixes"
```

The script will:
1. Increment `versionCode` in `app/build.gradle.kts`.
2. Compile the debug APK with Gradle (`./gradlew assembleDebug`).
3. Upload the APK and version metadata securely to the VPS.
