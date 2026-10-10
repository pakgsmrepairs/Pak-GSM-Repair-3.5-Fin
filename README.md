# Pak GSM Repair 3.7.4 --- Settings, Update & Restore

This README documents the intended frontend package version **3.7.4**.

## Important: README does not publish or change the website

Changing `README.md` only changes the project documentation. It does
**not** change the version served by GitHub Pages. The live website is
built from the published site's source files, especially `index.html`,
and may also be affected by browser/PWA caching.

## Verify the live website

1.  Open the GitHub repository that contains the deployed app.
2.  Open `index.html` and confirm that its title/footer/version text
    says `3.7.4` where appropriate.
3.  Confirm that the latest changes have been committed to the branch
    and folder selected in **Settings → Pages**.
4.  Open the GitHub Pages URL shown in **Settings → Pages**. Do not
    assume the repository name or URL path.
5.  On Android Chrome, open the URL in an Incognito tab to check whether
    an old cached copy is being shown. If Incognito shows the new
    version, clear the site's cached data or refresh the installed PWA.
6.  If the page still shows `3.7.1`, inspect the deployed `index.html`
    and the Pages source/branch. Updating this README will not fix that
    by itself.

## Files to check

-   `index.html` --- app interface and visible version text.
-   `sw.js` --- service worker cache; an old cache can keep older app
    files available.
-   `manifest.json` --- PWA metadata.
-   `update.json` --- update-check metadata, if the app uses it.
-   `cloud-config.js` and `Code.gs` --- backend configuration and
    server-side behavior; updating the frontend does not automatically
    update the backend.

## Release note

Target frontend package: **Pak GSM Repair 3.7.4 --- Settings Update
Restore**.

**Release status:** The version is not verified as live just because
this README says 3.7.4. Verify the published website and its actual
`index.html` before treating the release as deployed.

## Safety

Test login, backup, restore, and updates with dummy data first. Do not
put passwords, cloud keys, customer records, or other secrets in this
public repository.
