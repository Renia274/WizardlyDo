package com.example.wizardlydo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
// Remove BuildConfig import - we'll use a different approach
import com.example.wizardlydo.room.inventory.InventoryDao
import com.example.wizardlydo.room.inventory.InventoryItemEntity
import com.example.wizardlydo.room.pin.PinDao
import com.example.wizardlydo.room.pin.PinEntity
import com.example.wizardlydo.room.tasks.TaskDao
import com.example.wizardlydo.room.tasks.TaskEntity
import com.example.wizardlydo.room.wizard.WizardDao
import com.example.wizardlydo.room.wizard.WizardEntity


@Database(
    entities = [WizardEntity::class, PinEntity::class, TaskEntity::class, InventoryItemEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(WizardTypeConverters::class)
abstract class WizardDatabase : RoomDatabase() {
    abstract fun wizardDao(): WizardDao
    abstract fun pinDao(): PinDao
    abstract fun taskDao(): TaskDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: WizardDatabase? = null

        fun getDatabase(context: Context, isLive: Boolean = false): WizardDatabase {
            return INSTANCE ?: synchronized(this) {
                val databaseName = if (isLive) {
                    "wizard_database_live"
                } else {
                    "wizard_database_dev"
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WizardDatabase::class.java,
                    databaseName
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}