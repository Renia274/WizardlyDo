plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.wizardlydo.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wizardlydo.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "DATABASE_NAME", "\"wizard_database_dev\"")
        }

        release {
            isMinifyEnabled = false
            buildConfigField("String", "DATABASE_NAME", "\"wizard_database_live\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    sourceSets.configureEach {
        kotlin.srcDir("build/generated/ksp/${name}/kotlin")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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
    implementation(libs.play.services.auth)
    implementation(libs.play.services.fido)
    implementation(libs.material)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.tools.core)
    implementation(libs.androidx.compose.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test)

    // Debug implementations
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
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.annotation)
    ksp(libs.koin.ksp.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.bcrypt)

    // WorkManager with Kotlin support
    implementation(libs.androidx.work.runtime.ktx.v280)

    // Permission handling with accompanist
    implementation(libs.accompanist.permissions.v0301)
}