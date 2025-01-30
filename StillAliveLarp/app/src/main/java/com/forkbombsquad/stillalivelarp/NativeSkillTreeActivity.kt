package com.forkbombsquad.stillalivelarp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
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
class TouchImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs), ScaleGestureDetector.OnScaleGestureListener {
    private var scaleFactor = 1f
    private val minScale = 0.05f
    private val maxScale = 20f

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private var mode = NONE
    private var startX = 0f
    private var startY = 0f

    // Paint object for drawing on the Canvas
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private var rectLeft = 0f
    private var rectTop = 0f
    private var rectRight = 0f
    private var rectBottom = 0f

    private var skillGrid: SkillGrid? = null

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // Set touch listener for gestures
        setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    savedMatrix.set(matrix)
                    startX = event.x
                    startY = event.y
                    mode = DRAG
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mode == DRAG) {
                        val dx = event.x - startX
                        val dy = event.y - startY
                        matrix.set(savedMatrix)
                        matrix.postTranslate(dx, dy)
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    mode = ZOOM
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }
            }

            imageMatrix = matrix
            invalidate()
            true
        }
    }

    fun updateDrawables(skillGrid: SkillGrid) {
        this.skillGrid = skillGrid
        invalidate()
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scaleChange = (detector.scaleFactor - 1) * 1f  // Reduce scale sensitivity
        val scale = 1 + scaleChange

        scaleFactor *= scale
        scaleFactor = scaleFactor.coerceIn(minScale, maxScale)

        // Scale around the pinch center (focusX, focusY)
        matrix.postScale(scale, scale, detector.focusX, detector.focusY)

        imageMatrix = matrix
        invalidate()
        return true
    }

    // Function to draw a rectangle on the Canvas
    fun drawRect(left: Float, top: Float, right: Float, bottom: Float) {
        rectLeft = left
        rectTop = top
        rectRight = right
        rectBottom = bottom
        invalidate() // Redraw ImageView
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