# Pak GSM Repair 6.2

## Version 6.2 — corrective update

- Corrected visible version labels on the login screen, app header, and Settings. These labels were hard-coded as 5.8 even though the internal updater version had advanced.
- Reduced the Repair Shop Calculator panel to a maximum width of 320 px, reduced its display font and padding, and tightened keypad spacing.
- Restored the File Vault screen and separate Dashboard shortcuts for **Flash Files**, **Dump Files**, and **Keypad Flash Files**. Missing built-in folders are added by ID or name without deleting existing files or custom folders.
- Added optional device-local file saving through IndexedDB, plus file links and details. Saved local files can be downloaded from their folder. Files remain on this device; they are not automatically backed up to cloud.
- Registration keeps **Full Name** and hides the duplicate **Username** field; an internal username continues to be generated for the current registration API.
- The post-update **What’s New** popup lists the v6.2 changes once after the update is applied.

## Test checklist

1. Open the app and tap **Check for Update**. It should report v6.2.
2. Tap **Download & Apply**, wait for reload, and confirm the login page and header show v6.2.
3. Confirm the Calculator is visibly narrower and the keys are smaller.
4. From Dashboard, open Flash Files, Dump Files, and Keypad Flash Files separately. Add a test filename; optionally choose a small file and verify it can be downloaded again.
5. Verify existing customer records and folder items remain present.
6. Open Register: Full Name should remain visible and Username should remain hidden.

## Update notes

- `android-web-update/index.html` and its `update.json` are the source for the Android WebView's HTML-only updater.
- Root `index.html`, `update.json`, and `sw.js` serve the PWA release. Android embedded assets are kept synchronized for new APK builds.
- This update changes the web UI only. Native Android code and Bluetooth permissions are unchanged.
- The Android package version is 6.2 (`versionCode` 602); the installed display name remains **Pak GSM Repair**.

---

## Version 6.1 release notes

- Reduced the dashboard Repair Shop Calculator size: narrower panel, smaller display, tighter spacing, and smaller keys.
- Restored missing built-in **Dump Files**, **Flash Files**, and **Keypad Flash Files** folders on existing installations. The migration adds only missing built-in folders and preserves existing files and custom folders.
- Simplified registration: **Full Name** remains visible; the duplicate **Username** field is removed. The app generates a compatible internal username from the email and phone details so the current registration API continues receiving its required field.
- After the v6.1 update is applied and the app view reloads, a one-time **What’s New** popup lists these changes. Tap OK to mark the release notes as seen.
- Published version metadata is synchronized to 6.1 across the PWA, Android HTML-update package, and GitHub Pages update package.

## Recommended v6.1 test sequence

1. Open the installed app and choose **Check for Update**.
2. Confirm that it reports v6.1, then tap **Download & Apply**.
3. Wait for the app view to restart and confirm the v6.1 release-notes popup appears.
4. Confirm the calculator is smaller; open File Vault and verify Flash Files, Dump Files, and Keypad Flash Files are present.
5. Open Register and verify only Full Name (not Username) is shown; do not create a duplicate test account unless you intend to.
6. Re-open the app and confirm the release-notes popup does not repeat after pressing OK.

## GitHub Pages setup

See `SETUP_GITHUB.md`. Upload the extracted **contents** to the repository root, not a nested folder. `update.json` must be published beside `index.html`.

## Update behavior and limitations

- The in-app updater downloads the new HTML from `android-web-update/index.html` for the native Android WebView build, then saves it through the native bridge. This release changes the web UI only; it does not update native Java code or Bluetooth permissions.
- The PWA updater stages `index.html`, `manifest.json`, `sw.js`, `cloud-config.js`, `update.json`, and app icons from the repository root before applying the update.
- For each future web release, update `APP_VERSION` and `update.json` together in the root and `android-web-update` package. Keep the version strings identical.
- If Android **Download & Apply** fails, use the GitHub Actions artifact `Pak-GSM-Repair-6.1-APK` as the fallback APK, then report the exact on-screen error before trying again.
- The APK display name remains **Pak GSM Repair**. The workflow artifact contains the renamed APK file `Pak-GSM-Repair-6.1.apk`.

