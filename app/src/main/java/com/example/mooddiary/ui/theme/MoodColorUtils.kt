package com.example.mooddiary.ui.theme

import com.example.mooddiary.R
import com.example.mooddiary.data.model.MoodType

/**
 * 心情类型对应的颜色工具类
 */
object MoodColorUtils {
    /**
     * 获取心情类型对应的颜色资源ID
     */
    fun getMoodColorResId(moodType: MoodType): Int {
        return when (moodType) {
            MoodType.VERY_HAPPY -> R.color.mood_very_happy
            MoodType.HAPPY -> R.color.mood_happy
            MoodType.NEUTRAL -> R.color.mood_neutral
            MoodType.SAD -> R.color.mood_sad
            MoodType.VERY_SAD -> R.color.mood_very_sad
        }
    }

    /**
     * 获取心情类型对应的背景颜色资源ID
     */
    fun getMoodBackgroundColorResId(moodType: MoodType): Int {
        return when (moodType) {
            MoodType.VERY_HAPPY -> R.color.mood_very_happy_bg
            MoodType.HAPPY -> R.color.mood_happy_bg
            MoodType.NEUTRAL -> R.color.mood_neutral_bg
            MoodType.SAD -> R.color.mood_sad_bg
            MoodType.VERY_SAD -> R.color.mood_very_sad_bg
        }
    }

    /**
     * 获取心情类型对应的文本颜色资源ID
     */
    fun getMoodTextColorResId(moodType: MoodType): Int {
        return getMoodColorResId(moodType)
    }
}