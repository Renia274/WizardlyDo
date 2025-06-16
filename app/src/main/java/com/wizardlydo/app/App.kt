package com.wizardlydo.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.wizardlydo.app.repository.inventory.InventoryRepository
import com.wizardlydo.app.repository.pin.PinRepository
import com.wizardlydo.app.repository.tasks.TaskRepository
import com.wizardlydo.app.repository.wizard.WizardRepository
import com.wizardlydo.app.room.WizardDatabase
import com.wizardlydo.app.room.WizardTypeConverters
import com.wizardlydo.app.utilities.RememberMeManager
import com.wizardlydo.app.utilities.TaskNotificationService
import com.wizardlydo.app.utilities.security.SecurityProvider
import com.wizardlydo.app.viewmodel.customization.CustomizationViewModel
import com.wizardlydo.app.viewmodel.inventory.InventoryViewModel
import com.wizardlydo.app.viewmodel.login.LoginViewModel
import com.wizardlydo.app.viewmodel.pin.PinViewModel
import com.wizardlydo.app.viewmodel.recovery.RecoveryViewModel
import com.wizardlydo.app.viewmodel.settings.SettingsViewModel
import com.wizardlydo.app.viewmodel.signup.SignupViewModel
import com.wizardlydo.app.viewmodel.tasks.TaskViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Create notification channels for tasks
        createNotificationChannels()

        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Task reminder channel
            val taskChannel = NotificationChannel(
                "task_reminder",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task reminders and deadlines"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(taskChannel)
        }
    }
}

val appModule = module {
    // Firebase Services
    single<FirebaseAuth> { Firebase.auth }

    // WorkManager
    single { WorkManager.getInstance(androidContext()) }

    // Notification Service
    factory { (context: Context) -> TaskNotificationService(context) }



    // Room Database
    single { WizardDatabase.getDatabase(androidContext()) }
    single { get<WizardDatabase>().wizardDao() }
    single { get<WizardDatabase>().pinDao() }
    single { get<WizardDatabase>().taskDao() }
    single { get<WizardDatabase>().inventoryDao() }

    //Repository
    single {WizardRepository(get(), get(),get())}
    single { PinRepository(get(), get()) }
    single { TaskRepository(get()) }
    single { InventoryRepository(get()) }

    // Utilities
    single { WizardTypeConverters() }
    single { SecurityProvider(androidContext()) }
    single { RememberMeManager(androidContext()) }

    // ViewModels
    viewModelOf(::SignupViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RecoveryViewModel)
    viewModel { TaskViewModel(get(), get(), get(),get()) }
    viewModelOf(::PinViewModel)
    viewModelOf(::SettingsViewModel)
    viewModel { InventoryViewModel(get(), get()) }
    viewModel { params ->
        CustomizationViewModel(
            repository = get(),
            wizardClass = params.get()
        )
    }
}
