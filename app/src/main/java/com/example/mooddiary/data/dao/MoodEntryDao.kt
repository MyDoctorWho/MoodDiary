package com.example.mooddiary.data.dao

import androidx.room.*
import com.example.mooddiary.data.model.MoodEntry
import com.example.mooddiary.data.model.MoodType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * 心情日记数据访问对象接口
 */
@Dao
interface MoodEntryDao {
    /**
     * 插入一条心情记录
     */
    @Insert
    suspend fun insert(moodEntry: MoodEntry): Long
    
    /**
     * 更新一条心情记录
     */
    @Update
    suspend fun update(moodEntry: MoodEntry)
    
    /**
     * 删除一条心情记录
     */
    @Delete
    suspend fun delete(moodEntry: MoodEntry)
    
    /**
     * 获取所有心情记录，按日期降序排列
     */
    @Query("SELECT * FROM mood_entries ORDER BY date DESC")
    fun getAllMoodEntries(): Flow<List<MoodEntry>>
    
    /**
     * 根据ID获取一条心情记录
     */
    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getMoodEntryById(id: Long): MoodEntry?
    
    /**
     * 根据日期获取心情记录
     */
    @Query("SELECT * FROM mood_entries WHERE date = :date")
    suspend fun getMoodEntryByDate(date: LocalDate): MoodEntry?
    
    /**
     * 根据心情类型获取心情记录
     */
    @Query("SELECT * FROM mood_entries WHERE moodType = :moodType ORDER BY date DESC")
    fun getMoodEntriesByType(moodType: MoodType): Flow<List<MoodEntry>>
    
    /**
     * 获取指定日期范围内的心情记录
     */
    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getMoodEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<MoodEntry>>
    
    /**
     * 获取最近的心情记录，限制数量
     */
    @Query("SELECT * FROM mood_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentMoodEntries(limit: Int): Flow<List<MoodEntry>>
}