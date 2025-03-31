package com.example.mooddiary

import android.app.Application
import com.example.mooddiary.data.database.MoodDiaryDatabase
import com.example.mooddiary.data.repository.MoodEntryRepository

/**
 * 应用程序类，用于初始化应用级别的组件
 */
class MoodDiaryApplication : Application() {
    // 使用懒加载初始化数据库
    private val database by lazy { MoodDiaryDatabase.getDatabase(this) }
    
    // 使用懒加载初始化仓库
    val repository by lazy { MoodEntryRepository(database.moodEntryDao()) }
    
    override fun onCreate() {
        super.onCreate()
        // 在这里可以进行其他应用级别的初始化
    }
}