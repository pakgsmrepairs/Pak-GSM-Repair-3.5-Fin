# Pak GSM Repair 5.1 — Google Apps Script

Install `Code.gs.txt` as the Apps Script backend. The current backend keeps the existing OTP, account registration/login, and shared-key cloud backup endpoints.

Required script properties for the existing cloud backup flow include `CLOUD_KEY`; `OWNER_EMAIL` and `GOOGLE_CLIENT_ID` are used by the owner verification endpoint. Deploy as a Web App and use the `/exec` URL in `cloud-config.js`.

Important: the existing shared-key cloud backup is a testing scaffold, not production-grade per-user authorization. Test with dummy data before storing real customer records.
