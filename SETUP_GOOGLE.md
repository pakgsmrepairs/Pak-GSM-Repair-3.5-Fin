# Pak GSM Repair 5.4 — Google Apps Script Backend

**This ZIP does not contain `Code.gs`.** It is a public frontend package for GitHub Pages. Do not upload private backend code into the public GitHub repository.

If your existing Apps Script web app is already working for email OTP, account registration/login, and cloud backup, keep using that deployed backend; this dashboard update does not by itself require replacing server code.

If you need to change or restore the backend, open your private Google Apps Script project and verify the current `Code.gs` there before editing or deploying. Do not replace a working backend with an older 3.7 file without checking endpoint compatibility.

The frontend reads the configured Web App URL from `cloud-config.js`. The deployed script must support the actions the frontend uses. Test with dummy data before using real customer records.
