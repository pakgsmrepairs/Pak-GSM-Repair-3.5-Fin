# Pak GSM Repair 4.4

Green 4.4 frontend package for GitHub Pages. Includes File Vault on the first page, IMEI 1/2, Parcel repair status with TCS/Leopards/GPO, settings update controls, backup/restore UI, and login OTP/Google flows.

`Code.gs.txt` is a Google Apps Script backend source file and is not executed by GitHub Pages. Install it separately in Apps Script.

Before using real customer data, test login, backup, restore, and update behavior with dummy data. The current backend retains the existing shared-key cloud backup API; it is not a production-grade per-user authorization system.


## Splash/Login fix package
This build includes an independent splash watchdog and a bumped service-worker cache version. Extract the ZIP and upload/replace its contents in the GitHub Pages repository root (keep `index.html`, `sw.js`, `manifest.json`, `update.json`, `cloud-config.js`, and the `icons/` folder together). Uploading the ZIP file itself as a single repository file will not publish the app.

## Version 4.4.1 patch

- Makes the login/register initialization idempotent so the splash timer and fallback watchdog cannot initialize the form twice.
- Routes the watchdog through the same login-screen reveal path when the main script is available.
- Refreshes the service-worker cache name so browsers fetch the updated login code.
- Tunes the authentication panel for smaller Android screens.

Note: the ZIP is a static web/PWA source package, not a compiled APK. The email OTP and Google login flows still require the configured backend and authorized HTTPS origin.


## Version 4.4.2 preview fixes
- Fixed customer IMEI 1/2 being read from the wrong fields before saving.
- Added camera capture and gallery image attachment to customer/repair records; images are resized/compressed before local storage.
- Cleaned duplicate Ready status and clarified Leopards Courier.
- Bumped service-worker cache and release metadata.
- Browser preview can be opened by serving this folder over HTTP; real email OTP, Google Sign-In, and cloud backup still require valid backend configuration and an authorized HTTPS domain.
