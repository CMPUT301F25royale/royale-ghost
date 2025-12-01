plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

val nav_version = "2.7.7"

android {
    namespace = "com.example.project_part_3"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.camera.view)
    implementation(libs.identity.jvm)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")
    androidTestImplementation("androidx.fragment:fragment-testing:1.7.1")
    implementation("androidx.navigation:navigation-fragment:${nav_version}")
    implementation("androidx.navigation:navigation-ui:${nav_version}")
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-firestore")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

    // CameraX
    val camerax_version = "1.5.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    // ML Kit Barcode
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // QR Code generation
    implementation("com.google.zxing:core:3.5.4")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.guava:guava:31.1-android")
}


//JAVADOC STUFF (NOT WORKING)
/*
}

// -----------------------------------------------------------------------
// Javadoc Task
// -----------------------------------------------------------------------

// Place this at the bottom of app/build.gradle.kts

// Place this at the bottom of app/build.gradle.kts

tasks.register<Javadoc>("androidJavadocs") {
    // 1. Source: Your main source code
    val sourceFiles = android.sourceSets.getByName("main").java.srcDirs
    source = files(sourceFiles).asFileTree

    // 2. Output
    setDestinationDir(file("${project.rootDir}/javadoc/"))
    isFailOnError = false

    doFirst {
        println("Generating Javadoc...")

        // A. Boot Classpath (android.jar)
        // We convert the FileCollection directly to a list of files to ensure it resolves
        val bootClasspath = files(android.bootClasspath)

        // B. Project Dependencies
        // We use the 'debug' variant's compile configuration
        var dependencyClasspath: FileCollection = files()
        val variant = android.applicationVariants.find { it.name == "debug" }
        if (variant != null) {
            dependencyClasspath = variant.javaCompileProvider.get().classpath
        }

        // C. Generated R.jar
        val buildDir = layout.buildDirectory.get().asFile
        // Check multiple possible locations for R.jar
        val rJarLocation1 = file("$buildDir/intermediates/compile_and_runtime_not_namespaced_r_class_jar/debug/R.jar")
        val rJarLocation2 = file("$buildDir/intermediates/compile_and_runtime_not_namespaced_r_class_jar/debug/processDebugResources/R.jar")

        // Fallback: If R.jar doesn't exist, we don't add it (avoids crashing the task, though R symbols will fail)
        val rJar = if (rJarLocation1.exists()) {
            files(rJarLocation1)
        } else if (rJarLocation2.exists()) {
            files(rJarLocation2)
        } else {
            println("WARNING: R.jar not found. Resource IDs (R.id.xyz) will cause Javadoc errors.")
            files()
        }

        // D. Combine everything
        classpath = bootClasspath + dependencyClasspath + rJar
    }

    options {
        (this as StandardJavadocDocletOptions).apply {
            links("https://developer.android.com/reference/")
            linksOffline("https://docs.oracle.com/javase/8/docs/api/", "${android.sdkDirectory}/docs/reference/")
            addStringOption("Xdoclint:none", "-quiet")
        }
    }
}

// Ensure the build runs first so R.jar is generated
tasks.named("androidJavadocs") {
    dependsOn("assembleDebug")
 */