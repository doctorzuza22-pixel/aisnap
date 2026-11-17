# DUMA SNAP

DUMA SNAP is a native Android camera app built with CameraX.

## Current camera features
- Live rear/front camera preview
- Photo capture and video recording
- Flash modes (off/on/auto)
- Camera zoom controls (device-supported range)
- Highest available CameraX video quality selection
- Clean DUMA-branded dark camera interface

## Build
Push to `main` or run **Build DUMA SNAP APK** manually in GitHub Actions. The workflow downloads the original DUMA Browser logo at build time, creates Android launcher icon sizes, builds the debug APK with JDK 17, and uploads the APK as `DUMA-SNAP-APK`.

Package: `com.duma.snap`

## Quality note
Actual photo/video quality, optical zoom, stabilization, HDR and maximum recording resolution depend on the phone's camera hardware and Android camera implementation. The app selects the highest video quality exposed by the device; it does not claim to reproduce another phone's physical camera hardware.
