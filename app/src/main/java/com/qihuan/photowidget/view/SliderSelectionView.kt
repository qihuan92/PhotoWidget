package com.qihuan.photowidget.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.qihuan.photowidget.R

/**
 * SliderSelectionView
 * @author qi
 * @since 2021/10/8
 */
class SliderSelectionView : LinearLayout {

    private var title: String? = null
    private var value: Float = 0f
    private var valueFrom: Float = 0f
    private var valueTo: Float = 20f
    private var stepSize: Float = 1f

    private val titleView by lazy {
        TextView(context).apply {
            setTextAppearance(android.R.style.TextAppearance_Material_Body2)
            typeface = Typeface.DEFAULT_BOLD
            text = title
        }
    }

    private val contentView by lazy {
        Slider(context).apply {
            value = this@SliderSelectionView.value
            valueFrom = this@SliderSelectionView.valueFrom
            valueTo = this@SliderSelectionView.valueTo
            stepSize = this@SliderSelectionView.stepSize
        }
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
        }.recycle()
        initView()
    }

    private fun initView() {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(titleView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        addView(contentView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun setTitle(value: String) {
        this.title = value
        titleView.text = value
    }

    fun getTitle(): CharSequence = titleView.text

    fun setValue(value: Float) {
        this.value = value
        contentView.value = value
    }

    fun getValue() = contentView.value

    fun addOnChangeListener(listener: Slider.OnChangeListener) {
        contentView.addOnChangeListener(listener)
    }
}