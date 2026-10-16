Pak GSM Repair 5.7.4 — source adjustments and recheck

اس پیکج میں:
- Android app assets میں 5.7.3 کے پرانے version labels کو 5.7.4 کے ساتھ align کیا گیا ہے۔
- update.json کو 5.7.4 کیا گیا ہے۔
- PWA update restart URL کو index.html پر بھیجا گیا ہے، asset directory پر نہیں۔
- app/build.gradle.kts میں versionName 5.7.4 اور versionCode 575 برقرار ہیں۔
- پرانی build-android.yml فائل ہٹا دی گئی ہے کیونکہ وہ پرانے 5.7.3 ZIP سے APK بناتی تھی۔ اب build-apk.yml موجودہ Android project سے build کرتا ہے۔
- README_URDU_RECHECK_REPORT.txt میں چیک کی تفصیل اور ابھی باقی مسائل درج ہیں۔

جانچ:
- HTML کے inline JavaScript بلاکس Node.js syntax check میں کامیاب رہے۔
- update.json اور manifest.json درست JSON ہیں۔
- AndroidManifest.xml درست XML ہے۔
- source میں 5.7.P / 5.7.p نہیں ملا؛ پرانا مسئلہ 5.7.3 workflow کا تھا۔

اہم باقی کام:
- یہاں Android SDK/Gradle build نہیں چلایا گیا، اس لیے APK compile ہونے کی تصدیق باقی ہے۔ GitHub Actions کا نتیجہ دیکھنا ہوگا۔
- Bluetooth کی فہرست اور discovery موجود ہے، لیکن حقیقی profile connection state ابھی مکمل implement نہیں۔ isConnected() اس وقت false دیتا ہے، اور ڈیوائس منتخب کرنے پر Android Bluetooth Settings کھلتی ہیں۔
- بیٹری کی معلومات ہر Bluetooth device سے نہیں ملتیں۔
- موجودہ Update App بٹن PWA/web assets کو cache میں stage کرتا ہے، Android APK install نہیں کرتا۔ APK update کے لیے native installer flow الگ بنانا ہوگا۔
