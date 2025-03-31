package com.example.mooddiary.ui.screens.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mooddiary.MoodDiaryApplication
import com.example.mooddiary.R
import com.example.mooddiary.databinding.FragmentCalendarBinding
import com.example.mooddiary.ui.viewmodel.MoodEntryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/**
 * 日历页面Fragment，显示月历视图和心情记录
 */
class CalendarFragment : Fragment() {
    
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MoodEntryViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化ViewModel
        val application = requireActivity().application as MoodDiaryApplication
        viewModel = ViewModelProvider(
            requireActivity(),
            MoodEntryViewModel.Factory(application.repository)
        )[MoodEntryViewModel::class.java]
        
        setupToolbar()
        setupCalendarView()
        observeData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupCalendarView() {
        // 设置日历视图
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate(year, month + 1, dayOfMonth)
            viewModel.setSelectedDate(selectedDate)
            
            // 检查是否有该日期的记录，如果有则导航到详情页面
            viewLifecycleOwner.lifecycleScope.launch {
                val entry = viewModel.getMoodEntryByDate(selectedDate)
                if (entry != null) {
                    val action = CalendarFragmentDirections.actionCalendarFragmentToDetailFragment(
                        selectedDate.toString()
                    )
                    findNavController().navigate(action)
                } else {
                    // 如果没有记录，导航到编辑页面创建新记录
                    val action = CalendarFragmentDirections.actionCalendarFragmentToEditFragment(
                        selectedDate.toString()
                    )
                    findNavController().navigate(action)
                }
            }
        }
    }
    
    private fun observeData() {
        // 观察所有心情记录
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allMoodEntries.collectLatest { entries ->
                // 更新日历上的标记
                updateCalendarMarkers(entries)
                
                // 更新当前选中日期的记录
                binding.tvNoEntry.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun updateCalendarMarkers(entries: List<com.example.mooddiary.data.model.MoodEntry>) {
        // 在实际应用中，这里需要根据日历控件的API来实现标记功能
        // 这里只是一个示例，实际实现可能需要使用自定义日历控件
        
        // 清除所有标记
        // binding.calendarView.removeAllEvents()
        
        // 为每个有记录的日期添加标记
        entries.forEach { entry ->
            val date = entry.date.toJavaLocalDate()
            // 根据心情类型设置不同颜色的标记
            // binding.calendarView.addEvent(date, getColorForMoodType(entry.moodType))
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}