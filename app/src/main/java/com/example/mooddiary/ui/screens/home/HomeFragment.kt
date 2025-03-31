package com.example.mooddiary.ui.screens.home

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mooddiary.MoodDiaryApplication
import com.example.mooddiary.R
import com.example.mooddiary.data.model.MoodEntry
import com.example.mooddiary.databinding.FragmentHomeBinding
import com.example.mooddiary.ui.adapters.MoodEntryAdapter
import com.example.mooddiary.ui.viewmodel.MoodEntryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 主页Fragment，显示心情记录列表
 */
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MoodEntryViewModel
    private lateinit var adapter: MoodEntryAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_calendar -> {
                    findNavController().navigate(R.id.action_homeFragment_to_calendarFragment)
                    true
                }
                R.id.action_statistics -> {
                    findNavController().navigate(R.id.action_homeFragment_to_statisticsFragment)
                    true
                }
                else -> false
            }
        }
        
        // 添加菜单项
        binding.toolbar.inflateMenu(R.menu.menu_home)
    }
    
    private fun setupRecyclerView() {
        adapter = MoodEntryAdapter { moodEntry ->
            // 点击条目时导航到详情页面
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                moodEntry.date.toString()
            )
            findNavController().navigate(action)
        }
        
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun setupFab() {
        binding.fabAddEntry.setOnClickListener {
            // 获取当前日期
            val now = Clock.System.now()
            val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
            val currentDate = localDateTime.date
            
            // 设置当前选中的日期
            viewModel.setSelectedDate(currentDate)
            
            // 导航到详情页面
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                currentDate.toString()
            )
            findNavController().navigate(action)
        }
    }
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allMoodEntries.collectLatest { entries ->
                updateUI(entries)
            }
        }
    }
    
    private fun updateUI(entries: List<MoodEntry>) {
        adapter.submitList(entries)
        
        // 显示或隐藏空视图
        binding.emptyView.isVisible = entries.isEmpty()
        binding.recyclerView.isVisible = entries.isNotEmpty()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}