package com.example.mooddiary.ui.screens.statistics

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mooddiary.MoodDiaryApplication
import com.example.mooddiary.R
import com.example.mooddiary.data.model.MoodEntry
import com.example.mooddiary.data.model.MoodType
import com.example.mooddiary.databinding.FragmentStatisticsBinding
import com.example.mooddiary.ui.viewmodel.MoodEntryViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.FileOutputStream

/**
 * 统计页面Fragment，显示心情统计数据
 */
class StatisticsFragment : Fragment() {
    
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MoodEntryViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
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
        observeData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeData() {
        // 观察所有心情记录
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allMoodEntries.collectLatest { entries ->
                if (entries.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    
                    // 计算各种心情类型的数量
                    val moodCounts = MoodType.values().associateWith { moodType ->
                        entries.count { it.moodType == moodType }
                    }
                    
                    // 更新UI显示
                    updateMoodCountsUI(moodCounts)
                    
                    // 计算最常见的心情
                    val mostFrequentMood = moodCounts.maxByOrNull { it.value }?.key ?: MoodType.NEUTRAL
                    binding.tvMostFrequentMood.text = "最常见的心情: ${mostFrequentMood.getDisplayName()} ${mostFrequentMood.getEmojiIcon()}"
                    
                    // 计算总记录数
                    binding.tvTotalEntries.text = "总记录数: ${entries.size}"
                    
                    // 设置心情趋势图
                    setupMoodTrendChart(entries)
                    
                    // 设置心情分布图
                    setupMoodDistributionChart(moodCounts)
                    
                    // 设置心情得分
                    calculateAndDisplayMoodScore(moodCounts)
                    
                    // 设置分享按钮
                    setupShareButton()
                }
            }
        }
    }
    
    private fun updateMoodCountsUI(moodCounts: Map<MoodType, Int>) {
        // 更新各种心情类型的数量显示
        binding.tvVeryHappyCount.text = moodCounts[MoodType.VERY_HAPPY].toString()
        binding.tvHappyCount.text = moodCounts[MoodType.HAPPY].toString()
        binding.tvNeutralCount.text = moodCounts[MoodType.NEUTRAL].toString()
        binding.tvSadCount.text = moodCounts[MoodType.SAD].toString()
        binding.tvVerySadCount.text = moodCounts[MoodType.VERY_SAD].toString()
        
        // 计算总数用于百分比
        val total = moodCounts.values.sum().toFloat()
        
        // 更新进度条
        if (total > 0) {
            binding.progressVeryHappy.progress = ((moodCounts[MoodType.VERY_HAPPY] ?: 0) / total * 100).toInt()
            binding.progressHappy.progress = ((moodCounts[MoodType.HAPPY] ?: 0) / total * 100).toInt()
            binding.progressNeutral.progress = ((moodCounts[MoodType.NEUTRAL] ?: 0) / total * 100).toInt()
            binding.progressSad.progress = ((moodCounts[MoodType.SAD] ?: 0) / total * 100).toInt()
            binding.progressVerySad.progress = ((moodCounts[MoodType.VERY_SAD] ?: 0) / total * 100).toInt()
        }
    }
    
    /**
     * 设置心情趋势图
     */
    private fun setupMoodTrendChart(entries: List<MoodEntry>) {
        // 获取当前日期和一个月前的日期
        val now = Clock.System.now()
        val currentDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val oneMonthAgo = currentDate.minus(1, DateTimeUnit.MONTH)
        
        // 过滤最近一个月的记录并按日期排序
        val recentEntries = entries.filter { it.date >= oneMonthAgo && it.date <= currentDate }
            .sortedBy { it.date }
        
        // 准备图表数据
        val chartEntries = recentEntries.mapIndexed { index, entry ->
            // 将心情类型转换为数值（1-5）
            val moodValue = when (entry.moodType) {
                MoodType.VERY_HAPPY -> 5f
                MoodType.HAPPY -> 4f
                MoodType.NEUTRAL -> 3f
                MoodType.SAD -> 2f
                MoodType.VERY_SAD -> 1f
            }
            Entry(index.toFloat(), moodValue)
        }
        
        // 如果没有数据，添加一个默认值
        val dataSet = if (chartEntries.isEmpty()) {
            val defaultEntries = listOf(Entry(0f, 3f))
            LineDataSet(defaultEntries, "心情变化")
        } else {
            LineDataSet(chartEntries, "心情变化")
        }
        
        // 设置图表样式
        dataSet.apply {
            color = resources.getColor(R.color.mood_neutral, null)
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
            setCircleColor(resources.getColor(R.color.mood_happy, null))
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            
            // 设置渐变填充
            setDrawFilled(true)
            fillDrawable = resources.getDrawable(R.drawable.bg_chart_gradient, null)
        }
        
        // 设置X轴标签
        val xLabels = recentEntries.map { it.date.dayOfMonth.toString() }
        val lineData = LineData(dataSet)
        
        // 配置图表
        binding.moodTrendChart.apply {
            data = lineData
            description.isEnabled = false
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                form = Legend.LegendForm.LINE
            }
            
            // 配置X轴
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                if (xLabels.isNotEmpty()) {
                    valueFormatter = IndexAxisValueFormatter(xLabels)
                }
                setDrawGridLines(false)
            }
            
            // 配置Y轴
            axisLeft.apply {
                axisMinimum = 0.5f
                axisMaximum = 5.5f
                granularity = 1f
                setDrawGridLines(true)
            }
            axisRight.isEnabled = false
            
            // 刷新图表
            invalidate()
        }
    }
    
    /**
     * 设置心情分布饼图
     */
    private fun setupMoodDistributionChart(moodCounts: Map<MoodType, Int>) {
        // 准备饼图数据
        val pieEntries = moodCounts.map { (moodType, count) ->
            PieEntry(count.toFloat(), moodType.getEmojiIcon())
        }
        
        // 设置颜色
        val colors = listOf(
            resources.getColor(R.color.mood_very_happy, null),
            resources.getColor(R.color.mood_happy, null),
            resources.getColor(R.color.mood_neutral, null),
            resources.getColor(R.color.mood_sad, null),
            resources.getColor(R.color.mood_very_sad, null)
        )
        
        // 创建数据集
        val dataSet = PieDataSet(pieEntries, "心情分布")
        dataSet.apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
            selectionShift = 5f
        }
        
        // 创建饼图数据
        val pieData = PieData(dataSet)
        
        // 配置饼图
        binding.moodDistributionChart.apply {
            data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "心情分布"
            setCenterTextSize(16f)
            setUsePercentValues(false)
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            
            // 刷新图表
            invalidate()
        }
    }
    
    /**
     * 计算并显示心情得分
     */
    private fun calculateAndDisplayMoodScore(moodCounts: Map<MoodType, Int>) {
        // 计算心情得分（加权平均）
        val totalCount = moodCounts.values.sum()
        if (totalCount > 0) {
            val weightedSum = moodCounts[MoodType.VERY_HAPPY]!! * 100 +
                    moodCounts[MoodType.HAPPY]!! * 75 +
                    moodCounts[MoodType.NEUTRAL]!! * 50 +
                    moodCounts[MoodType.SAD]!! * 25 +
                    moodCounts[MoodType.VERY_SAD]!! * 0
            
            val score = weightedSum / totalCount
            binding.tvMoodScore.text = score.toString()
            
            // 创建心情指数仪表盘
            setupMoodGaugeChart(score)
        } else {
            binding.tvMoodScore.text = "--"
        }
    }
    
    /**
     * 设置分享按钮
     */
    private fun setupShareButton() {
        binding.btnShareTrend.setOnClickListener {
            // 获取图表视图
            val chart = binding.moodTrendChart
            
            // 创建位图
            chart.isDrawingCacheEnabled = true
            chart.buildDrawingCache()
            val bitmap = chart.drawingCache
            
            try {
                // 保存位图到临时文件
                val cachePath = File(requireContext().cacheDir, "images")
                cachePath.mkdirs()
                
                val stream = FileOutputStream("$cachePath/shared_image.png")
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()
                
                val imagePath = File(cachePath, "shared_image.png")
                val contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.mooddiary.fileprovider",
                    imagePath
                )
                
                // 创建分享意图
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                // 启动分享
                startActivity(Intent.createChooser(shareIntent, "分享心情趋势图"))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 清理绘图缓存
                chart.isDrawingCacheEnabled = false
            }
        }
    }
    
    /**
     * 设置心情指数仪表盘
     */
    private fun setupMoodGaugeChart(score: Int) {
        // 如果布局中有MoodGaugeChart组件，则设置心情得分
        val moodGaugeView = binding.moodGaugeChart
        moodGaugeView.moodScore = score
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}