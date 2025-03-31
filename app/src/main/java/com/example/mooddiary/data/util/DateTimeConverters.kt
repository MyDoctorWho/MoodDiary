package com.example.mooddiary.data.util

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

/**
 * Room数据库的日期时间类型转换器
 * 用于在数据库中存储和读取kotlinx.datetime.LocalDate类型
 */
class DateTimeConverters {
    /**
     * 将LocalDate转换为String存储到数据库
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.toString() // 格式: yyyy-MM-dd
    }
    
    /**
     * 将数据库中的String转换回LocalDate
     */
    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }
}