# Pak GSM Repair 4.4 — Splash and Update Fix

This package aligns the frontend/PWA metadata to version 4.4 and adds a splash-screen fail-safe so the login page appears even if later application code fails.

## Publish on GitHub Pages
1. Extract the ZIP.
2. Upload the files inside the extracted folder to the same repository branch/folder used by GitHub Pages; `index.html` must be at the repository root.
3. Include `update.json`, `manifest.json`, `sw.js`, and `cloud-config.js`.
4. Commit the changes and wait for Pages to finish publishing.
5. Open the live URL in Chrome Incognito first.

## Scope
The Apps Script backend file (`Code.gs.txt`) is included unchanged. The cloud backup and OTP endpoint URLs and Google client ID in `cloud-config.js` are retained. This package does not prove that the live GitHub Pages deployment has changed; test the actual published URL after commit.
