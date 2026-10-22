# WebRTC bridge implementation checklist

The website and signaling server deliberately include `signal` and `screen-ready` events, but the end-to-end WebRTC transport is not yet implemented. The correct next step is to choose and pin a maintained WebRTC Android SDK version, then implement and test both ends together.

## Browser controller
- Create `RTCPeerConnection` with STUN and a TURN server you operate.
- On `ontrack`, assign the remote stream to `#remoteVideo`.
- Send offer/answer and ICE candidates over the authenticated Socket.IO `signal` event.
- Map the remote video viewport coordinates to the Android capture dimensions, accounting for letterboxing, rotation and display insets.
- Send gestures only after the target explicitly accepts and enables remote control.

## Android target
- Initialize `PeerConnectionFactory`.
- Create `ScreenCapturerAndroid` from the one-time MediaProjection result.
- Add the video track to a peer connection and exchange SDP/ICE via Socket.IO.
- Verify every control message against the current short-lived pairing session.
- Call `dispatchRemoteTap` only when the owner has enabled Accessibility, the session was accepted, and the session has not expired.
- Stop and release capture/peer resources immediately when projection stops or either side disconnects.

## Do not
- Hide the MediaProjection foreground notification.
- auto-enable Accessibility or misrepresent why it is needed.
- permit unauthenticated control events or permanent pairing codes.
- claim universal control of every app/device.
