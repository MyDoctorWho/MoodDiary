package com.example.mooddiary.data.repository

import com.example.mooddiary.data.dao.MoodEntryDao
import com.example.mooddiary.data.model.MoodEntry
import com.example.mooddiary.data.model.MoodType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * 心情日记数据仓库
 * 作为数据源和视图模型之间的中介
 */
class MoodEntryRepository(private val moodEntryDao: MoodEntryDao) {
    
    /**
     * 获取所有心情记录
     */
    fun getAllMoodEntries(): Flow<List<MoodEntry>> {
        return moodEntryDao.getAllMoodEntries()
    }
    
    /**
     * 根据ID获取心情记录
     */
    suspend fun getMoodEntryById(id: Long): MoodEntry? {
        return moodEntryDao.getMoodEntryById(id)
    }
    
    /**
     * 根据日期获取心情记录
     */
    suspend fun getMoodEntryByDate(date: LocalDate): MoodEntry? {
        return moodEntryDao.getMoodEntryByDate(date)
    }
    
    /**
     * 根据心情类型获取心情记录
     */
    fun getMoodEntriesByType(moodType: MoodType): Flow<List<MoodEntry>> {
        return moodEntryDao.getMoodEntriesByType(moodType)
    }
    
    /**
     * 获取指定日期范围内的心情记录
     */
    fun getMoodEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<MoodEntry>> {
        return moodEntryDao.getMoodEntriesBetweenDates(startDate, endDate)
    }
    
    /**
     * 获取最近的心情记录
     */
    fun getRecentMoodEntries(limit: Int): Flow<List<MoodEntry>> {
        return moodEntryDao.getRecentMoodEntries(limit)
    }
    
    /**
     * 插入心情记录
     */
    suspend fun insertMoodEntry(moodEntry: MoodEntry): Long {
        return moodEntryDao.insert(moodEntry)
    }
    
    /**
     * 更新心情记录
     */
    suspend fun updateMoodEntry(moodEntry: MoodEntry) {
        moodEntryDao.update(moodEntry)
    }
    
    /**
     * 删除心情记录
     */
    suspend fun deleteMoodEntry(moodEntry: MoodEntry) {
        moodEntryDao.delete(moodEntry)
    }
}