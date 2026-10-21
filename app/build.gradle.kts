plugins { id("com.android.application") }

android {
    namespace = "com.pakgsm.repair"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pakgsm.repair"
        minSdk = 26
        targetSdk = 35
        versionCode = 850
        // 8.5 release validation: status picture and shop-logo picker fixes
        versionName = "8.5"
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
