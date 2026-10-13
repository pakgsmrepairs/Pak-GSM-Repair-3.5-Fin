# Pak GSM Repair 4.4.7 — GitHub Pages

1. Extract this ZIP on your phone/computer.
2. Upload the **files inside** the extracted folder to the repository root. `index.html`, `update.json`, `sw.js`, `manifest.json`, `cloud-config.js`, and `icons/` must sit together at the root (do not upload only the ZIP or the enclosing folder).
3. Commit the files and wait until GitHub Pages finishes publishing.
4. Open the published HTTPS URL once. Because older 4.4.6 builds used a network-first service worker, the first move to this manual-update-capable build may load the new release once while the service worker is replaced. After this bootstrap, normal releases are held until the user taps **Settings → App Update → Check for Updates → Update App**.
5. For later app releases, increment `APP_VERSION` in `index.html` and `version` in `update.json` together. Keep `sw.js` unchanged for routine app-content releases so it can keep serving the last approved release; only change the service worker when its behavior genuinely needs a new migration.
6. Test with dummy data first. This app stores records locally unless the cloud backend is configured separately.

`Code.gs.txt` is installed separately in Google Apps Script if you use the cloud-backup feature.
