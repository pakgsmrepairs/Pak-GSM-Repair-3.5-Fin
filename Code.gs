/**
 * Pak GSM Repair 3.7 - Google Apps Script backend
 * IMPORTANT: shared-key auth is suitable for testing only, not production customer data.
 * Script Properties required: CLOUD_KEY
 */
const APP_NAME = 'Pak GSM Repair';
const APP_VERSION = '4.4.9';
const BACKUP_FOLDER = 'Pak GSM Repair 4.4.9 Backups';
const MAX_BODY = 8 * 1024 * 1024;
const OWNER_EMAIL = 'pakgsmrepairs@gmail.com';

function json_(data) {
  return ContentService.createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}
function safe_(value) {
  return String(value || '').replace(/[^a-zA-Z0-9._-]/g, '_').slice(0, 100);
}
function key_() {
  return PropertiesService.getScriptProperties().getProperty('CLOUD_KEY') || '';
}
function auth_(candidate) {
  const configured = key_();
  return !!configured && typeof candidate === 'string' && candidate === configured;
}
function folder_() {
  const props = PropertiesService.getScriptProperties();
  const savedId = props.getProperty('BACKUP_FOLDER_ID');
  if (savedId) {
    try { return DriveApp.getFolderById(savedId); } catch (err) { /* recreate below */ }
  }
  const matches = DriveApp.getFoldersByName(BACKUP_FOLDER);
  const folder = matches.hasNext() ? matches.next() : DriveApp.createFolder(BACKUP_FOLDER);
  props.setProperty('BACKUP_FOLDER_ID', folder.getId());
  return folder;
}
function fileFor_(name) {
  const files = folder_().getFilesByName(name);
  return files.hasNext() ? files.next() : null;
}
function readJsonFile_(name, fallback) {
  const file = fileFor_(name);
  if (!file) return fallback;
  return JSON.parse(file.getBlob().getDataAsString());
}
function writeJsonFile_(name, value) {
  const body = JSON.stringify(value, null, 2);
  let file = fileFor_(name);
  if (file) file.setContent(body);
  else file = folder_().createFile(name, body, MimeType.PLAIN_TEXT);
  return file;
}

function doGet(e) {
  const params = (e && e.parameter) || {};
  const callback = String(params.callback || '').replace(/[^a-zA-Z0-9_.$]/g, '');
  const action = String(params.action || 'health');
  let result;
  try {
    if (action === 'health') {
      result = { ok: true, app: APP_NAME, version: APP_VERSION, message: 'Backend online.' };
    } else if (action === 'diagnostics') {
      result = otpDiagnostics_();
    } else if (action === 'sendOtp') {
      result = sendEmailOtp_(params.email);
    } else if (action === 'verifyOtp') {
      result = verifyEmailOtp_(params.email, params.otp);
    } else if (action === 'checkEmail') {
      result = checkEmailAvailability_(params.email);
    } else if (action === 'registerAccount') {
      result = registerAccount_(params.email, params.name, params.phone, params.passwordHash);
    } else if (action === 'loginAccount') {
      result = loginAccount_(params.email, params.passwordHash);
    } else if (action === 'ping') {
      result = auth_(params.key)
        ? { ok: true, app: APP_NAME, version: APP_VERSION, service: 'cloud-backup' }
        : { ok: false, error: 'Unauthorized: check CLOUD_KEY and app settings.' };
    } else if (action === 'restore' || action === 'records') {
      if (!auth_(params.key)) {
        result = { ok: false, error: 'Unauthorized' };
      } else {
        const deviceId = safe_(params.deviceId);
        if (!deviceId) result = { ok: false, error: 'Missing device ID' };
        else {
          const payload = readJsonFile_('device_' + deviceId + '.json', null);
          result = payload
            ? { ok: true, payload: payload, version: APP_VERSION }
            : { ok: false, error: 'No cloud backup found for this device.' };
        }
      }
    } else if (action === 'products') {
      if (!auth_(params.key)) result = { ok: false, error: 'Unauthorized' };
      else result = { ok: true, products: readJsonFile_('products.json', []) };
    } else {
      result = { ok: false, error: 'Unknown action' };
    }
  } catch (err) {
    result = { ok: false, error: String(err && err.message || err) };
  }
  if (callback) {
    return ContentService.createTextOutput(callback + '(' + JSON.stringify(result) + ');')
      .setMimeType(ContentService.MimeType.JAVASCRIPT);
  }
  return json_(result);
}

