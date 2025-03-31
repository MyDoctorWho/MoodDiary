package com.example.mooddiary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * 表示一条心情日记记录
 */
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val moodType: MoodType,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 心情类型枚举
 */
enum class MoodType {
    VERY_HAPPY,
    HAPPY,
    NEUTRAL,
    SAD,
    VERY_SAD;
    
    fun getEmojiIcon(): String {
        return when (this) {
            VERY_HAPPY -> "😄"
            HAPPY -> "🙂"
            NEUTRAL -> "😐"
            SAD -> "😔"
            VERY_SAD -> "😢"
        }
    }
    
    fun getDisplayName(): String {
        return when (this) {
            VERY_HAPPY -> "非常开心"
            HAPPY -> "开心"
            NEUTRAL -> "平静"
            SAD -> "难过"
            VERY_SAD -> "非常难过"
        }
    }
}