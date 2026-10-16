# Pak GSM Repair 5.5 — اصلاح شدہ Android سورس پیکج

یہ Android Studio/Gradle source project ہے، پہلے سے بنی ہوئی APK نہیں۔ اس ورژن میں پچھلی build کی واضح کمیوں کو درست کرنے کی کوشش کی گئی ہے:

- Android launcher icon کے لیے اصل Pak GSM Repair icon شامل کیا گیا ہے۔
- WebView اب `file://` کے بجائے Android `WebViewAssetLoader` کے HTTPS app-assets origin پر HTML کھولتا ہے، تاکہ Apps Script کے HTTPS endpoints کے ساتھ web-style requests بہتر طور پر کام کر سکیں۔
- Bluetooth bridge paired devices کے ساتھ Android discovery کے دوران ملنے والی Classic Bluetooth devices بھی دکھانے کی کوشش کرتا ہے۔
- Android 13+ پر اگر آڈیو ڈیوائس battery level ظاہر کرے اور public Android API اسے واپس دے، تو فیصد دکھایا جائے گا؛ ورنہ صاف طور پر “Not provided” دکھایا جائے گا، کوئی فرضی battery نہیں۔
- موجودہ Google Apps Script URLs کو `cloud-config.js` میں برقرار رکھا گیا ہے۔ Server connectivity پھر بھی GitHub Actions build کے بعد اصل فون پر OTP، cloud ping، backup اور restore سے لازماً ٹیسٹ کریں۔

## اہم حدود
- یہ سورس پیکج اس ماحول میں Android SDK/Gradle کے ساتھ build یا حقیقی فون پر test نہیں ہوا۔ GitHub Actions کی کامیاب build اور اصل فون پر functional tests ابھی ضروری ہیں۔
- عام Android API ہر headset کی battery نہیں بتاتی، اور ہر paired device کا active audio connection status ہر فون پر عام public API سے ثابت نہیں کیا جا سکتا۔ ANC/Transparency/Spatial Audio controls کے لیے مخصوص headset کا قابلِ اعتماد vendor protocol/SDK درکار ہے۔
- Email OTP Apps Script endpoint موجود ہے؛ Google Sign-In کا origin/OAuth setup الگ مسئلہ ہو سکتا ہے۔ `appassets.androidplatform.net` کو OAuth authorized JavaScript origin کے طور پر منظور کیے بغیر Google button لازماً کام نہیں کرے گا۔
- VersionCode 55 اور versionName 5.5-native-bt ہیں۔ اگر آپ کے فون پر پہلے والی APK مختلف signing key سے install ہے تو Android update قبول نہیں کرے گا؛ اسے update کرنے کے لیے ایک مستقل signing key درکار ہے۔ Debug APK کو production updates کے لیے مستقل key نہ سمجھیں۔

## GitHub Actions سے APK بنائیں
1. اس ZIP کو extract کریں۔
2. اس کے اندر کی تمام چیزیں repository کے root میں upload کریں (یعنی `app/`, `.github/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`). ZIP کو اکیلا upload کرنا کافی نہیں۔
3. GitHub → Actions → `Build Pak GSM Repair Android APK` → `Run workflow` منتخب کریں۔
4. build کامیاب ہو تو اسی run کے آخر میں `Pak-GSM-Repair-5.5-Native-Bluetooth-debug-APK` Artifact ڈاؤن لوڈ کریں۔
5. فون میں Bluetooth آن کریں، Nearby devices permission دیں، earbuds کو pairing/discoverable mode میں رکھیں، پھر Bluetooth panel میں scan کریں۔
6. login/OTP، Cloud Settings کا Test Connection، backup اور restore الگ الگ آزمائیں۔ Build سبز ہونے کا مطلب یہ نہیں کہ ہر server یا headset feature پہلے ہی functional ثابت ہو گیا ہے۔


## Version 5.6 release notes
- Update checks/downloads use the published GitHub Pages base URL, while downloaded files are staged under the app's local HTTPS asset origin.
- App logo references use the existing PNG icon, and the updater no longer requests a missing pgr-logo.svg.
- Native Bluetooth cards are selectable in the app; opening Android Bluetooth settings is still needed to complete pairing/connection where Android does not expose a generic connection API.
- Registration sends username and shopName parameters to the Apps Script endpoint. Confirm the deployed Apps Script register/login handlers store and return these fields; front-end changes alone cannot guarantee server persistence.
- Shop cover photo and shop identity are stored locally on the device unless the Apps Script backend is extended to persist them.
- This source package has not been built or device-tested in this environment.
