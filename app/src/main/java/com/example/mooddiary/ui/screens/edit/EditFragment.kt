package com.example.mooddiary.ui.screens.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mooddiary.MoodDiaryApplication
import com.example.mooddiary.R
import com.example.mooddiary.data.model.MoodType
import com.example.mooddiary.databinding.FragmentEditBinding
import com.example.mooddiary.ui.viewmodel.MoodEntryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/**
 * 心情编辑Fragment，用于创建或编辑心情记录
 */
class EditFragment : Fragment() {
    
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MoodEntryViewModel
    private lateinit var date: LocalDate
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 获取传递的日期参数
        arguments?.let {
            val dateString = EditFragmentArgs.fromBundle(it).date
            date = LocalDate.parse(dateString)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
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
        
        // 设置当前选中的日期并开始编辑
        viewModel.setSelectedDate(date)
        viewModel.startEditing()
        
        setupToolbar()
        setupMoodTypeRadioGroup()
        setupSaveButton()
        setupDeleteButton()
        observeData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // 取消编辑并返回
            viewModel.cancelEditing()
            findNavController().navigateUp()
        }
    }
    
    private fun setupMoodTypeRadioGroup() {
        // 清除现有的选项
        binding.rgMoodType.removeAllViews()
        
        // 为每种心情类型创建单选按钮
        MoodType.values().forEach { moodType ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = "${moodType.getEmojiIcon()} ${moodType.getDisplayName()}"
                textSize = 16f
                setPadding(24, 16, 24, 16)
            }
            
            binding.rgMoodType.addView(radioButton)
            
            // 设置点击监听器
            radioButton.setOnClickListener {
                viewModel.updateTempMoodType(moodType)
            }
        }
    }
    
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            // 获取输入的标题和内容
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            
            // 更新临时数据
            viewModel.updateTempTitle(title)
            viewModel.updateTempContent(content)
            
            // 保存记录
            viewModel.saveMoodEntry()
            
            // 返回上一页
            findNavController().navigateUp()
            
            // 显示保存成功提示
            Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupDeleteButton() {
        binding.btnDelete.setOnClickListener {
            // 显示确认对话框
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除记录")
                .setMessage("确定要删除这条记录吗？此操作不可撤销。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除") { _, _ ->
                    // 删除记录
                    viewModel.deleteMoodEntry()
                    
                    // 返回上一页
                    findNavController().navigateUp()
                    
                    // 显示删除成功提示
                    Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }
    
    private fun observeData() {
        // 设置日期显示
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE")
        binding.tvDate.text = dateFormatter.format(date.toJavaLocalDate())
        
        // 观察临时数据变化
        viewLifecycleOwner.lifecycleScope.launch {
            // 观察标题
            viewModel.tempTitle.collectLatest { title ->
                if (binding.etTitle.text.toString() != title) {
                    binding.etTitle.setText(title)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // 观察内容
            viewModel.tempContent.collectLatest { content ->
                if (binding.etContent.text.toString() != content) {
                    binding.etContent.setText(content)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // 观察心情类型
            viewModel.tempMoodType.collectLatest { moodType ->
                updateSelectedMoodType(moodType)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // 观察当前记录，决定是否显示删除按钮
            viewModel.currentMoodEntry.collectLatest { entry ->
                binding.btnDelete.visibility = if (entry != null) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun updateSelectedMoodType(moodType: MoodType) {
        // 遍历单选按钮组，找到对应的心情类型并选中
        for (i in 0 until binding.rgMoodType.childCount) {
            val radioButton = binding.rgMoodType.getChildAt(i) as RadioButton
            val text = radioButton.text.toString()
            
            if (text.contains(moodType.getDisplayName())) {
                radioButton.isChecked = true
                break
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}