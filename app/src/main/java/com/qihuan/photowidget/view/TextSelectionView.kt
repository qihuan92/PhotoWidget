package com.qihuan.photowidget.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.qihuan.photowidget.R

/**
 * TextSelectionView
 * @author qi
 * @since 2021/10/8
 */
class TextSelectionView : LinearLayout {

    private var title: String? = null
    private var content: String? = null

    private val titleView by lazy {
        TextView(context).apply {
            setTextAppearance(android.R.style.TextAppearance_Material_Body2)
            typeface = Typeface.DEFAULT_BOLD
            text = title
        }
    }

    private val contentView by lazy {
        TextView(context).apply {
            setTextAppearance(android.R.style.TextAppearance_Material_Caption)
            text = content
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.TextSelectionView).apply {
            title = getString(R.styleable.TextSelectionView_selectionTitle)
            content = getString(R.styleable.TextSelectionView_selectionContent)
        }.recycle()
        initView()
    }

    private fun initView() {
        orientation = VERTICAL
        addView(titleView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        addView(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    fun setTitle(value: String) {
        this.title = value
        titleView.text = value
    }

    fun setContent(value: String) {
        this.content = value
        contentView.text = value
    }
}