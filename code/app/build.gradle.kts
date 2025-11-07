plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

val nav_version = "2.7.7"

android {
    namespace = "com.example.project_part_3"

    // Use a stable compile/target SDK (36 is fine if you installed the preview SDK)
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.project_part_3"
        minSdk = 24
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java 8+ is fine; bump to 17 if your AGP requires it
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Uncomment if you want findViewById safety/less boilerplate
    // buildFeatures { viewBinding = true }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Navigation (use -ktx if your code is Kotlin)
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    // If Java-only, you can switch these back to non-ktx artifacts

    // Firebase BoM + Firestore
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.material:material:1.12.0")

    // ===== CameraX =====
//    implementation(platform("androidx.camera:camera-bom:1.3.4"))
//    implementation("androidx.camera:camera-core")
//    implementation("androidx.camera:camera-camera2")
//    implementation("androidx.camera:camera-lifecycle")
//    implementation("androidx.camera:camera-view")
//    implementation("com.google.android.material:material:1.12.0")

}
