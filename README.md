# Pak GSM Repair 5.4

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
