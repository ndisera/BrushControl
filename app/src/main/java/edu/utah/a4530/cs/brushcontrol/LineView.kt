package edu.utah.a4530.cs.brushcontrol

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.util.DisplayMetrics



/**
 * Represents the displayed line that the other controls influence.
 * Created by Nico on 9/17/2017.
 */
class LineView : View {
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

    private lateinit var path: Path
    private lateinit var paint: Paint
    private lateinit var borderPath: Path
    private lateinit var borderPaint: Paint

    var currentColor: Int = 0
        set(newColor) {
            field = newColor
            invalidate()
        }

    var brushWidth: Float = 500F
        set(value) {
            field = value
            invalidate()
        }

    var joinType: Paint.Join = Paint.Join.ROUND
        set(value) {
            field = value
            invalidate()
        }

    var capType: Paint.Cap = Paint.Cap.SQUARE
        set(value) {
            field = value
            invalidate()
        }

    private fun init() {
        paint = Paint()
        borderPaint = Paint()
        path = Path()
        borderPath = Path()
        paint.style = Paint.Style.STROKE
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = convertDpToPixel(3f)
        borderPaint.strokeJoin = Paint.Join.ROUND
        borderPaint.color = Color.WHITE
    }

    private fun convertDpToPixel(dp: Float): Float {
        val displayMetrics = Resources.getSystem().displayMetrics
        return dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        // Sets up default line path

        val x1 = width / 8f
        val x5 = width - x1
        val x3 = (x1 + x5) / 2f
        //val x2 = x1
        val x4 = (x3 + x5) / 2f
        val y5 = height / 2f
        val y1 = height / 10f * 3f
        val y3 = height / 10f * 3.5f
        //y2 = y4
        val y4 = height - height / 10f * 3.5f

        path.moveTo(x1, y1)
        path.lineTo(x1, y4)
        path.lineTo(x3, y3)
        path.lineTo(x4, y4)
        path.lineTo(x5, y5)

        val dpUnit = convertDpToPixel(2.5f)

        // Sets up white border

        borderPath.moveTo(dpUnit, dpUnit)
        borderPath.lineTo(width.toFloat() - dpUnit, dpUnit)
        borderPath.lineTo(width.toFloat() - dpUnit, height.toFloat() - dpUnit)
        borderPath.lineTo(dpUnit, height.toFloat() - dpUnit)
        borderPath.lineTo(dpUnit, dpUnit)
        // This extra part is just needed to get the top left corner join
        borderPath.lineTo(dpUnit + convertDpToPixel(1f), dpUnit)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas !is Canvas) return

        paint.strokeWidth = convertDpToPixel(brushWidth / 20f)
        paint.color = currentColor
        paint.strokeJoin = joinType
        paint.strokeCap = capType

        canvas.drawPath(path, paint)
        canvas.drawPath(borderPath, borderPaint)
    }
}