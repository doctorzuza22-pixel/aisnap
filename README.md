# AI SNAP — Android Camera Starter

Native Android project for package `com.aisnap.camera.pro`.

## Features in this starter
- CameraX live camera preview (front/back camera switch)
- Capture photos to `Pictures/AI Snap`
- Background and outfit preset selector UI (12 choices each)
- Dark, short-video-inspired camera UI
- GitHub Actions workflow that compiles and uploads a debug APK

## Important capability note
This project is a camera starter, not a finished AI photo editor. The preset buttons currently select a preset and explain that an AI image-edit service must be configured. The Enhance button is also a placeholder. No OpenAI API key is embedded in the app; production AI editing should call a secure backend that stores the API key. This avoids exposing secret keys inside an APK.

## Build
Requires JDK 17 and Android SDK platform 35. Run `./gradlew assembleDebug`. The debug APK is `app/build/outputs/apk/debug/app-debug.apk`.

The included wrapper launcher downloads Gradle 8.9 into the standard Gradle user cache when first run. GitHub Actions is configured in `.github/workflows/android-build.yml`.
