# Pak GSM Repair 3.7 Beta — Login Fix

A new standalone starter project, separate from the old repository.

## Files
- `index.html`: responsive dashboard, customer records, repair jobs, inventory, local JSON export/import
- `cloud-config.js`: backend URL placeholder
- `manifest.json` and `sw.js`: basic PWA metadata/offline shell
- `Code.gs`: Apps Script API for health/ping, owner-token verification, and customer/job/inventory backup/restore in Drive
- `SETUP_GITHUB.md`: GitHub Pages instructions
- `SETUP_GOOGLE.md`: Apps Script instructions

## Important limitations
This is a fresh starter, not a verified migration of every feature in the previous app. Local browser storage remains the app’s primary save location. Cloud backup/restore is optional and must be configured and tested after deployment. Google sign-in requires a published HTTPS site whose origin is listed in Google OAuth Authorized JavaScript origins. Opening index.html directly from Android File Manager (content:// or file://) will not complete Google sign-in. Email/password buttons are UI-only until a secure account service is connected. Shared-key cloud backup is not production-grade. Use dummy records until Google sign-in and server-side authorization are fully wired into all data endpoints. Do not upload secrets to a public repository.


This revision keeps the existing login card and adds email OTP login, Guest View, and a login-page welcome voice/music attempt. See SETUP_GOOGLE.md for OTP server deployment requirements.


Login flow correction: Sign In restores Full name, Email, Create password, Confirm password and Get Email OTP. After the OTP request succeeds, a separate 6-digit verification step appears. Login has Email, Password and Get Email OTP. Email OTP must be supported by the deployed Apps Script endpoint. This beta still does not store or validate passwords against a server-side account database; email OTP is the actual verification gate.


## OTP login fix (v2)
- The OTP request now requires only a valid email address. Name and password fields are optional because this beta does not authenticate passwords against an account database.
- After the OTP server returns `ok: true`, the 6-digit OTP panel is shown.
- The configured OTP server URL is in `cloud-config.js`.
- For testing from a phone, serve this folder from an HTTPS static host rather than opening the HTML as a local file.
- This ZIP's login flow was syntax-checked; a live end-to-end email test still must be performed in the hosted page.
