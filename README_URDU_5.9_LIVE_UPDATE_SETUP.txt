Pak GSM Repair 5.9 — Urdu Setup Guide

اہم: یہ سورس پیکج ہے، APK نہیں۔ پہلی مرتبہ 5.9 APK بنانا ضروری ہے کیونکہ اس ورژن میں Android کا نیا HTML-update bridge شامل کیا گیا ہے۔ اس ایک بار کے بعد عام HTML/UI تبدیلیوں کے لیے ہر دفعہ APK rebuild نہیں کرنا پڑے گا۔ Java، Bluetooth bridge، Android permissions یا دوسری native تبدیلیوں کے لیے نئی APK پھر بھی ضروری ہے۔

1) مکمل APK سورس اپلوڈ کرنے کا طریقہ
- ZIP Extract کریں۔ اندر موجود Pak-GSM-Repair-5.9-Final-Source فولڈر کے اندر کی فائلیں/فولڈرز اپنی GitHub repository کے root میں اپلوڈ/replace کریں؛ outer folder کو repository کے اندر اضافی nesting کے ساتھ نہ رکھیں۔
- GitHub Actions کھولیں اور “Build Pak GSM Repair 5.9 Android APK” workflow چلائیں۔
- مکمل ہونے کے بعد artifact “Pak-GSM-Repair-5.9-debug-APK” ڈاؤن لوڈ کریں۔ یہ build یہاں مقامی طور پر نہیں چلایا گیا؛ GitHub Actions کا نتیجہ چیک کریں۔

2) Android ایپ کے مستقبل کے HTML-only اپڈیٹس
- repository کے root میں android-web-update/ فولڈر موجود رہنا چاہیے۔ اس کے اندر index.html اور update.json ہیں۔
- اس فولڈر کو GitHub Pages پر اسی راستے پر شائع ہونا چاہیے:
  https://pakgsmrepairs.github.io/Pak-GSM-Repair-3.5-Fin/android-web-update/update.json
  https://pakgsmrepairs.github.io/Pak-GSM-Repair-3.5-Fin/android-web-update/index.html
- آئندہ UI/HTML اپڈیٹ کے لیے android-web-update/index.html اور update.json دونوں ساتھ اپڈیٹ کریں۔ update.json میں version اور index.html کے const APP_VERSION کی قدر ایک جیسی رکھیں، مثلاً 5.10۔
- پھر ایپ کے Update App/Download & Apply بٹن سے اپڈیٹ چیک، ڈاؤن لوڈ اور apply کریں۔
- اگر آپ کا GitHub Pages کسی دوسرے branch/folder سے publish ہوتا ہے تو android-web-update/ فولڈر کو اسی published جگہ پر رکھیں تاکہ اوپر والے URLs واقعی کھلیں۔

3) لوگو/آئیکنز
- HTML assets میں icons/ اور icon/ دونوں راستے شامل ہیں تاکہ پرانے singular/plural path فرق سے لوگو غائب نہ ہو۔
- Android launcher icon کے mipmap وسائل بھی سورس میں موجود ہیں۔

4) اہم حدود
- عام HTML/CSS/JavaScript اسکرین کی اپڈیٹ کے لیے نیا APK نہیں چاہیے، بشرطیکہ android-web-update/ فولڈر شائع ہو اور ایپ کو انٹرنیٹ دستیاب ہو۔
- Java/Android permissions/Bluetooth native code کی تبدیلی کے لیے نیا APK بنانا ہوگا۔
- Bluetooth کی اصل pairing/connection اور earbuds battery reporting Android اور device کی support پر منحصر رہتی ہے؛ اس پیکج کی Bluetooth hardware پر جانچ یہاں نہیں ہوئی۔