## Existing project notes

- Run on the HTTPS GitHub Pages URL for service-worker and Google sign-in support.
- Customer data is local to the browser unless a cloud backup backend is configured.
- `Code.gs` is intentionally NOT included in this public GitHub Pages package. The backend is a separate Google Apps Script project and must not be uploaded to a public GitHub repository.

---

Mobile repair-shop workspace with customer records, repair jobs, File Vault, Parts Availability, settings, local backup/restore, and a user-triggered GitHub Pages update flow.

## GitHub Pages setup

See `SETUP_GITHUB.md`. Upload the extracted **contents** to the repository root, not a nested folder. `update.json` must be published beside `index.html`.

## Important: first install this updater fix manually

BOOTSTRAP FIX: This release addresses a stale 4.4.7 service worker and leftover partial staging caches. Do not apply this bootstrap ZIP using the old in-app updater. Extract it and upload/replace every file directly in the GitHub repository root, then commit and wait for GitHub Pages to publish. Open the published HTTPS site in Chrome once, wait a few seconds, close all app/site tabs, and reopen it. The new worker skips the waiting phase, takes control, and removes old app/staging caches while preserving localStorage customer data. The worker migration itself must be bootstrapped by publishing these files to GitHub; an old cached app cannot reliably self-replace its own broken updater.

## Manual update flow

Open **Settings → App Update → Check for Updates**. If `update.json` advertises a higher version, tap **Update App**. The app downloads and verifies the app shell/assets into a staging cache, then restarts to the approved release. The app shell is cache-first after this updater build is installed, so a normal GitHub commit is not shown as an update until the user approves it.

For each future app release, update the version in `index.html` (`APP_VERSION`) and `update.json` together. Keep `sw.js` unchanged for routine content-only releases; service-worker behavior changes require careful migration testing.

## Notes

- Run on the HTTPS GitHub Pages URL for service-worker and Google sign-in support.
- Customer data is local to the browser unless a cloud backup backend is configured.
- `Code.gs` is intentionally NOT included in this public GitHub Pages package. The backend is a separate Google Apps Script project and must not be uploaded to a public GitHub repository.


## Login OTP fix (5.3)
- Existing-account login checks the server with email + password and opens the app directly.
- Email OTP is requested only during new-account registration/email verification.
- If updating an existing deployment, upload the updated `index.html`, `sw.js`, and `update.json` from this ZIP. The Apps Script backend does not need a change for this front-end flow, provided `loginAccount` is already supported.


## Version 5.3 release checks
- Existing account login validates email + password and opens the app without sending email OTP.
- Email OTP remains only in the new-account registration flow.
- Dashboard header includes Shop Logo and Repair Status shortcuts.
- Shop logo is stored locally on the current device; repair status searches this device's saved repair records.
- Test the live deployment after uploading these files; this ZIP has not been tested against the live Apps Script server.


## Version 5.3 dashboard and login improvements
- Modern dashboard is now the first screen after sign-in, with responsive statistic cards and quick actions.
- Successful sign-in displays a compact welcome notification.
- The welcome notification can be dismissed with OK or by tapping outside it.

## Version 5.3 profile photo and Bluetooth panel
- Tap the round profile icon in the header to open Profile, then choose Change Photo. The photo is stored on this device and shown in the header icon.
- Customer/device photos can be attached while creating a repair record and are displayed with the customer record. Photos are stored locally; export backups before clearing browser data.
- The Bluetooth panel can request a device through Web Bluetooth where the browser/device supports it. It can display battery, model, hardware, or firmware information only when the headset exposes those standard GATT services. Most Bluetooth hands-free devices do not expose all these fields to web apps; use Android Bluetooth settings for full headset details.
