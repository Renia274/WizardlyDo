package com.example.wizardlydo

import android.app.Application
import com.example.wizardlydo.repository.WizardRepository
import com.example.wizardlydo.room.WizardDatabase
import com.example.wizardlydo.room.WizardTypeConverters
import com.example.wizardlydo.viewmodel.LoginViewModel
import com.example.wizardlydo.viewmodel.WizardAuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
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

val appModule = module {
    // Firebase Services (singleton SDK instances)
    singleOf(::FirebaseAuth) { Firebase.auth }
    single<FirebaseAuth> { Firebase.auth }

    // Room Database
    single { WizardDatabase.getDatabase(androidContext()) }
    single { get<WizardDatabase>().wizardDao() }

    // Repository
    single { WizardRepository(get()) }

    // Utilities
    single { WizardTypeConverters() }

    // ViewModels
    viewModelOf(::WizardAuthViewModel)
    viewModelOf(::LoginViewModel)
}