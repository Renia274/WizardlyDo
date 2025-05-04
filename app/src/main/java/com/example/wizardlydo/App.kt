package com.example.wizardlydo

import android.app.Application
import androidx.work.WorkManager
import com.example.wizardlydo.repository.wizard.WizardRepository
import com.example.wizardlydo.repository.pin.PinRepository
import com.example.wizardlydo.repository.tasks.TaskRepository
import com.example.wizardlydo.room.WizardDao
import com.example.wizardlydo.room.WizardDatabase
import com.example.wizardlydo.room.WizardTypeConverters
import com.example.wizardlydo.utilities.SecurityProvider
import com.example.wizardlydo.viewmodel.CustomizationViewModel
import com.example.wizardlydo.viewmodel.LoginViewModel
import com.example.wizardlydo.viewmodel.PinViewModel
import com.example.wizardlydo.viewmodel.RecoveryViewModel
import com.example.wizardlydo.viewmodel.SettingsViewModel
import com.example.wizardlydo.viewmodel.TaskViewModel
import com.example.wizardlydo.viewmodel.WizardAuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin

import org.koin.dsl.module
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.wizardlydo.utilities.TaskNotificationService
import com.example.wizardlydo.viewmodel.InventoryViewModel
import com.example.wizardlydo.repository.inventory.InventoryRepository

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

    // WorkManager - used for scheduling notifications
    single { WorkManager.getInstance(androidContext()) }

    // Notification Service
    factory { (context: Context) -> TaskNotificationService(context) }

    // Room Database
    single { WizardDatabase.getDatabase(androidContext()) }
    single { get<WizardDatabase>().wizardDao() }
    single { get<WizardDatabase>().pinDao() }
    single { get<WizardDatabase>().taskDao() }
    single { get<WizardDatabase>().inventoryDao() }

    single<WizardRepository> {
        object : WizardRepository {
            override val wizardDao = get<WizardDao>()
            override val firebaseAuth = get<FirebaseAuth>()
        }
    }
    single { PinRepository(get(), get()) }
    single { TaskRepository(get()) }
    single { InventoryRepository(get()) }

    // Utilities
    single { WizardTypeConverters() }
    single { SecurityProvider(androidContext()) }

    // ViewModels
    viewModelOf(::WizardAuthViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RecoveryViewModel)
    viewModel { TaskViewModel(get(), get(), get()) }
    viewModelOf(::PinViewModel)
    viewModelOf(::SettingsViewModel)

    // Add inventory viewmodel
    viewModel { InventoryViewModel(get(), get()) }

    viewModel { params ->
        CustomizationViewModel(
            repository = get(),
            wizardClass = params.get()
        )
    }
}