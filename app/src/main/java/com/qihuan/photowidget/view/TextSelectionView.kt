package com.qihuan.photowidget.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.qihuan.photowidget.R
import com.qihuan.photowidget.databinding.LayoutTextSelectionBinding

/**
 * TextSelectionView
 * @author qi
 * @since 2021/10/8
 */
class TextSelectionView : LinearLayout {

    private var title: String? = null
    private var content: String? = null
    private var icon: Drawable? = null

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        LayoutTextSelectionBinding.inflate(LayoutInflater.from(context), this, true)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.TextSelectionView).apply {
            title = getString(R.styleable.TextSelectionView_textSelectionTitle)
            content = getString(R.styleable.TextSelectionView_textSelectionContent)
            icon = getDrawable(R.styleable.TextSelectionView_textSelectionIcon)
        }.recycle()
        initView()
    }

    private fun initView() {
        binding.tvTitle.text = title
        binding.tvContent.text = content
        binding.ivIcon.isVisible = icon != null
        binding.ivIcon.setImageDrawable(icon)
    }

    fun setTitle(value: String) {
        this.title = value
        binding.tvTitle.text = value
    }

    fun getTitle(): CharSequence = binding.tvTitle.text

    fun setContent(value: String) {
        this.content = value
        binding.tvContent.text = value
    }

    fun getContent(): CharSequence = binding.tvContent.text
}