function doPost(e) {
  try {
    const raw = e && e.postData && e.postData.contents;
    if (!raw) return json_({ ok: false, error: 'Empty request' });
    if (raw.length > MAX_BODY) return json_({ ok: false, error: 'Request too large' });
    const body = JSON.parse(raw);

    // Owner identity verification is available when Google Identity Services is configured.
    // It is intentionally separate from the shared-key backup endpoints used by this starter UI.
    if (body.action === 'verifyOwner') {
      const owner = verifyOwnerToken_(body.idToken);
      return owner.ok
        ? json_({ ok: true, authorized: true, email: owner.email })
        : json_({ ok: false, error: 'Unauthorized' });
    }
    if (!auth_(body.key)) return json_({ ok: false, error: 'Unauthorized' });

    // Save the app's complete customer/job/inventory data as one device-scoped backup.
    if (body.action === 'backup' || body.action === 'records') {
      const deviceId = safe_(body.deviceId);
      const payload = body.payload;
      if (!deviceId || !payload || typeof payload !== 'object') {
        return json_({ ok: false, error: 'Missing deviceId or payload' });
      }
      if (!Array.isArray(payload.customers) || !Array.isArray(payload.jobs) || !Array.isArray(payload.inventory)) {
        return json_({ ok: false, error: 'Invalid records payload: customers, jobs and inventory arrays are required.' });
      }
      const file = writeJsonFile_('device_' + deviceId + '.json', {
        app: APP_NAME,
        version: APP_VERSION,
        savedAt: new Date().toISOString(),
        customers: payload.customers,
        jobs: payload.jobs,
        inventory: payload.inventory
      });
      return json_({ ok: true, savedAt: file.getLastUpdated().toISOString(), deviceId: deviceId });
    }

    if (body.action === 'products') {
      if (!Array.isArray(body.products)) return json_({ ok: false, error: 'Invalid products' });
      const products = body.products.slice(0, 500).filter(p => p && p.name).map(p => ({
        id: safe_(p.id),
        name: String(p.name).slice(0, 160),
        sku: String(p.sku || '').slice(0, 80),
        price: Math.max(0, Number(p.price) || 0),
        description: String(p.description || '').slice(0, 1000)
      }));
      writeJsonFile_('products.json', products);
      return json_({ ok: true, count: products.length });
    }
    return json_({ ok: false, error: 'Unknown action' });
  } catch (err) {
    return json_({ ok: false, error: String(err && err.message || err) });
  }
}


function verifyOwnerToken_(idToken) {
  if (!idToken || typeof idToken !== 'string') return { ok: false };
  const props = PropertiesService.getScriptProperties();
  const clientId = props.getProperty('GOOGLE_CLIENT_ID');
  const ownerEmail = String(props.getProperty('OWNER_EMAIL') || OWNER_EMAIL).trim().toLowerCase();
  if (!clientId || !ownerEmail) return { ok: false };
  try {
    const response = UrlFetchApp.fetch(
      'https://oauth2.googleapis.com/tokeninfo?id_token=' + encodeURIComponent(idToken),
      { method: 'get', muteHttpExceptions: true }
    );
    if (response.getResponseCode() !== 200) return { ok: false };
    const claims = JSON.parse(response.getContentText());
    const email = String(claims.email || '').toLowerCase();
    const audience = String(claims.aud || '');
    const expires = Number(claims.exp || 0);
    const now = Math.floor(Date.now() / 1000);
    if (audience !== clientId || email !== ownerEmail || String(claims.email_verified) !== 'true' || !expires || expires <= now) {
      return { ok: false };
    }
    return { ok: true, email: email };
  } catch (err) {
    return { ok: false };
  }
}

