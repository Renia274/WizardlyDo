plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.wizardlydo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wizardlydo"
        minSdk = 25
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    sourceSets.configureEach {
        kotlin.srcDir("build/generated/ksp/${name}/kotlin")
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.core)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.fido)
    implementation(libs.material)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.fido)
    implementation(libs.play.services.fido)
    implementation(libs.androidx.tools.core)


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test)

    debugImplementation(libs.androidx.ui.tooling)




    // Coil for Image Loading
    implementation(libs.coil.compose)

    // Firebase UI
    implementation(libs.firebase.ui.auth)



    // Accompanist Permissions 
    implementation(libs.accompanist.permissions)

    // Compose Constraint Layout
    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.googleid)

    // Credential Manager
    implementation (libs.androidx.credentials)

    implementation (libs.androidx.credentials.play.services.auth)


    //Koin
    // Koin Core
    implementation (libs.koin.core)

    // Koin Android
    implementation (libs.koin.android)

    // Koin Compose
    implementation (libs.koin.androidx.compose)

    implementation(libs.koin.annotation)

    ksp(libs.koin.ksp.compiler)

    // Room
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    ksp (libs.androidx.room.compiler)

    implementation(libs.bcrypt)

    // WorkManager with Kotlin support
    implementation (libs.androidx.work.runtime.ktx.v280)




}