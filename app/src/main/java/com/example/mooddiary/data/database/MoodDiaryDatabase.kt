package com.example.mooddiary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mooddiary.data.dao.MoodEntryDao
import com.example.mooddiary.data.model.MoodEntry
import com.example.mooddiary.data.util.DateTimeConverters

/**
 * 心情日记应用的Room数据库
 */
@Database(entities = [MoodEntry::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeConverters::class)
abstract class MoodDiaryDatabase : RoomDatabase() {
    
    /**
     * 获取心情记录DAO
     */
    abstract fun moodEntryDao(): MoodEntryDao
    
    companion object {
        @Volatile
        private var INSTANCE: MoodDiaryDatabase? = null
        
        /**
         * 获取数据库实例，如果不存在则创建
         */
        fun getDatabase(context: Context): MoodDiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodDiaryDatabase::class.java,
                    "mood_diary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}