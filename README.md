# Pak GSM Repair Pro 4.4.7

Mobile repair-shop workspace with customer records, repair jobs, File Vault, Parts Availability, settings, local backup/restore, and a user-triggered GitHub Pages update flow.

## GitHub Pages setup

See `SETUP_GITHUB.md`. Upload the extracted **contents** to the repository root, not a nested folder. `update.json` must be published beside `index.html`.

## Manual update flow

Open **Settings → App Update → Check for Updates**. If `update.json` advertises a higher version, tap **Update App**. The app downloads and verifies the app shell/assets into a staging cache, then restarts to the approved release. The app shell is cache-first after this updater build is installed, so a normal GitHub commit is not shown as an update until the user approves it.

For each future app release, update the version in `index.html` (`APP_VERSION`) and `update.json` together. Keep `sw.js` unchanged for routine content-only releases; service-worker behavior changes require careful migration testing.

## Notes

- Run on the HTTPS GitHub Pages URL for service-worker and Google sign-in support.
- Customer data is local to the browser unless a cloud backup backend is configured.
- `Code.gs.txt` is a separate Google Apps Script deployment artifact.
