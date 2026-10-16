plugins { id("com.android.application") }

android {
    namespace = "com.pakgsm.repair"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pakgsm.repair"
        minSdk = 26
        targetSdk = 35
        versionCode = 575
        versionName = "5.7.4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}


dependencies {
    implementation("androidx.webkit:webkit:1.12.1")
}
