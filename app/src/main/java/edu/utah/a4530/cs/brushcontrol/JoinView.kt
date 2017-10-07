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
 * View used to control the join type of the displayed line.
 * Created by Nico on 9/20/2017.
 */
class JoinView : View {
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

    private lateinit var leftRect: RectF
    private lateinit var middleRect: RectF
    private lateinit var rightRect: RectF

    private val borderWidth = convertDpToPixel(15f)
    private var chosen: BooleanArray = booleanArrayOf(true, false, false)
    private var onRectChosenListener: OnRectChosenListener? = null

    interface OnRectChosenListener {
        fun onRectChosen(joinView: JoinView, rects: BooleanArray) {}
    }

    fun setOnRectChosenListener(onRectChosenListener: OnRectChosenListener) {
        this.onRectChosenListener = onRectChosenListener
    }

    fun setOnRectChosenListener(onRectChosenListener: ((joinView: JoinView, rects: BooleanArray) -> Unit)) {
        this.onRectChosenListener = object : OnRectChosenListener {
            override fun onRectChosen(joinView: JoinView, rects: BooleanArray) {
                onRectChosenListener(joinView, rects)
            }
        }
    }

    fun removeOnColorChangedListener() {
        onRectChosenListener = null
    }

    private fun init() {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.strokeWidth = borderWidth
        paint.strokeCap = Paint.Cap.ROUND

        leftPaint = Paint(paint)
        middlePaint = Paint(paint)
        rightPaint = Paint(paint)

        leftPaint.strokeJoin = Paint.Join.ROUND
        leftPaint.color = Color.rgb(255, 204, 0)
        middlePaint.strokeJoin = Paint.Join.BEVEL
        rightPaint.strokeJoin = Paint.Join.MITER
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
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas !is Canvas) return

        canvas.drawRect(leftRect, leftPaint)
        canvas.drawRect(middleRect, middlePaint)
        canvas.drawRect(rightRect, rightPaint)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event !is MotionEvent)
            return super.onTouchEvent(event)
        calcRectColor(event.x, event.y)
        return super.onTouchEvent(event)
    }
}

