package com.example.mooddiary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mooddiary.data.model.MoodEntry
import com.example.mooddiary.data.model.MoodType
import com.example.mooddiary.data.repository.MoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 心情日记视图模型
 * 负责处理UI层的业务逻辑，并与数据仓库交互
 */
class MoodEntryViewModel(private val repository: MoodEntryRepository) : ViewModel() {
    
    // 所有心情记录
    val allMoodEntries: Flow<List<MoodEntry>> = repository.getAllMoodEntries()
    
    // 当前选中的日期
    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    // 当前选中日期的心情记录
    private val _currentMoodEntry = MutableStateFlow<MoodEntry?>(null)
    val currentMoodEntry: StateFlow<MoodEntry?> = _currentMoodEntry.asStateFlow()
    
    // 编辑状态
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()
    
    // 临时标题
    private val _tempTitle = MutableStateFlow("")
    val tempTitle: StateFlow<String> = _tempTitle.asStateFlow()
    
    // 临时内容
    private val _tempContent = MutableStateFlow("")
    val tempContent: StateFlow<String> = _tempContent.asStateFlow()
    
    // 临时心情类型
    private val _tempMoodType = MutableStateFlow(MoodType.NEUTRAL)
    val tempMoodType: StateFlow<MoodType> = _tempMoodType.asStateFlow()
    
    /**
     * 初始化函数，加载当前日期的心情记录
     */
    init {
        loadMoodEntryForSelectedDate()
    }
    
    /**
     * 获取当前日期
     */
    private fun getCurrentDate(): LocalDate {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.date
    }
    
    /**
     * 设置选中的日期
     */
    fun setSelectedDate(date: LocalDate) {
        if (_selectedDate.value != date) {
            _selectedDate.value = date
            loadMoodEntryForSelectedDate()
        }
    }
    
    /**
     * 加载选中日期的心情记录
     */
    private fun loadMoodEntryForSelectedDate() {
        viewModelScope.launch {
            val entry = repository.getMoodEntryByDate(_selectedDate.value)
            _currentMoodEntry.value = entry
            
            // 如果有记录，初始化临时数据
            if (entry != null) {
                _tempTitle.value = entry.title
                _tempContent.value = entry.content
                _tempMoodType.value = entry.moodType
            } else {
                // 如果没有记录，重置临时数据
                resetTempData()
            }
        }
    }
    
    /**
     * 开始编辑
     */
    fun startEditing() {
        _isEditing.value = true
        
        // 如果当前没有记录，初始化临时数据
        if (_currentMoodEntry.value == null) {
            resetTempData()
        }
    }
    
    /**
     * 取消编辑
     */
    fun cancelEditing() {
        _isEditing.value = false
        
        // 恢复原始数据
        val entry = _currentMoodEntry.value
        if (entry != null) {
            _tempTitle.value = entry.title
            _tempContent.value = entry.content
            _tempMoodType.value = entry.moodType
        } else {
            resetTempData()
        }
    }
    
    /**
     * 重置临时数据
     */
    private fun resetTempData() {
        _tempTitle.value = ""
        _tempContent.value = ""
        _tempMoodType.value = MoodType.NEUTRAL
    }
    
    /**
     * 更新临时标题
     */
    fun updateTempTitle(title: String) {
        _tempTitle.value = title
    }
    
    /**
     * 更新临时内容
     */
    fun updateTempContent(content: String) {
        _tempContent.value = content
    }
    
    /**
     * 更新临时心情类型
     */
    fun updateTempMoodType(moodType: MoodType) {
        _tempMoodType.value = moodType
    }
    
    /**
     * 保存心情记录
     */
    fun saveMoodEntry() {
        viewModelScope.launch {
            val currentEntry = _currentMoodEntry.value
            val now = System.currentTimeMillis()
            
            if (currentEntry != null) {
                // 更新现有记录
                val updatedEntry = currentEntry.copy(
                    title = _tempTitle.value,
                    content = _tempContent.value,
                    moodType = _tempMoodType.value,
                    updatedAt = now
                )
                repository.updateMoodEntry(updatedEntry)
                _currentMoodEntry.value = updatedEntry
            } else {
                // 创建新记录
                val newEntry = MoodEntry(
                    date = _selectedDate.value,
                    title = _tempTitle.value,
                    content = _tempContent.value,
                    moodType = _tempMoodType.value,
                    createdAt = now,
                    updatedAt = now
                )
                val id = repository.insertMoodEntry(newEntry)
                _currentMoodEntry.value = newEntry.copy(id = id)
            }
            
            // 退出编辑模式
            _isEditing.value = false
        }
    }
    
    /**
     * 根据日期获取心情记录
     */
    suspend fun getMoodEntryByDate(date: LocalDate): MoodEntry? {
        return repository.getMoodEntryByDate(date)
    }
    
    /**
     * 删除心情记录
     */
    fun deleteMoodEntry() {
        viewModelScope.launch {
            val currentEntry = _currentMoodEntry.value
            if (currentEntry != null) {
                repository.deleteMoodEntry(currentEntry)
                _currentMoodEntry.value = null
                resetTempData()
            }
        }
    }
    
    /**
     * 获取最近的心情记录
     */
    fun getRecentMoodEntries(limit: Int): Flow<List<MoodEntry>> {
        return repository.getRecentMoodEntries(limit)
    }
    
    /**
     * 根据心情类型获取记录
     */
    fun getMoodEntriesByType(moodType: MoodType): Flow<List<MoodEntry>> {
        return repository.getMoodEntriesByType(moodType)
    }
    
    /**
     * 获取日期范围内的记录
     */
    fun getMoodEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<MoodEntry>> {
        return repository.getMoodEntriesBetweenDates(startDate, endDate)
    }
    
    /**
     * 视图模型工厂
     */
    class Factory(private val repository: MoodEntryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoodEntryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MoodEntryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}