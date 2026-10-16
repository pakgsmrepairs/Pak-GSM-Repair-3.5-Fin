Pak GSM Repair 5.7.4 — Android APK Source ZIP

اس ZIP میں Android Studio کا source project ہے، تیار شدہ APK نہیں۔

اہم تبدیلیاں:
1) Android app کی versionName اب 5.7.4 اور versionCode 572 ہے۔
2) Android کے اندر 5.7.4 PWA کی index.html، manifest، service worker، cloud config، update.json اور icons شامل ہیں۔ Native Bluetooth Java bridge برقرار ہے۔
3) Update flow میں اصل bug درست کیا گیا: download کے بعد update.json کو remote GitHub URL سے temporary cache میں تلاش کیا جا رہا تھا، حالانکہ فائل app کے local cache URL پر رکھی گئی تھی۔ اب lookup local staged URL سے ہوتی ہے۔
4) github-pages-update فولڈر بھی 5.7.4 release files رکھتا ہے؛ اسے صرف اس وقت repository root میں publish کریں جب اسی release کو ویب سائٹ پر جاری کرنا مقصود ہو۔

APK بنانے کا طریقہ:
- ZIP کو extract کریں۔
- فولڈر Android Studio میں Open کریں۔
- Gradle sync مکمل ہونے دیں؛ Java 17 اور Android SDK Platform 35 درکار ہیں۔
- Build > Build Bundle(s) / APK(s) > Build APK(s) منتخب کریں۔
- Debug APK عموماً app/build/outputs/apk/debug/app-debug.apk میں ملے گا۔

GitHub Actions سے:
- اس project کو GitHub repository میں upload/push کریں۔
- Actions میں “Build Pak GSM Repair Android APK” workflow کھولیں اور Run workflow دبائیں۔
- کامیاب build کے بعد Pak-GSM-Repair-5.7.4-debug-APK artifact ڈاؤن لوڈ کریں۔

نوٹ: Debug APK test کے لیے ہے۔ اگر پہلے سے install APK کو update کرنا ہے تو signed release build کے لیے وہی signing key درکار ہو سکتی ہے جو پرانی APK میں استعمال ہوئی تھی۔ اس ZIP سے APK build/run کرنے کے بعد فون پر login, customer save, Bluetooth اور update flow لازماً ٹیسٹ کریں۔
