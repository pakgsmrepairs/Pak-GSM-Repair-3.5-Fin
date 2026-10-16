Pak GSM Repair 5.7.4 — Android Source ZIP (Gradle wrapper-free build)

This source project does not contain the Gradle Wrapper (gradlew / gradle-wrapper.jar). The included GitHub Actions workflow is designed to install Gradle 8.9 on the GitHub runner and build with `gradle --no-daemon assembleDebug`, so the build does not depend on a missing wrapper.

APK artifact name: Pak-GSM-Repair-5.7.4.apk
Android app display name is controlled separately by AndroidManifest.xml / string resources; renaming the APK file does not change the installed app label.

Build not executed in this environment. After the GitHub build succeeds, test installation, Bluetooth, login, customer saving, and Update/update.json on the device.
