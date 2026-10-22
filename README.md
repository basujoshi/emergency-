# BD Mobile Remote Support — source project

This is a source starter for a consent-based Android remote-support system. It includes:
- Web dashboard / controller UI
- Node.js Socket.IO signaling server
- Android Studio Kotlin helper-app scaffold for MediaProjection and an explicitly enabled AccessibilityService

## Important truth about completeness
This source is a development scaffold, not a production-certified remote-control product. The web and signaling pieces are provided; Android WebRTC offer/answer and ICE bridging must be completed and tested against the chosen WebRTC SDK version before live screen streaming works end-to-end. The Android service demonstrates the required consent surfaces and gesture handling, but does not silently enable control. Do not deploy this as a finished product or claim full remote control until you have completed, built, and tested the WebRTC bridge.

## How it is intended to work
1. Deploy `server/` on a Node.js host with HTTPS/WSS (not static-only hosting).
2. Host `web/` on `https://bashurod.com.np/remote/` or your preferred path.
3. Build/install the Android helper on the target Android phone.
4. The phone owner opens the helper, creates a short-lived pairing code, grants Android screen-capture consent, and explicitly enables Accessibility if they want remote gestures.
5. The support agent enters the code in the website. Both sides see a visible active session and can disconnect.
6. WebRTC sends screen video; authenticated signaling relays SDP/ICE; remote gesture messages are validated and passed to the enabled Accessibility service.

## Server setup
```bash
cd server
npm install
cp .env.example .env
npm start
```
Set `PORT` if needed. Put the server behind HTTPS/WSS using your hosting provider/reverse proxy. Do not use a static-only host for the signaling server.

## Website setup
Edit `web/config.js` and set `SIGNALING_URL` to your WSS endpoint, e.g. `wss://remote-signal.example.com`. If deploying same-origin, use `location.origin`.

## Android setup
Open the `android/` folder in Android Studio. Let Gradle sync. Use a current Android SDK. The Android project includes:
- a visible pairing/session UI,
- the system MediaProjection consent prompt,
- a foreground notification service placeholder,
- AccessibilityService metadata and gesture dispatch,
- Socket.IO pairing/control message scaffolding.

The exact WebRTC native API varies by SDK release. Complete the `TODO: WEBRTC BRIDGE` sections in `RemoteSession.kt` before expecting video to reach the browser.

## Security requirements before public launch
- Use authenticated users and server-side authorization (Firebase Auth/custom claims or your own identity provider).
- Store only hashed, short-lived pairing tokens; rate-limit code attempts.
- Add per-session authorization for every signaling/control event.
- Use TLS/WSS; rotate secrets; never put server secrets in browser code.
- Log consent/session start/end; expire sessions automatically.
- Do not hide the Android sharing notification, bypass OS permission prompts, or persist remote-control permission without user awareness.
- Test on multiple Android/OEM versions and on mobile networks behind NAT.
- Consider privacy, accessibility-service disclosure, Google Play policies, and local law before distributing.

## Platform limits
- Android screen capture requires user consent through MediaProjection. Android version/OEM restrictions vary.
- Accessibility gesture dispatch only works after the user explicitly enables the service; it cannot guarantee control of every secure screen, protected app, system prompt, or OEM-specific UI.
- iOS does not permit a normal website/app to arbitrarily control another iPhone's UI. This project targets Android-to-Android only.
- This project does not bypass locks, OTPs, banking protections, or device security controls.
