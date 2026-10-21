plugins { id("com.android.application") }

android {
    namespace = "com.pakgsm.repair"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pakgsm.repair"
        minSdk = 26
        targetSdk = 35
        versionCode = 881
        // 8.8 release: install over 8.5 without uninstall; in-app updater saves HTML and logo/icon assets
        versionName = "8.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}


dependencies {
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("androidx.biometric:biometric:1.1.0")
}
