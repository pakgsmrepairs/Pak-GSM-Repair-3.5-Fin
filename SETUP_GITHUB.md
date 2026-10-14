# Pak GSM Repair 5.4 — GitHub Pages

1. Extract this ZIP on your phone/computer.
2. Upload the **files inside** the extracted folder to the repository root. `index.html`, `update.json`, `sw.js`, `manifest.json`, `cloud-config.js`, and `icons/` must sit together at the root (do not upload only the ZIP or the enclosing folder).
3. Commit the files and wait until GitHub Pages finishes publishing.
4. **Bootstrap recovery for old 4.4.7 installs:** do not rely on the old in-app updater for this one-time fix. Open the published HTTPS URL in Chrome, wait 5–10 seconds for the new service worker to install/activate, close every tab/window for this site and the installed PWA, then reopen it. This worker calls `skipWaiting()`, claims clients, and removes stale app/staging caches. Customer data in `localStorage` is not deleted. If it still shows 4.4.7, use Chrome site settings to clear only the site cache/storage only after exporting a backup; this last resort can remove local data.
5. For later app releases, increment `APP_VERSION` in `index.html` and `version` in `update.json` together. Keep `sw.js` unchanged for routine app-content releases so it can keep serving the last approved release; only change the service worker when its behavior genuinely needs a new migration.
6. Test with dummy data first. This app stores records locally unless the cloud backend is configured separately.

`Code.gs` is not included in this public package. Keep backend code inside your private Google Apps Script project; do not upload it to public GitHub Pages.


### Dashboard and welcome notification
Replace the existing `index.html` with the supplied 5.3 file. The Dashboard tab is now the first screen after login, and successful login displays a welcome notification. Upload `update.json` and `sw.js` too so Pages serves the matching version and cache updates.