function setupBackupFolder() {
  const folder = folder_();
  Logger.log('Backup folder ready: ' + folder.getUrl());
}


// Email OTP for login. Requires Gmail/Mail permissions the first time this script sends mail.
function otpEmailKey_(email) {
  const bytes = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, String(email).toLowerCase().trim(), Utilities.Charset.UTF_8);
  return bytes.map(b => ('0' + ((b + 256) % 256).toString(16)).slice(-2)).join('');
}
function otpCodeHash_(email, otp) {
  const bytes = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, String(email).toLowerCase().trim() + ':' + String(otp) + ':' + ScriptApp.getScriptId(), Utilities.Charset.UTF_8);
  return bytes.map(b => ('0' + ((b + 256) % 256).toString(16)).slice(-2)).join('');
}
function sendEmailOtp_(email) {
  email = String(email || '').trim().toLowerCase();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return { ok: false, error: 'Enter a valid email address.' };
  const cache = CacheService.getScriptCache();
  const key = otpEmailKey_(email);
  if (cache.get('otp-rate-' + key)) return { ok: false, error: 'Please wait one minute before requesting another OTP.' };
  const code = String(Math.floor(100000 + Math.random() * 900000));
  cache.put('otp-code-' + key, JSON.stringify({ hash: otpCodeHash_(email, code), attempts: 0 }), 300);
  cache.put('otp-rate-' + key, '1', 60);
  try {
    MailApp.sendEmail({
      to: email,
      subject: 'Your Pak GSM Repair verification code',
      body: 'Your Pak GSM Repair verification code is: ' + code + '\n\nThis code expires in 5 minutes. If you did not request it, you can ignore this email.'
    });
    return { ok: true, message: 'OTP sent. It expires in 5 minutes.' };
  } catch (err) {
    cache.remove('otp-code-' + key);
    return { ok: false, error: 'Could not send email. Authorize MailApp in Apps Script and try again.' };
  }
}
function verifyEmailOtp_(email, otp) {
  email = String(email || '').trim().toLowerCase();
  otp = String(otp || '').trim();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) || !/^\d{6}$/.test(otp)) return { ok: false, verified: false, error: 'Enter a valid email and 6-digit OTP.' };
  const cache = CacheService.getScriptCache();
  const key = otpEmailKey_(email);
  const raw = cache.get('otp-code-' + key);
  if (!raw) return { ok: false, verified: false, error: 'OTP expired or not requested. Request a new code.' };
  const saved = JSON.parse(raw);
  if (saved.hash !== otpCodeHash_(email, otp)) {
    saved.attempts = Number(saved.attempts || 0) + 1;
    if (saved.attempts >= 5) cache.remove('otp-code-' + key);
    else cache.put('otp-code-' + key, JSON.stringify(saved), 300);
    return { ok: false, verified: false, error: saved.attempts >= 5 ? 'Too many incorrect attempts. Request a new OTP.' : 'Incorrect OTP. Please check the code and try again.' };
  }
  cache.remove('otp-code-' + key);
  // Allow the immediately following account-registration request only after OTP verification.
  // The registration endpoint consumes this short-lived server-side proof.
  cache.put('otp-verified-' + key, '1', 300);
  return { ok: true, verified: true, email: email };
}


// Account registration: store account records in Drive, never plaintext passwords.
function accountFileKey_(email) {
  const bytes = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, String(email || '').trim().toLowerCase(), Utilities.Charset.UTF_8);
  return bytes.map(b => ('0' + ((b + 256) % 256).toString(16)).slice(-2)).join('');
}
function accountPasswordHash_(clientHash, salt) {
  const bytes = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, String(clientHash || '') + ':' + String(salt || '') + ':' + ScriptApp.getScriptId(), Utilities.Charset.UTF_8);
  return bytes.map(b => ('0' + ((b + 256) % 256).toString(16)).slice(-2)).join('');
}
function accountRecord_(email) { return readJsonFile_('account_' + accountFileKey_(email) + '.json', null); }

