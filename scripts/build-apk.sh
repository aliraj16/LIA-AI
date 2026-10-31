#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."
echo 'MILI AI APK build helper'
echo 'Checking Java and Android SDK...'
if ! command -v java >/dev/null 2>&1; then
  echo 'Java 17 is missing. In Termux: pkg update && pkg install openjdk-17 git unzip'
  exit 1
fi
if [ -z "${ANDROID_HOME:-}" ] && [ -z "${ANDROID_SDK_ROOT:-}" ]; then
  echo 'ANDROID_HOME/ANDROID_SDK_ROOT is not set.'
  echo 'Most Android SDK build-tools are x86_64 Linux binaries; native Termux ARM builds may fail.'
  echo 'Recommended: push this project to a GitHub repository, then use Actions -> Build MILI AI APK -> Run workflow.'
  echo 'The included .github/workflows/android.yml builds the APK and uploads it as an artifact.'
  exit 2
fi
export ANDROID_HOME="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
chmod +x ./gradlew
./gradlew --no-daemon assembleDirectDebug
APK=$(find app/build/outputs/apk/direct/debug -name '*.apk' -type f | head -n 1)
if [ -n "$APK" ]; then echo "APK ready: $APK"; else echo 'Build finished but APK not found'; exit 3; fi
