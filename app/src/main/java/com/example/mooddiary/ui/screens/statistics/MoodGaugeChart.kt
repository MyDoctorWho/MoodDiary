package com.example.mooddiary.ui.screens.statistics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.mooddiary.R

/**
 * 自定义心情指数仪表盘视图
 * 根据心情得分(0-100)显示不同颜色的仪表盘
 */
class MoodGaugeChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // 心情得分(0-100)
    var moodScore: Int = 50
        set(value) {
            field = value
            invalidate()
        }
    
    // 仪表盘颜色
    private val veryHappyColor: Int = context.getColor(R.color.mood_very_happy)
    private val happyColor: Int = context.getColor(R.color.mood_happy)
    private val neutralColor: Int = context.getColor(R.color.mood_neutral)
    private val sadColor: Int = context.getColor(R.color.mood_sad)
    private val verySadColor: Int = context.getColor(R.color.mood_very_sad)
    
    // 表情图标
    private val emojiVeryHappy = "😄"
    private val emojiHappy = "🙂"
    private val emojiNeutral = "😐"
    private val emojiSad = "😔"
    private val emojiVerySad = "😢"
    
    init {
        textPaint.apply {
            color = Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height * 0.6f
        val radius = Math.min(width, height) * 0.4f
        
        // 绘制仪表盘背景
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.2f
        paint.color = Color.LTGRAY
        
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        
        // 绘制仪表盘背景(灰色半圆)
        canvas.drawArc(rectF, 180f, 180f, false, paint)
        
        // 根据得分计算角度(0-100分映射到180-0度)
        val sweepAngle = 180f * (1 - moodScore / 100f)
        
        // 根据得分选择颜色
        paint.color = when {
            moodScore >= 80 -> veryHappyColor
            moodScore >= 60 -> happyColor
            moodScore >= 40 -> neutralColor
            moodScore >= 20 -> sadColor
            else -> verySadColor
        }
        
        // 绘制得分弧
        canvas.drawArc(rectF, 180f, sweepAngle, false, paint)
        
        // 绘制中心得分文本
        textPaint.textSize = radius * 0.5f
        canvas.drawText(moodScore.toString(), centerX, centerY, textPaint)
        
        // 绘制表情图标
        textPaint.textSize = radius * 0.4f
        val emoji = when {
            moodScore >= 80 -> emojiVeryHappy
            moodScore >= 60 -> emojiHappy
            moodScore >= 40 -> emojiNeutral
            moodScore >= 20 -> emojiSad
            else -> emojiVerySad
        }
        canvas.drawText(emoji, centerX, centerY + radius * 0.6f, textPaint)
        
        // 绘制刻度
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 2f
        
        // 绘制最小值和最大值标签
        textPaint.textSize = radius * 0.25f
        canvas.drawText("0", centerX - radius, centerY + radius * 0.3f, textPaint)
        canvas.drawText("100", centerX + radius, centerY + radius * 0.3f, textPaint)
    }
}