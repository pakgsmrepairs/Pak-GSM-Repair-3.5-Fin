# GitHub Pages deployment — Pak GSM Repair 4.3

1. Open the repository used by your live Pak GSM Repair website.
2. Upload the complete contents of this release folder into the same root folder as `index.html`.
3. Confirm `index.html`, `sw.js`, `manifest.json`, `cloud-config.js`, `update.json`, and the `icons` folder are all present together.
4. Commit all files in one commit so the app and its update metadata are published as one release.
5. Open the repository's Settings → Pages and confirm the correct branch and root folder are selected.
6. Wait for GitHub Pages to finish publishing, then open the HTTPS Pages URL.
7. Test sign-in, account separation, local backup/restore, and update checks using dummy data before using real customer data.

## Important

- `update.json` only announces a version; it does not contain the app build.
- Future versions must update `index.html`, `sw.js` (including its cache name), `manifest.json`, `cloud-config.js`, and `update.json` together.
- Do not upload only `update.json` to test a release. The update button requires a newer service-worker build to be published.
- Use the published HTTPS URL, not a local `file://` preview, to test Google sign-in, service workers, and manual updates.
