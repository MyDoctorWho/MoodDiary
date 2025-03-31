package com.example.mooddiary.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mooddiary.data.model.MoodEntry
import com.example.mooddiary.data.model.MoodType
import com.example.mooddiary.databinding.ItemMoodEntryBinding
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/**
 * 心情记录适配器，用于在RecyclerView中显示心情记录列表
 */
class MoodEntryAdapter(
    private val onItemClick: (MoodEntry) -> Unit
) : ListAdapter<MoodEntry, MoodEntryAdapter.MoodEntryViewHolder>(MoodEntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodEntryViewHolder {
        val binding = ItemMoodEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodEntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodEntryViewHolder, position: Int) {
        val moodEntry = getItem(position)
        holder.bind(moodEntry)
    }

    inner class MoodEntryViewHolder(private val binding: ItemMoodEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(moodEntry: MoodEntry) {
            // 设置心情表情
            binding.tvMoodEmoji.text = moodEntry.moodType.getEmojiIcon()

            // 设置标题
            binding.tvTitle.text = moodEntry.title

            // 设置日期
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
            binding.tvDate.text = dateFormatter.format(moodEntry.date.toJavaLocalDate())

            // 设置心情类型
            binding.tvMoodType.text = moodEntry.moodType.getDisplayName()

            // 设置心情类型的背景颜色
            val context = binding.root.context
            val backgroundColorResId = getMoodBackgroundColorResId(moodEntry.moodType)
            val textColorResId = getMoodTextColorResId(moodEntry.moodType)
            binding.tvMoodType.setBackgroundResource(com.example.mooddiary.R.drawable.bg_mood_type)
            binding.tvMoodType.backgroundTintList = context.getColorStateList(backgroundColorResId)
            binding.tvMoodType.setTextColor(context.getColor(textColorResId))

            // 设置内容
            binding.tvContent.text = moodEntry.content
        }

        private fun getMoodBackgroundColorResId(moodType: MoodType): Int {
            return when (moodType) {
                MoodType.VERY_HAPPY -> com.example.mooddiary.R.color.mood_very_happy_bg
                MoodType.HAPPY -> com.example.mooddiary.R.color.mood_happy_bg
                MoodType.NEUTRAL -> com.example.mooddiary.R.color.mood_neutral_bg
                MoodType.SAD -> com.example.mooddiary.R.color.mood_sad_bg
                MoodType.VERY_SAD -> com.example.mooddiary.R.color.mood_very_sad_bg
            }
        }

        private fun getMoodTextColorResId(moodType: MoodType): Int {
            return when (moodType) {
                MoodType.VERY_HAPPY -> com.example.mooddiary.R.color.mood_very_happy
                MoodType.HAPPY -> com.example.mooddiary.R.color.mood_happy
                MoodType.NEUTRAL -> com.example.mooddiary.R.color.mood_neutral
                MoodType.SAD -> com.example.mooddiary.R.color.mood_sad
                MoodType.VERY_SAD -> com.example.mooddiary.R.color.mood_very_sad
            }
        }
    }
}

/**
 * 心情记录差异比较回调
 */
class MoodEntryDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
    override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
        return oldItem == newItem
    }
}