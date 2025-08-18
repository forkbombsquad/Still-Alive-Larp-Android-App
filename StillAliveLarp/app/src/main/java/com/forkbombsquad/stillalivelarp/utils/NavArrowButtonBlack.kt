package com.forkbombsquad.stillalivelarp.utils

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import kotlin.math.abs

class NavArrowButtonBlack(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView
    val notificationBubble: TextView

    init {
        inflate(context, R.layout.navarrowbutton_black, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)
        notificationBubble = findViewById(R.id.notificationBubble)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NavArrowButton)
        textView.text = attributes.getString(R.styleable.NavArrowButton_text)
        progressBar.isGone = !attributes.getBoolean(R.styleable.NavArrowButton_showLoading, false)
        arrow.isGone = attributes.getBoolean(R.styleable.NavArrowButton_showLoading, true)
        notificationBubble.isGone = true
        attributes.recycle()
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

    fun setNotificationBubble(text: String?) {
        notificationBubble.isGone = text == null
        notificationBubble.text = text
    }

}

class NavArrowButtonBlackBuildable(context: Context): LinearLayout(context) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_black, this)

        textView = findViewById(R.id.navarrow_text)
        progressBar = findViewById(R.id.navarrow_progressbar)
        arrow = findViewById(R.id.navarrow_arrow)
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonRed(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_red, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NavArrowButton)
        textView.text = attributes.getString(R.styleable.NavArrowButton_text)
        progressBar.isGone = !attributes.getBoolean(R.styleable.NavArrowButton_showLoading, false)
        arrow.isGone = attributes.getBoolean(R.styleable.NavArrowButton_showLoading, true)
        attributes.recycle()
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonRedBuildable(context: Context): LinearLayout(context) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_red, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonBlue(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_blue, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NavArrowButton)
        textView.text = attributes.getString(R.styleable.NavArrowButton_text)
        progressBar.isGone = !attributes.getBoolean(R.styleable.NavArrowButton_showLoading, false)
        arrow.isGone = attributes.getBoolean(R.styleable.NavArrowButton_showLoading, true)
        attributes.recycle()
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonBlueBuildable(context: Context): LinearLayout(context) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_blue, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

@SuppressLint("ClickableViewAccessibility")
class NavArrowButtonBlueSwipeable(context: Context): LinearLayout(context) {

    val container: FrameLayout
    val swipeTextView: TextView
    val navArrow: NavArrowButtonBlue
    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    private var onClick: () -> Unit = {}
    private var onSwipe: (() -> Unit)? = null

    val bgStartColor = Color.TRANSPARENT
    val bgEndColor = context.getColor(R.color.mid_red)
    val textStartColor = Color.TRANSPARENT
    val textEndColor = Color.WHITE
    val evaluator = ArgbEvaluator()

    private val swipeThreshold = 0.5f  // 50% of maxSwipe triggers delete
    private val tapThreshold = 20f
    private var downX = 0f
    private var isSwiping = false
    private var allowSwipe = true

    init {
        inflate(context, R.layout.navarrowbuttonblue_swipeable, this)

        container = findViewById(R.id.swipe_container)
        navArrow = findViewById(R.id.swipe_foreground)
        swipeTextView = findViewById(R.id.swipe_background)

        textView = findViewById(R.id.navarrow_text)
        progressBar = findViewById(R.id.navarrow_progressbar)
        arrow = findViewById(R.id.navarrow_arrow)

        container.post {
            val maxSwipe = container.width * 0.75f
            this.navArrow.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (allowSwipe && progressBar.isGone) {
                            downX = event.rawX
                            isSwiping = false
                            true
                        } else {
                            false
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (allowSwipe && progressBar.isGone) {
                            val deltaX = event.rawX - downX
                            if (deltaX < 0) { // Left swipe only
                                val absDeltaX = -deltaX
                                val progress = (absDeltaX / maxSwipe).coerceIn(0f, 1f)

                                // Move foreground view
                                navArrow.translationX = deltaX

                                // Update background color
                                val color =
                                    evaluator.evaluate(progress, bgStartColor, bgEndColor) as Int
                                swipeTextView.setBackgroundColor(color)

                                val tColor =
                                    evaluator.evaluate(progress, textStartColor, textEndColor) as Int
                                swipeTextView.setTextColor(tColor)

                                // Bold background text if past threshold
                                swipeTextView.setTypeface(
                                    null,
                                    if (progress > swipeThreshold) Typeface.BOLD else Typeface.NORMAL
                                )

                                isSwiping = true
                            }
                            true
                        } else {
                            false
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (allowSwipe && progressBar.isGone) {
                            val totalDeltaX = event.rawX - downX
                            val absDeltaX = abs(totalDeltaX)

                            if (absDeltaX < tapThreshold && progressBar.isGone) {
                                // Consider it a tap
                                this@NavArrowButtonBlueSwipeable.onClick()
                            } else {
                                val progress = (-totalDeltaX / maxSwipe).coerceIn(0f, 1f)
                                if (progress > swipeThreshold && progressBar.isGone && onSwipe != null) {
                                    // Swipe far enough — delete
                                    container.animate()
                                        .translationX(-container.width.toFloat())
                                        .alpha(0f)
                                        .setDuration(200)
                                        .withEndAction {
                                            this@NavArrowButtonBlueSwipeable.onSwipe?.let { it() }
                                        }.start()
                                } else {
                                    // Not enough swipe — animate back
                                    navArrow.animate()
                                        .translationX(0f)
                                        .setDuration(200)
                                        .withEndAction {
                                            swipeTextView.setBackgroundColor(bgStartColor)
                                            swipeTextView.setTextColor(textStartColor)
                                            swipeTextView.setTypeface(null, Typeface.NORMAL)
                                        }.start()
                                }
                            }
                            isSwiping = false
                            true
                        } else {
                            false
                        }
                    }

                    else -> {
                        false
                    }
                }
            }
        }
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.onClick = {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

    fun setCanSwipe(canSwipe: Boolean) {
        this.allowSwipe = canSwipe
    }

    fun setOnSwipe(callback: () -> Unit) {
        this.onSwipe = callback
    }

}

class NavArrowButtonGreen(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_green, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NavArrowButton)
        textView.text = attributes.getString(R.styleable.NavArrowButton_text)
        progressBar.isGone = !attributes.getBoolean(R.styleable.NavArrowButton_showLoading, false)
        arrow.isGone = attributes.getBoolean(R.styleable.NavArrowButton_showLoading, true)
        attributes.recycle()
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonGreenBuildable(context: Context): LinearLayout(context) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_green, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}