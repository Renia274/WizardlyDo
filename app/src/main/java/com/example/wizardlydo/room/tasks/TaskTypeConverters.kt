package com.example.wizardlydo.room.tasks

import androidx.room.TypeConverter
import com.example.wizardlydo.data.Priority
import java.util.Date

class TaskTypeConverters {
    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = enumValueOf(value)

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? = date?.time
}