Pak GSM Repair 5.6 — upload instructions

1. This ZIP is the Android source project for the APK build workflow. Do not merge it with another ZIP.
2. Keep the uploaded ZIP filename exactly:
   Pak_GSM_Repair_5.5_Native_Bluetooth_Android_Source.zip
   The filename is retained to match the existing GitHub Actions workflow trigger; the app inside is version 5.6.
3. After upload, run the Android APK workflow manually from GitHub Actions.
4. The github-pages-update folder inside this ZIP contains separate web/PWA files. Extract that folder and upload/replace its contents in the repository root (icons files belong inside root/icons). Commit them together so update.json and index.html are from the same version.
5. Build success is not proof of device behavior. Test login/OTP, Bluetooth discovery/selection, and update on the phone.
6. Registration UI sends username/shopName to the Apps Script endpoint, but server-side persistence requires the changes in AppsScript_Profile_Fields_Patch_URDU.txt and redeploying the Apps Script web app.
7. Shop cover photo is local-only in this build. Server/Drive photo sync is not implemented.
