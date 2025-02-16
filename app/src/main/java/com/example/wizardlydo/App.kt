package com.example.wizardlydo

import android.app.Application
import com.google.firebase.FirebaseApp
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
    }
}

// Create your Koin module
val appModule = module {
    // Firebase Auth
    single { Firebase.auth }

    // Add other dependencies
    single<SharedPreferences> {
        androidContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    // ViewModels
    viewModel { AuthViewModel(get(),get()) }
}