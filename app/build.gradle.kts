plugins { id("com.android.application") }

android {
    namespace = "com.pakgsm.repair"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pakgsm.repair"
        minSdk = 26
        targetSdk = 35
        versionCode = 609
        versionName = "6.9"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}


dependencies {
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.fragment:fragment:1.8.5")
}
