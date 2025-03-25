package com.example.wizardlydo

import android.app.Application
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wizardlydo.repository.WizardRepository
import com.example.wizardlydo.repository.pin.PinRepository
import com.example.wizardlydo.room.WizardDatabase
import com.example.wizardlydo.room.WizardTypeConverters
import com.example.wizardlydo.utilities.SecurityProvider
import com.example.wizardlydo.viewmodel.CustomizationViewModel
import com.example.wizardlydo.viewmodel.LoginViewModel
import com.example.wizardlydo.viewmodel.PinViewModel
import com.example.wizardlydo.viewmodel.RecoveryViewModel
import com.example.wizardlydo.viewmodel.WizardAuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel


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
    // Firebase Services
    single<FirebaseAuth> { Firebase.auth }

    // Room Database
    single { WizardDatabase.getDatabase(androidContext()) }
    single { get<WizardDatabase>().wizardDao() }
    single { get<WizardDatabase>().pinDao() }

    // Repository
    single { WizardRepository(get(), get()) }
    single { PinRepository(get(), get()) }

    // Utilities
    single { WizardTypeConverters() }
    single { SecurityProvider(androidContext()) }

    // ViewModels
    viewModelOf(::WizardAuthViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RecoveryViewModel)
    viewModel { PinViewModel(get()) }

    viewModel { params ->
        CustomizationViewModel(
            repository = get(),
            wizardClass = params.get()  // Get from navigation parameters
        )
    }

}