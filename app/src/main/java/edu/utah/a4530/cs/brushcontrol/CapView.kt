package edu.utah.a4530.cs.brushcontrol

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View

/**
 * View used to control the cap type of the displayed line.
 * Created by Nico on 9/21/2017.
 */
class CapView : View {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private lateinit var leftPaint: Paint
    private lateinit var middlePaint: Paint
    private lateinit var rightPaint: Paint
    private lateinit var paint: Paint
    private val borderWidth = convertDpToPixel(10f)
    private var leftX = 0f
    private var middleX = 0f
    private var rightX = 0f
    private var yTop = 0f
    private var yBottom = 0f
    private lateinit var leftRect: RectF
    private lateinit var middleRect: RectF
    private lateinit var rightRect: RectF
    private var chosen: BooleanArray = booleanArrayOf(true, false, false)
    private var onRectChosenListener: OnRectChosenListener? = null

    interface OnRectChosenListener {
        fun onRectChosen(capView: CapView, rects: BooleanArray) {}
    }

    fun setOnRectChosenListener(onRectChosenListener: OnRectChosenListener) {
        this.onRectChosenListener = onRectChosenListener
    }

    fun setOnRectChosenListener(onRectChosenListener: ((capView: CapView, rects: BooleanArray) -> Unit)) {
        this.onRectChosenListener = object : OnRectChosenListener {
            override fun onRectChosen(capView: CapView, rects: BooleanArray) {
                onRectChosenListener(capView, rects)
            }
        }
    }

    fun removeOnColorChangedListener() {
        onRectChosenListener = null
    }

    private fun init() {
        paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.strokeWidth = borderWidth
        paint.strokeCap = Paint.Cap.ROUND

        leftPaint = Paint(paint)
        middlePaint = Paint(paint)
        rightPaint = Paint(paint)

        paint.strokeWidth = convertDpToPixel(3f)

        leftPaint.color = Color.rgb(255, 204, 0)
        leftPaint.strokeCap = Paint.Cap.SQUARE
        middlePaint.strokeCap = Paint.Cap.ROUND
        rightPaint.strokeCap = Paint.Cap.BUTT
    }

    private fun invokeRectListener() {
        onRectChosenListener?.onRectChosen(this, chosen)
        invalidate()
    }

    private fun convertDpToPixel(dp: Float): Float {
        val displayMetrics = Resources.getSystem().displayMetrics
        return dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /** Changes rectangle color */
    private fun calcRectColor(x: Float, y: Float) {
        val bottomLeft = RectF(x + borderWidth / 2, y - borderWidth / 2, x + borderWidth / 2, y - borderWidth / 2)
        val topLeft = RectF(x - borderWidth / 2, y + borderWidth / 2, x - borderWidth / 2, y + borderWidth / 2)
        val topRight = RectF(x - borderWidth / 2, y - borderWidth / 2, x - borderWidth / 2, y - borderWidth / 2)
        val bottomRight = RectF(x + borderWidth / 2, y - borderWidth / 2, x + borderWidth / 2, y - borderWidth / 2)

        if ((leftRect.contains(bottomLeft) || leftRect.contains(topLeft) || leftRect.contains(topRight) || leftRect.contains(bottomRight)) && !chosen[0]) {
            leftPaint.color = Color.rgb(255, 204, 0)
            middlePaint.color = Color.WHITE
            rightPaint.color = Color.WHITE
            chosen[0] = true
            chosen[1] = false
            chosen[2] = false
            invokeRectListener()
        } else if ((middleRect.contains(bottomLeft) || middleRect.contains(topLeft) || middleRect.contains(topRight) || middleRect.contains(bottomRight)) && !chosen[1]) {
            leftPaint.color = Color.WHITE
            middlePaint.color = Color.rgb(255, 204, 0)
            rightPaint.color = Color.WHITE
            chosen[0] = false
            chosen[1] = true
            chosen[2] = false
            invokeRectListener()
        } else if ((rightRect.contains(bottomLeft) || rightRect.contains(topLeft) || rightRect.contains(topRight) || rightRect.contains(bottomRight)) && !chosen[2]) {
            leftPaint.color = Color.WHITE
            middlePaint.color = Color.WHITE
            rightPaint.color = Color.rgb(255, 204, 0)
            chosen[0] = false
            chosen[1] = false
            chosen[2] = true
            invokeRectListener()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // width unit
        val wu: Float = width.toFloat() / 10f
        // small width unit
        val swu: Float = width.toFloat() / 20f

        val top: Float = height / 2f - swu
        val bottom: Float = height / 2f + swu

        leftRect = RectF(wu + swu, top, wu * 3f - swu, bottom)
        middleRect = RectF(wu * 4f + swu, top, wu * 6f - swu, bottom)
        rightRect = RectF(wu * 7f + swu, top, wu * 9f - swu, bottom)

        leftX = wu * 2f
        middleX = wu * 5f
        rightX = wu * 8f
        yTop = top + borderWidth
        yBottom = bottom - borderWidth
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas !is Canvas) return

        //x1, y1, x1, y1, x2, y2, x2, y2, x3, y3, x3, y3

        canvas.drawLine(leftX, yTop, leftX, yBottom, leftPaint)
        canvas.drawLine(middleX, yTop, middleX, yBottom, middlePaint)
        canvas.drawLine(rightX, yTop, rightX, yBottom, rightPaint)

        canvas.drawRect(leftRect, paint)
        canvas.drawRect(middleRect, paint)
        canvas.drawRect(rightRect, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event !is MotionEvent)
            return super.onTouchEvent(event)
        calcRectColor(event.x, event.y)
        return super.onTouchEvent(event)
    }
}