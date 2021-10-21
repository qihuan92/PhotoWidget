package com.qihuan.photowidget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.google.android.material.slider.Slider
import com.qihuan.photowidget.R
import com.qihuan.photowidget.databinding.LayoutSliderSelectionBinding

/**
 * SliderSelectionView
 * @author qi
 * @since 2021/10/8
 */
@SuppressLint("SetTextI18n")
class SliderSelectionView : LinearLayout {

    private var title: String? = null
    private var value: Float = 0f
    private var valueFrom: Float = 0f
    private var valueTo: Float = 20f
    private var stepSize: Float = 1f
    private var icon: Drawable? = null
    private var unit: String = ""

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        LayoutSliderSelectionBinding.inflate(LayoutInflater.from(context), this, true)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.SliderSelectionView).apply {
            title = getString(R.styleable.SliderSelectionView_sliderSelectionTitle)
            value = getFloat(R.styleable.SliderSelectionView_sliderSelectionValue, 0f)
            valueFrom = getFloat(R.styleable.SliderSelectionView_sliderSelectionValueFrom, 0f)
            valueTo = getFloat(R.styleable.SliderSelectionView_sliderSelectionValueTo, 20f)
            stepSize = getFloat(R.styleable.SliderSelectionView_sliderSelectionStepSize, 1f)
            icon = getDrawable(R.styleable.SliderSelectionView_sliderSelectionIcon)
            unit = getString(R.styleable.SliderSelectionView_sliderSelectionValueUnit) ?: ""
        }.recycle()
        initView()
    }

    private fun initView() {
        binding.tvTitle.text = title
        binding.slider.apply {
            value = this@SliderSelectionView.value
            valueFrom = this@SliderSelectionView.valueFrom
            valueTo = this@SliderSelectionView.valueTo
            stepSize = this@SliderSelectionView.stepSize
        }
        binding.ivIcon.isVisible = icon != null
        binding.ivIcon.setImageDrawable(icon)
        binding.tvValue.text = "${value}${unit}"
    }

    fun setTitle(value: String) {
        this.title = value
        binding.tvTitle.text = value
    }

    fun getTitle(): CharSequence = binding.tvTitle.text

    fun setValue(value: Float) {
        this.value = value
        binding.slider.value = value
        binding.tvValue.text = "${value}${unit}"
    }

    fun getValue() = binding.slider.value

    fun addOnChangeListener(listener: Slider.OnChangeListener) {
        binding.slider.addOnChangeListener(listener)
    }
}