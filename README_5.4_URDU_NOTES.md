# Pak GSM Repair 5.4 — Dashboard Mockup / Option B

## Included
- `index.html` — updated app with the sketch-matched dashboard and Option B Bluetooth diagnostics.
- `Pak_GSM_Repair_5.4_Dashboard_Mockup.png` — visual mockup/sketch reference.
- `manifest.json`, `sw.js`, `update.json`, `cloud-config.js`, icons and setup notes.

## Important Bluetooth note
The ANC, Transparency and Voice Assistant tiles are support-check shortcuts, not fake ON/OFF controls. Web Bluetooth can only access services exposed by the headset; Pro2-specific ANC control has not been verified. The browser/system Bluetooth chooser is still required.

## Publish
1. Back up your current GitHub repository first.
2. Upload the package's public app files into the repository root, replacing the old `index.html`, `manifest.json`, `sw.js` and `update.json`.
3. Do not upload private Apps Script server code to a public GitHub repository.
4. Wait for GitHub Pages to redeploy, then open the site and hard-refresh/clear the installed PWA cache if it still shows the previous version.

This is a packaged frontend update; it has not been pushed to GitHub or tested against the physical Pro2 headset.


## Recheck note
- `cloud-config.js` app version metadata is set to 5.4.
- `Code.gs` is not included intentionally: keep server-side code in Apps Script, not the public GitHub repository.
- `Pak_GSM_Repair_5.4_Dashboard_Mockup.png` is a design reference image; upload it to the repository root only if you want to keep the mockup file online. The app does not need it to run.
