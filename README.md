# Pak GSM Repair 4.3

This release is based on the Pak GSM Repair 4.2 project files.

## Changes in 4.3

- Replaced the shared Apps Script backup UI with personal Google Drive backup and restore.
- A signed-in account must explicitly authorize Google Drive; the app checks that the Google email matches the signed-in account email.
- Backups use the restricted `drive.file` scope and are stored in a per-account JSON file in that account's Drive.
- Google access tokens are kept in memory only and are not written to local storage.
- This feature requires the Google Drive API to be enabled and the OAuth consent/client configured for the published HTTPS origin. It is not live-verified in this package.

- Added account-scoped local records. Each signed-in email uses its own local data key; Guest View has a separate database.
- Added account-specific cloud-backup device IDs so newly created backups for different signed-in emails do not reuse the same device ID.
- Added automatic update *checking* at app opening/sign-in. The app never applies an update automatically; the user must choose **Update Now**.
- Added a manual service-worker apply flow. The waiting worker downloads and validates the app shell after the user presses Update Now, then activates and reloads the app.
- Changed the service worker to serve the active cached HTML until the user approves a waiting update.
- Moved access to Settings to the corner gear and removed the Backup & Settings tab from the main workspace. Update and Restore controls remain inside Settings.
- Aligned the visible version, manifest, cloud config, and `update.json` to 4.3 / versionCode 403.

## Publish the whole release together

Upload **all** of these files to the same GitHub Pages root folder in one commit:

- `index.html`
- `sw.js`
- `manifest.json`
- `cloud-config.js`
- `update.json`
- `README.md`
- `SETUP_GITHUB.md`
- `SETUP_GOOGLE.md`
- `Code.gs.txt` (reference copy of the OTP Apps Script; it is not automatically deployed)
- `icons/` (PWA icons)

Do not upload only `update.json`: it is metadata, not an app build. For future releases, update the version in `index.html`, change the cache name in `sw.js`, update `manifest.json` / `cloud-config.js`, and publish a matching `update.json` with all files together.

## Account-data behavior and migration

On the first real account that opens 4.3 on a browser containing the old shared `pgr36_data` database, that existing database is migrated to that first account so its records are not immediately lost. The old shared key is then removed. Other email accounts and Guest View get separate databases. This is a local-browser migration; it cannot determine which account originally created records in the old shared database. Before using different accounts, export a backup of important data.

Cloud backup now uses a different device ID for each email account on that browser. Existing cloud backups made with the old shared device ID are not automatically restored into each account, because doing so could expose another account's records.

**Security note:** local storage separation prevents accidental cross-account display on the same browser, but it is not a substitute for server-side authorization. The cloud Apps Script must verify the user/account on every backup and restore request to provide strong multi-user security. Do not publish a Cloud Key or customer data in a public repository.

## Manual update behavior

The app checks `update.json` at startup/sign-in and displays a prompt if the published version is newer. It only downloads the new app shell and activates the waiting service worker after the user presses **Update Now**. This requires the published HTTPS site and service-worker support; opening `index.html` as a local file in a file manager cannot install a PWA update.

The currently deployed 4.1 service worker used network-first HTML fetching and `skipWaiting()`. A static web app cannot retroactively change that old worker before the first 4.2 load, so the strict manual-approval behavior is guaranteed for subsequent releases after 4.2 is installed. Test this transition on a non-critical browser/device first.

## Test checklist

1. Open the GitHub Pages HTTPS URL, sign in as account A, and create a dummy customer.
2. Sign out, sign in as account B, and confirm account A's dummy customer is not shown.
3. Sign out and sign in as account A again; confirm its dummy customer returns.
4. Check Settings → Backup & Restore using dummy records before restoring any real backup.
5. Test an update on a second browser/device before relying on it for all customers.


## Personal Google Drive backup setup (4.3)

1. In the Google Cloud project that owns the Client ID in `cloud-config.js`, enable **Google Drive API**.
2. Configure the OAuth client for the exact published HTTPS origin (for example, the GitHub Pages site).
3. Configure the OAuth consent screen and testing users if the app is still in Testing. Google may show an unverified/testing warning depending on project status.
4. Publish the complete 4.3 package to the HTTPS site. A local `file://` preview is not supported for OAuth.
5. Sign in to Pak GSM Repair with a verified email. In Settings → Backup & Restore, press **Connect Google Drive** and approve the requested permissions. The selected Google email must exactly match the email used to sign in to the app.
6. Test with dummy records: press **Back Up Now**, confirm a success message, and check the JSON backup file in that same Google Drive. Then test Restore Latest Backup.

The app uses `openid`, `email`, and `https://www.googleapis.com/auth/drive.file`. Drive files created by this app are stored in the user's own Drive. Existing files from the previous shared Apps Script backup are not automatically migrated. The feature still requires a live test on the published HTTPS site; this ZIP alone cannot prove that the existing OAuth project has the Drive API and consent settings enabled.
