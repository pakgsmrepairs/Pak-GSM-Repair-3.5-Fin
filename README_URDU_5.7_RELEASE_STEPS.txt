Pak GSM Repair 5.7.2 — Release Notes

اس ریلیز میں:
- رجسٹریشن فارم کو موبائل کے لیے زیادہ compact کیا گیا۔
- Username/Email fields پر غیر ضروری browser autofill تجاویز کم کرنے کے لیے autocomplete hints بدلے گئے؛ Android/Chrome Password Manager کو مکمل طور پر بند کرنے کی ضمانت نہیں۔
- Password کے نیچے واضح requirements شامل ہیں۔ موجودہ کم از کم 4 حروف اور uppercase/lowercase/number/special-character کی validation برقرار ہے۔
- Bluetooth status/Connect strip کو Dashboard Bluetooth card کے اندر منتقل کیا گیا تاکہ الگ اوپر/نیچے حصے نہ ہوں۔
- Shop photo/logo controls اور existing handlers برقرار رکھے گئے؛ header icon sizing بہتر کی گئی۔
- تمام version metadata کو 5.7.2 پر یکساں کیا گیا۔

اہم حدود: دونوں earbuds کی الگ battery صرف اسی وقت دکھائی جا سکتی ہے جب Android/native bridge یا headset یہ values فراہم کرے۔ ANC/Transparency controls صرف supported headset protocol کے ساتھ کام کرتے ہیں۔

GitHub Pages پر deploy کرنے کے لیے ZIP کو extract کرکے تمام release files ایک ساتھ repository root میں publish کریں۔ HTTPS پر ٹیسٹ کریں؛ local file:// preview Google Sign-In اور بعض Bluetooth APIs کو سپورٹ نہیں کرتا۔


نوٹ: Earbud کی Left/Right بیٹری صرف اسی وقت اصل فیصد دکھائے گی جب native Android bridge الگ values فراہم کرے؛ عام ایک battery value کو دونوں طرف نقل نہیں کیا جاتا۔