// Email availability check used by the registration screen.
function checkEmailAvailability_(email) {
  email = String(email || '').trim().toLowerCase();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    return { ok: false, error: 'Enter a valid email address.' };
  }
  const exists = !!accountRecord_(email);
  return { ok: true, email: email, exists: exists, registered: exists, available: !exists };
}

function registerAccount_(email, name, phone, passwordHash) {
  email=String(email||'').trim().toLowerCase(); name=String(name||'').trim().slice(0,100); phone=String(phone||'').trim().slice(0,25); passwordHash=String(passwordHash||'').trim();
  if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))return {ok:false,error:'Enter a valid email address.'};
  if(!name||!phone)return {ok:false,error:'Full name and mobile number are required.'};
  if(!/^[a-f0-9]{64}$/i.test(passwordHash))return {ok:false,error:'Invalid password format. Please try again.'};
  const verifiedKey='otp-verified-'+otpEmailKey_(email);
  const cache=CacheService.getScriptCache();
  if(cache.get(verifiedKey)!=='1')return {ok:false,error:'Verify the email OTP before registering this account.'};
  cache.remove(verifiedKey);
  const lock=LockService.getScriptLock();lock.waitLock(10000);
  try { if(accountRecord_(email))return {ok:false,error:'An account already exists for this email. Please use Login.'};
    const salt=Utilities.getUuid()+Utilities.getUuid();
    const rec={email:email,name:name,phone:phone,salt:salt,passwordHash:accountPasswordHash_(passwordHash,salt),createdAt:new Date().toISOString(),verified:true};
    writeJsonFile_('account_'+accountFileKey_(email)+'.json',rec);return {ok:true,email:email,name:name,message:'Account registered.'};
  } finally {lock.releaseLock();}
}
function loginAccount_(email, passwordHash) {
  email = String(email || '').trim().toLowerCase();
  passwordHash = String(passwordHash || '').trim();

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    return { ok: false, code: 'INVALID_EMAIL', error: 'Please enter a valid email address.' };
  }
  if (!/^[a-f0-9]{64}$/i.test(passwordHash)) {
    return { ok: false, code: 'INVALID_PASSWORD_FORMAT', error: 'Please enter your password correctly.' };
  }

  const rec = accountRecord_(email);
  if (!rec || !rec.salt || !rec.passwordHash) {
    return { ok: false, code: 'EMAIL_NOT_REGISTERED', error: 'No account is registered with this email address.' };
  }
  if (accountPasswordHash_(passwordHash, rec.salt) !== rec.passwordHash) {
    return { ok: false, code: 'WRONG_PASSWORD', error: 'Wrong password. Please try again.' };
  }
  return { ok: true, email: rec.email, name: rec.name, phone: rec.phone };
}

/**
 * Manual email delivery test. Run from the Apps Script editor.
 * This sends a TEST message only; it does not issue an OTP.
 */
function testSendEmail() {
  const recipient = 'pakgsmrepairs@gmail.com';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(recipient)) {
    throw new Error('Please set a valid recipient email address in testSendEmail().');
  }
  const remainingBefore = MailApp.getRemainingDailyQuota();
  Logger.log('Test recipient: ' + recipient);
  Logger.log('Quota before test: ' + remainingBefore);
  if (remainingBefore < 1) {
    throw new Error('Daily email recipient quota exhausted. Try again after reset.');
  }
  MailApp.sendEmail({
    to: recipient,
    subject: 'Pak GSM Repair 4.4.9 - Email Delivery Test',
    body: 'Pak GSM Repair Email Test\n\n' +
      'This is a test email from your Google Apps Script server.\n' +
      'If you received this message, the email sending request was accepted.\n\n' +
      'This is a test email, not an OTP.',
    name: 'Pak GSM Repair'
  });
  Logger.log('MailApp.sendEmail completed without an error.');
  Logger.log('Recipient: ' + recipient);
  Logger.log('Quota after test: ' + MailApp.getRemainingDailyQuota());
}

/** Returns email quota diagnostics without sending a message. */
function otpDiagnostics_() {
  return {
    ok: true,
    app: APP_NAME,
    version: APP_VERSION,
    remainingDailyEmailRecipients: MailApp.getRemainingDailyQuota()
  };
}
