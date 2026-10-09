# Pak GSM Repair 3.7 — Google Apps Script setup

1. Visit https://script.google.com/ and create a project named `Pak GSM Repair 3.7 Backend`.
2. Replace the editor's default code with this ZIP's `Code.gs` and save.
3. Open **Project Settings → Script properties** and add:
   - `CLOUD_KEY` = a long random key used by the current app's cloud backup UI.
   - `OWNER_EMAIL` = `pakgsmrepairs@gmail.com` (or your authorized owner email).
   - `GOOGLE_CLIENT_ID` = your Google Identity Services OAuth client ID if you plan to use the `verifyOwner` endpoint.
4. Run `setupBackupFolder` once in the Apps Script editor and approve the requested Drive permission.
5. Choose **Deploy → New deployment → Web app**. Execute as **Me**. For this basic external web app, set access to **Anyone** only if you understand the risks and need the public endpoint; then deploy and copy the URL ending in `/exec`.
6. In the app, open **Backup & settings**, enter the `/exec` URL and `CLOUD_KEY`, save settings, and test the connection. Try cloud backup and restore using dummy data first.

## API actions
- `GET ?action=health` — public health check.
- `GET ?action=ping&key=...&callback=...` — key-authenticated connection test; JSONP callback is used by the static web app.
- `POST` with `{ action: "backup", key, deviceId, payload: { customers: [], jobs: [], inventory: [] } }` — save a device-scoped backup in Google Drive.
- `GET ?action=restore&key=...&deviceId=...&callback=...` — restore the saved backup using JSONP.
- `POST` with `{ action: "verifyOwner", idToken: "..." }` — validates a Google ID token against `GOOGLE_CLIENT_ID` and the configured owner email.

## Important security notes
The current app UI uses `CLOUD_KEY` for backup and restore. A key entered into a public web app can be copied, so this is a testing scaffold—not production-grade authorization. The `verifyOwner` endpoint is separate and is not yet wired to a Google sign-in button in the UI. Before storing real customer details, integrate Google Identity Services, require a verified ID token for all data endpoints, and enforce authorization server-side. Do not publish keys or use real customer records during initial testing.


## Sign-in troubleshooting
- Do not test Google sign-in by opening index.html directly from File Manager. Publish the files to an HTTPS host first.
- In Google Cloud OAuth Client, add the exact published origin under Authorized JavaScript origins (for example, https://YOURNAME.github.io). Do not use content://, file://, or the Apps Script URL as the site origin.
- This beta shows Sign In and Login tabs, but email/password accounts are not connected to a secure authentication service; only the Google button is intended for HTTPS testing.


## Email OTP login (3.7 Beta)
- The login page calls `PGR_OTP_SERVER_URL` in `cloud-config.js` using JSONP actions `sendOtp` and `verifyOtp`.
- The bundled `Code.gs` implements email OTP delivery through `MailApp`; the first send may require Google authorization. OTP expires in 5 minutes and is rate-limited per email.
- The existing health response only proves the web app is online; it does not prove that OTP actions exist. If tapping **Get OTP** returns “Unknown action” or a similar error, open the Apps Script project behind the OTP URL, replace its code with this ZIP's `Code.gs`, authorize mail sending, then deploy a new version of the web app. Keep the new `/exec` URL in `PGR_OTP_SERVER_URL`.
- SMS OTP is not implemented in this build; the OTP delivered by this version is email only.
- Audio is attempted automatically on the login page. Some Android browsers block audio until the first tap; in that case, the first interaction starts the voice/music without a separate Play button.


## Email OTP login flow
The Sign In tab validates the name and matching password/confirm-password fields, requests an email OTP, then reveals a dedicated 6-digit verification step. Login requests OTP after email/password fields are filled. The deployed Apps Script URL must implement `sendOtp` and `verifyOtp`, and MailApp must be authorized. OTP is the real email verification gate in this beta; passwords are not yet stored/validated by a server-side user-account database.


## Version 3.7 email OTP deployment checklist

- The bundled `Code.gs` includes the cloud-backup API and email OTP actions (`sendOtp` and `verifyOtp`). It also includes `testSendEmail()` for a manual delivery test and a read-only `?action=diagnostics` endpoint.
- To test delivery, open Apps Script, select `testSendEmail`, and click **Run**. Confirm the message arrives in `pakgsmrepairs@gmail.com` (check Spam/Junk too). This test message is not an OTP.
- To activate changed server code, save it, then choose **Deploy → Manage deployments → Edit → New version → Deploy**. If updating the existing OTP script project, keep its `/exec` URL in `PGR_OTP_SERVER_URL`. The cloud-backup URL in `PGR_CLOUD_BACKUP_URL` remains a separate setting unless you intentionally reconfigure it.
- After deployment, test the OTP flow from the published HTTPS app: request an OTP, check email, enter the 6-digit code before it expires, and verify that login proceeds.
