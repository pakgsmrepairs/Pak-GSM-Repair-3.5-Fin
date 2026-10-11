# Pak GSM Repair 4.3 — Google Apps Script and account notes

This package includes `Code.gs.txt` as the supplied Apps Script source. It contains OTP/account actions and shared-key cloud backup actions. It is a text copy for review/pasting; GitHub Pages does not deploy Apps Script code automatically.

## Before changing any live script

1. Open the Apps Script project currently used by the OTP URL in `cloud-config.js`.
2. Make a copy of the live script/project before replacing code.
3. Compare its current code with `Code.gs.txt`; do not overwrite the live project until the existing OTP and backup actions are confirmed.
4. If you deploy a new Apps Script version, copy the deployed `/exec` URL into the appropriate `PGR_OTP_SERVER_URL` or `PGR_CLOUD_BACKUP_URL` setting in `cloud-config.js`. These two settings currently point to different deployments in the supplied configuration; keep them separate unless you deliberately consolidate and test both services.
5. Test health, email OTP, account registration/login, backup and restore with dummy data.

## Important security limitation

The 4.3 frontend uses per-user Google Drive authorization for cloud backup. See the personal Google Drive setup instructions below. Do not put secrets or real customer records in a public GitHub repository.

## Google sign-in

Google sign-in requires the published HTTPS site and an authorized JavaScript origin in Google Cloud. A local `file://` preview will not support the full Google sign-in flow. The current frontend reads the Google credential in the browser; the backend should validate ID tokens before using them to authorize protected data actions.


## 4.3: personal Google Drive backup

The 4.3 frontend no longer uses the shared Apps Script backup endpoint for the Backup & Restore buttons. It asks each signed-in user to authorize their own Google Drive with the Google Identity Services token flow, then stores one JSON backup file in that user's Drive. The account email returned by Google must match the app's signed-in email.

Required before testing:
- Enable Google Drive API in the Google Cloud project for the Client ID.
- Ensure the OAuth client has the published HTTPS origin authorized.
- Configure OAuth consent screen and add testing users if applicable.
- Test on the published HTTPS URL, not a local HTML file.

The OTP server verifies email registration/login, but OTP verification itself does not grant Drive access. The user must approve Drive permission in Google's consent screen. The access token is held only in page memory; reconnect after refreshing the app.
