plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.Apoloplay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.Apoloplay"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        vectorDrawables {
            useSupportLibrary = true
        }

    // --- Add manifest placeholders here ---
    manifestPlaceholders["redirectSchemeName"] = "com.example.apoloplay"
    manifestPlaceholders["redirectHostName"] = "callback"
}
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false //
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    packaging {
        resources {
            excludes += setOf(
                "/META-INF/*.kotlin_module",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }
    lint {
        abortOnError = false
    }
}





dependencies {
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // --- Dependência do Desugar ---
    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.0")

    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))
    //implementation(files("libs/spotify-auth-release-2.1.0.aar"))
    implementation(files("libs/spotify-auth-store-release-2.1.0.aar"))




    // Networking e Imagens
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.picasso:picasso:2.71828")

    // AndroidX e UI
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    //  OkHttp Logging Interceptor (útil para depuração)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    }




