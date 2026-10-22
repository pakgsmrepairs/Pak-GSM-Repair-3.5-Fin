# Pak GSM Repair — Android APK updater setup

The in-app updater downloads the APK from the GitHub Release, verifies its package name, version, version code and signing certificate, then opens Android's installer. Android still requires the user to confirm installation.

## Configure a permanent signing key once

Do not use the temporary APK from the GitHub Actions debug artifact as the long-term update channel. CI debug APKs may be signed with a different temporary key on each run.

On a trusted PC with Java's `keytool` available, create a release keystore and keep a secure offline backup:

```powershell
keytool -genkeypair -v -keystore pakgsm-release.jks -alias pakgsm -keyalg RSA -keysize 2048 -validity 10000
```

Convert the keystore to Base64 in PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("pakgsm-release.jks"))
```

In GitHub, open **Settings → Secrets and variables → Actions → New repository secret** and add:

- `ANDROID_KEYSTORE_BASE64` — the complete Base64 output
- `ANDROID_KEYSTORE_PASSWORD` — the keystore password
- `ANDROID_KEY_ALIAS` — `pakgsm` (or the alias you chose)
- `ANDROID_KEY_PASSWORD` — the key password

Never commit the keystore or passwords to the repository. Keep a secure backup of the keystore; losing it prevents compatible future updates.

## Publish the first stable APK

After the four repository secrets are configured, open **Actions → Build Pak GSM Repair 9.0 Android APK → Run workflow**. A successful signed build publishes `Pak-GSM-Repair.apk` to the GitHub Release. The in-app updater's stable URL is:

`https://github.com/pakgsmrepairs/Pak-GSM-Repair-3.5-Fin/releases/latest/download/Pak-GSM-Repair.apk`

## Important migration warning

Android permits an in-place update only when the new APK uses the same signing certificate as the installed app and has a higher version code. Earlier debug APKs built on temporary CI runners may have a different certificate. If the app reports a signing-key mismatch, do not repeatedly retry installation. First back up customer/repair records using the app's available backup/export options; a one-time reinstall may be required. After the stable signed APK is installed, keep using the same keystore for every later release.

For future releases, increment both `versionName` and `versionCode`, update the release tag/name in `.github/workflows/build-apk.yml`, and keep `apkVersion` in all `update.json` copies synchronized with the APK.
