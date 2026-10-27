BD DANGER ALERT SYSTEM — COMPLETE FILES

Upload monitor.html, alert.html, and admin.html to the ROOT of the same GitHub Pages repository.

TEST SETUP:
1. Firebase Console > project king-59c08 > Realtime Database > Rules.
2. For TESTING ONLY, publish firebase-rules-test.json.
3. Open monitor.html from your HTTPS GitHub Pages URL. Enter name and Room ID (example BD123), click Start, allow GPS.
4. Keep Monitor open. Open alert.html on another device, enter exactly BD123 and click Connect.
5. Open admin.html to see all rooms automatically.

TROUBLESHOOT:
- Permission denied: rules not published, wrong project, or wrong database URL.
- Room not found: start Monitor first and match Room ID exactly.
- No GPS: HTTPS, device location enabled, browser permission granted.
- Admin empty: root /bdDangerRooms read permission needed in testing rules.

SECURITY: These testing rules expose all GPS/contact data publicly and allow anyone to modify it. Never use them for real sensitive data. Production requires authentication and restrictive rules. GitHub Pages cannot guarantee background tracking or notifications when the page is closed. This system does not contact emergency services.
