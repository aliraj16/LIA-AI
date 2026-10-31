# MILI AI — ALIRAJ EDITION

Android source project based on the user's original `lia-ai-master.zip`. The original Android source tree, direct/play product flavors, existing screens, voice pipeline, accessibility module, Website Forge module, and tests have been retained while adding MILI branding and a separate ALIRAJ web-admin service.

## Included in this ZIP

### Android application source
- Existing Gemini text chat and Gemini Live voice client.
- Existing animated Compose UI / 3D orb, chat, history, quick actions, settings, personality, profile, permissions, privacy, debug and Website Forge screens.
- Existing voice selection, audio streaming, echo guard and conversation/session context code.
- Existing direct/play product flavors and accessibility service code.
- MILI AI name and creator instruction: Developer Aliraj.
- Pink/blue/purple theme accents and `POWERED BY ALIRAJ` settings branding.
- Bangla language option and creator identity in the system prompts.
- Owner-security settings section (experimental switch only; not biometric security).
- `MiliAdminApi.kt`: reusable Android HTTP client for public remote config, license activation request and license status-check endpoints. This client is provided as integration code; the current UI/runtime does not yet call it automatically.

### ALIRAJ Web Admin Panel (`admin-panel/`)
- Admin login with expiring signed token.
- Edit AI personality, model, voice and system prompt configuration.
- Feature flags.
- Generate Premium/Lifetime license keys.
- View pending activation requests and approve/reject them.
- Device binding on approval, license expiry and revocation.
- Public activation-request and license-check API endpoints.
- Browser dashboard refreshes requests periodically and can show browser notifications when permission is granted.
- JSON-file storage for easy testing; persistent production database is recommended.

### Build and setup
- Gradle wrapper and Android build scripts.
- GitHub Actions workflow for building APKs from a phone using GitHub.
- Termux setup guide.

## Important: what is and is not complete

This is a source project package, not a verified ready-to-install APK. The original LIA source was not supplied with a backend deployment or credentials. It is not technically possible to guarantee every Android feature works without building and testing on the target phone and connecting real API services.

1. **Voice Match is not implemented as secure speaker biometrics.** The Settings switch is a UI preference only. A secure owner-only voice gate requires a real speaker-verification model, voice enrollment, replay/liveness protection, calibration, and on-device testing. Do not use the current switch to protect private data or actions.
2. **Admin-to-app live control is not fully wired.** The web backend and Android API client are included, but the app screens and AI runtime do not yet automatically fetch/apply remote config or submit activation requests. The client class is a starting integration point.
3. **Admin notifications:** browser dashboard polls while open. This is not push notification delivery to a closed browser/phone. Real push requires Firebase Cloud Messaging and deployment credentials.
4. **WhatsApp/Facebook/Instagram automation:** the original project contains tool declarations/task structures, but some actions are placeholders/queued and are not end-to-end automation.
5. **Floating character and live camera vision:** the original source does not contain a complete production-ready floating assistant + live camera vision implementation.
6. **MILI portrait:** the settings header currently uses a stylized M avatar, not a photographic portrait. Add an approved portrait asset to replace it.
7. **Admin backend:** JSON storage is suitable for local testing only. For production, use HTTPS, persistent database, backups, rate limiting, monitoring, and secret management. Never place the admin password or token secret in the APK.
8. **Build verification:** no APK compilation or physical-device QA is claimed. Android SDK/Gradle dependencies must be downloaded, and the build must be tested.

## Build APK

### Recommended: GitHub Actions from a phone
1. Create a private GitHub repository.
2. Upload all files in this project, including the hidden `.github` folder.
3. Open Actions → **Build MILI AI APK** → Run workflow.
4. Download the APK artifact after the workflow completes.

### Termux local build
Ordinary Android phones run ARM64, while parts of Android SDK Build Tools are often distributed as x86_64 Linux binaries. Therefore local Termux builds may not work on every phone. If a compatible Android SDK is installed:

```bash
pkg update -y
pkg install openjdk-17 git unzip -y
unzip MILI-AI-Termux-Project.zip
cd lia-ai-master
chmod +x gradlew scripts/build-apk.sh
./scripts/build-apk.sh
```

If `ANDROID_HOME` or SDK build-tools are missing, use GitHub Actions.

## Run the admin panel locally in Termux

```bash
pkg install nodejs-lts openssl -y
cd admin-panel
export ADMIN_USER=aliraj
export ADMIN_PASSWORD='use-a-unique-password-at-least-12-characters'
export TOKEN_SECRET="$(openssl rand -hex 32)"
node server.js
```

Open `http://127.0.0.1:8787` in the same phone's browser. This is local testing only. Remote access requires HTTPS hosting and persistent storage.

See `TERMUX-START-HERE.txt` and `admin-panel/README.md`.

## Floating Microphone (new)
Open **Settings → Floating Assistant → Floating Microphone** and enable it. Android will ask you to allow **Display over other apps**; grant that permission and return to MILI AI. A draggable microphone bubble will appear above other apps. Tap it once to start the existing MILI voice session and tap again to stop it. The bubble does not listen in the background by itself; microphone capture starts only after an explicit tap and Android's microphone permission is required. Disable the switch to remove the bubble.
