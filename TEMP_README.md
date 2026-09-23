# TEMP README — Auth + Paywall

## Goal
- Support multi‑device parent login with Firebase UID as the single identity across Android/iOS.
- Add Google + Apple sign‑in alongside email/password, with provider linking and account‑collision handling.
- Keep device‑specific state (active child profile, FCM token) per device.
- Ensure paywall uses RevenueCat offerings (dynamic pricing from console) with consistent appUserID.

## Done so far
- **Auth identity**: UID‑first Firestore reads/writes (`users/{uid}`), not email‑based.
- **User doc creation**: Create minimal `users/{uid}` after any successful sign‑in; set `primaryEmail` when available.
- **Provider linking**: Handle `account-exists-with-different-credential`, store pending credential, and link after successful sign‑in.
- **Email provider mismatch guidance**:
  - On email sign‑up/sign‑in errors, call `fetchSignInMethodsForEmail`.
  - If the email is linked to Google/Apple, show a clear message and collapse the email form so social buttons are visible.
  - If it’s an existing email/password account, prompt to log in.
- **Apple Hide My Email**: Treat email as optional; prompt for `contactEmail` when missing.
- **Device‑scoped state**:
  - `users/{uid}/devices/{deviceId}` stores `activeProfileId`, `fcmToken`, `platform`, `lastSeen`.
  - Active profile resolution prefers device doc; legacy `activeProfileIndex` is avoided.
- **RevenueCat**: `Purchases.logIn(uid)` after sign‑in; entitlements synced by UID.
- **UI (Auth)**:
  - Landing screen shows only “I’m new” / “I’m already”.
  - These open a **new auth options screen**.
  - Social buttons + “Email and password” shown there; email form collapses until tapped.
  - Social buttons are full‑width, black, and Google appears first with subtext.
  - Back button sits **below** the auth options card container.
- **Google sign‑in**: Added debug SHA‑1/256 to Firebase, updated `google-services.json` (validated in repo).

## Next steps
- Finish Apple sign‑in setup in Firebase + Apple Developer (Service ID, Team ID, Key ID, private key, return URL).
- Add Play App Signing SHA‑1/256 in Firebase once Play Console is configured.
- (Optional) Add a “link password” flow after Google/Apple sign‑in for dual‑provider login.
- Re‑test Google/Apple sign‑in on both Android and iOS for UID consistency.
