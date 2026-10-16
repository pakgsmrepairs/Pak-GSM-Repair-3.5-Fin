Pak GSM Repair 5.8 — Bluetooth Fix + Calculator

اس سورس ZIP میں درج ذیل تبدیلیاں شامل ہیں:
1) Android app versionName: 5.8 اور versionCode: 580۔
2) Bluetooth connected status کے لیے Android کے دستیاب HEADSET/A2DP profiles استعمال کیے گئے ہیں۔
3) Scan refresh کے دوران منتخب Bluetooth device برقرار رکھنے کی کوشش کی گئی ہے۔
4) Calculator صفحہ اور dashboard shortcut شامل ہے۔
5) Android APK کے assets اور github-pages-update کی version metadata کو 5.8 پر یکساں کیا گیا ہے۔

اہم حدود:
- ہر Bluetooth device یا Android/OEM پر connection state اور battery percentage دستیاب ہونا یقینی نہیں؛ battery information standard public API سے ہر device پر نہیں ملتی۔
- اس ماحول میں Gradle/Android build نہیں چلایا گیا، اس لیے GitHub Actions میں APK build مکمل ہونے اور حقیقی فون پر Bluetooth testing کے بعد ہی اسے final tested APK سمجھیں۔

GitHub پر Android build کے لیے project کے اندر موجود app/، Gradle files اور .github/workflows/build-apk.yml کو repository root پر رکھیں۔ صرف ZIP فائل repository میں ڈالنا کافی نہیں، جب تک workflow اسے extract کرنے کے لیے نہ بنایا گیا ہو۔
