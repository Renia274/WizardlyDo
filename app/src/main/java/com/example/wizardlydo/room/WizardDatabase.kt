package com.example.wizardlydo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [WizardEntity::class,PinEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(WizardTypeConverters::class)
abstract class WizardDatabase : RoomDatabase() {
    abstract fun wizardDao(): WizardDao
    abstract fun pinDao(): PinDao


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