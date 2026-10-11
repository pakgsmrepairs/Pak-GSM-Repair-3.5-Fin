# Pak GSM Repair 4.4

Green 4.4 frontend package for GitHub Pages. Includes File Vault on the first page, IMEI 1/2, Parcel repair status with TCS/Leopards/GPO, settings update controls, backup/restore UI, and login OTP/Google flows.

`Code.gs.txt` is a Google Apps Script backend source file and is not executed by GitHub Pages. Install it separately in Apps Script.

Before using real customer data, test login, backup, restore, and update behavior with dummy data. The current backend retains the existing shared-key cloud backup API; it is not a production-grade per-user authorization system.


## Splash/Login fix package
This build includes an independent splash watchdog and a bumped service-worker cache version. Extract the ZIP and upload/replace its contents in the GitHub Pages repository root (keep `index.html`, `sw.js`, `manifest.json`, `update.json`, `cloud-config.js`, and the `icons/` folder together). Uploading the ZIP file itself as a single repository file will not publish the app.
