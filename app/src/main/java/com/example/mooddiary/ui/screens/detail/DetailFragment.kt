package com.example.mooddiary.ui.screens.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mooddiary.MoodDiaryApplication
import com.example.mooddiary.R
import com.example.mooddiary.data.model.MoodEntry
import com.example.mooddiary.databinding.FragmentDetailBinding
import com.example.mooddiary.ui.viewmodel.MoodEntryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/**
 * 心情详情Fragment，显示单个心情记录的详细信息
 */
class DetailFragment : Fragment() {
    
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MoodEntryViewModel
    private lateinit var date: LocalDate
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        
        // 获取传递的日期参数
        arguments?.let {
            val dateString = DetailFragmentArgs.fromBundle(it).date
            date = LocalDate.parse(dateString)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化ViewModel
        val application = requireActivity().application as MoodDiaryApplication
        viewModel = ViewModelProvider(
            this,
            MoodEntryViewModel.Factory(application.repository)
        )[MoodEntryViewModel::class.java]
        
        // 设置当前选中的日期
        viewModel.setSelectedDate(date)
        
        setupToolbar()
        setupFab()
        observeData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupFab() {
        binding.fabEdit.setOnClickListener {
            // 导航到编辑页面
            val action = DetailFragmentDirections.actionDetailFragmentToEditFragment(date.toString())
            findNavController().navigate(action)
        }
    }
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentMoodEntry.collectLatest { moodEntry ->
                updateUI(moodEntry)
            }
        }
    }
    
    private fun updateUI(moodEntry: MoodEntry?) {
        if (moodEntry != null) {
            // 显示记录内容
            binding.emptyView.isVisible = false
            
            // 设置日期
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE")
            binding.tvDate.text = dateFormatter.format(date.toJavaLocalDate())
            
            // 设置心情表情
            binding.tvMoodEmoji.text = moodEntry.moodType.getEmojiIcon()
            
            // 设置心情类型
            binding.tvMoodType.text = moodEntry.moodType.getDisplayName()
            
            // 设置心情类型的背景颜色
            val backgroundColorResId = getMoodBackgroundColorResId(moodEntry.moodType)
            val textColorResId = getMoodTextColorResId(moodEntry.moodType)
            binding.tvMoodType.setBackgroundResource(R.drawable.bg_mood_type)
            binding.tvMoodType.backgroundTintList = requireContext().getColorStateList(backgroundColorResId)
            binding.tvMoodType.setTextColor(requireContext().getColor(textColorResId))
            
            // 设置标题和内容
            binding.tvTitle.text = moodEntry.title
            binding.tvContent.text = moodEntry.content
        } else {
            // 显示空视图
            binding.emptyView.isVisible = true
        }
    }
    
    private fun getMoodBackgroundColorResId(moodType: com.example.mooddiary.data.model.MoodType): Int {
        return when (moodType) {
            com.example.mooddiary.data.model.MoodType.VERY_HAPPY -> R.color.mood_very_happy_bg
            com.example.mooddiary.data.model.MoodType.HAPPY -> R.color.mood_happy_bg
            com.example.mooddiary.data.model.MoodType.NEUTRAL -> R.color.mood_neutral_bg
            com.example.mooddiary.data.model.MoodType.SAD -> R.color.mood_sad_bg
            com.example.mooddiary.data.model.MoodType.VERY_SAD -> R.color.mood_very_sad_bg
        }
    }
    
    private fun getMoodTextColorResId(moodType: com.example.mooddiary.data.model.MoodType): Int {
        return when (moodType) {
            com.example.mooddiary.data.model.MoodType.VERY_HAPPY -> R.color.mood_very_happy
            com.example.mooddiary.data.model.MoodType.HAPPY -> R.color.mood_happy
            com.example.mooddiary.data.model.MoodType.NEUTRAL -> R.color.mood_neutral
            com.example.mooddiary.data.model.MoodType.SAD -> R.color.mood_sad
            com.example.mooddiary.data.model.MoodType.VERY_SAD -> R.color.mood_very_sad
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}