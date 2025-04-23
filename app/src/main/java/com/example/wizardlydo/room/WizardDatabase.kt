package com.example.wizardlydo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wizardlydo.room.tasks.TaskDao
import com.example.wizardlydo.room.tasks.TaskEntity

@Database(
    entities = [WizardEntity::class,PinEntity::class,TaskEntity::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(WizardTypeConverters::class)
abstract class WizardDatabase : RoomDatabase() {
    abstract fun wizardDao(): WizardDao
    abstract fun pinDao(): PinDao
    abstract fun taskDao(): TaskDao


    companion object {
        @Volatile
        private var INSTANCE: WizardDatabase? = null

        fun getDatabase(context: Context): WizardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WizardDatabase::class.java,
                    "wizard_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}