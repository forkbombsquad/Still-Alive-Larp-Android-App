package com.forkbombsquad.stillalivelarp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import android.view.View
import android.widget.OverScroller
import androidx.appcompat.widget.AppCompatImageView
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.SkillCategoryModel
import com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree.SkillGrid
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.Shapes
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlin.math.max

class NativeSkillTreeActivity : NoStatusBarActivity() {

    private lateinit var img: TouchImageView
    private lateinit var paint: Paint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_skill_tree)
        setupView()
    }

    private var scaleFactor = 1f
    private var minScale = 0.1f
    private var maxScale = 100f

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f
    private var activePointerId = -1

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {
        img = findViewById(R.id.nativeskilltree_img)

        paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 10F
        img.invalidate()
        renderSkills()
    }

    private fun renderSkills() {
        img.updateDrawables(
            SkillGrid(
                DataManager.shared.skills!!,
                DataManager.shared.skillCategories!!.asList()
            )
        )
        img.invalidate()
    }

}

@SuppressLint("ClickableViewAccessibility")
class TouchImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs),
    ScaleGestureDetector.OnScaleGestureListener {

    private var scaleFactor = 1f
    private val minScale = 0.05f
    private val maxScale = 20f

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private val scroller = OverScroller(context)
    private val velocityTracker =  VelocityTracker.obtain()

    private var lastFlingX = 0
    private var lastFlingY = 0

    private val flingRunnable = object : Runnable {
        override fun run() {
            if (scroller.computeScrollOffset()) {
                val newX = scroller.currX
                val newY = scroller.currY

                // Compute the difference (delta)
                val dx = (newX - lastFlingX).toFloat()
                val dy = (newY - lastFlingY).toFloat()

                // Update last positions
                lastFlingX = newX
                lastFlingY = newY

                // Apply only the delta movement
                matrix.postTranslate(dx, dy)
                imageMatrix = matrix
                invalidate()

                // Continue running until the scroller finishes
                postOnAnimation(this)
            }
        }
    }

    // GestureDetector for tap events
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Transform the tap coordinates into canvas space
            val inverseMatrix = Matrix()
            matrix.invert(inverseMatrix)
            val touchPoint = floatArrayOf(e.x, e.y)
            inverseMatrix.mapPoints(touchPoint)
            val canvasX = touchPoint[0]
            val canvasY = touchPoint[1]
            onTapEvent(canvasX, canvasY)
            return true
        }
    })

    private var mode = NONE
    private var startX = 0f
    private var startY = 0f

    private var skillGrid: SkillGrid? = null

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // Set touch listener for gestures, taps, panning and zooming
        setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    // Stop fling when a new touch starts
                    scroller.forceFinished(true)

                    velocityTracker.clear()
                    velocityTracker.addMovement(event)

                    savedMatrix.set(matrix)
                    startX = event.x
                    startY = event.y
                    mode = if (event.pointerCount > 1) ZOOM else DRAG  // Correctly detect zoom mode
                }

                MotionEvent.ACTION_MOVE -> {
                    velocityTracker.addMovement(event)

                    if (mode == DRAG) {
                        val dx = event.x - startX
                        val dy = event.y - startY
                        matrix.set(savedMatrix)
                        matrix.postTranslate(dx, dy)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    velocityTracker.addMovement(event)
                    velocityTracker.computeCurrentVelocity(1000)

                    val velocityX = velocityTracker.xVelocity
                    val velocityY = velocityTracker.yVelocity

                    if (Math.abs(velocityX) > 50 || Math.abs(velocityY) > 50) {
                        startFling(-velocityX.toInt(), -velocityY.toInt())  // Start fling correctly
                    }

                    mode = NONE
                }
            }

            imageMatrix = matrix
            invalidate()
            true
        }

    }

    private fun startFling(velocityX: Int, velocityY: Int) {
        lastFlingX = 0
        lastFlingY = 0

        scroller.fling(
            0, 0,  // Start at (0,0) since we track deltas
            -velocityX, -velocityY,  // Invert velocity to fix direction
            Int.MIN_VALUE, Int.MAX_VALUE,  // X bounds
            Int.MIN_VALUE, Int.MAX_VALUE   // Y bounds
        )

        postOnAnimation(flingRunnable)
    }


    // Callback function when a tap is confirmed
    private fun onTapEvent(canvasX: Float, canvasY: Float) {
        // Let the SkillGrid handle the tap event.
        // You can further test the (canvasX, canvasY) position if needed.
        skillGrid?.handleTap(canvasX, canvasY)
        invalidate()
    }

    fun updateDrawables(skillGrid: SkillGrid) {
        this.skillGrid = skillGrid
        invalidate()
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        scroller.forceFinished(true)  // Stop fling when pinch-zoom starts

        val scaleChange = (detector.scaleFactor - 1) * 0.8f  // Reduce sensitivity slightly
        val scale = 1 + scaleChange

        scaleFactor *= scale
        scaleFactor = scaleFactor.coerceIn(minScale, maxScale)

        // Apply scaling **around the pinch center**
        matrix.postScale(scale, scale, detector.focusX, detector.focusY)

        imageMatrix = matrix
        invalidate()
        return true
    }


    private fun getScaleFromMatrix(matrix: Matrix): Float {
        val values = FloatArray(9)
        matrix.getValues(values)
        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]  // Extracts scale from the matrix
        return (scaleX + scaleY) / 2  // Average the scale (handles non-uniform scaling)
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true
    override fun onScaleEnd(detector: ScaleGestureDetector) {}

    // Override onDraw to draw directly to the canvas
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Extract the real scale from the matrix
        val scale = getScaleFromMatrix(matrix)

        // Apply transformations
        canvas.concat(matrix)

        // Pass the actual scale to skillGrid.draw()
        skillGrid?.draw(canvas, scale)
    }
}
