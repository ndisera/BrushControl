package edu.utah.a4530.cs.brushcontrol

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader.TileMode
import android.graphics.SweepGradient
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Color Picker View used to choose the color of the displayed line.
 * The majority of this was created from a Java open source project on GitHub:
 * https://github.com/chiralcode/Android-Color-Picker/blob/master/src/com/chiralcode/colorpicker/ColorPicker.java
 *
 * Created by Nico on 9/14/2017.
 */
class ColorPicker: View {
    constructor(context: Context?) : super(context) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes){
        init()
    }

    /**
     * Customizable display parameters (in percents)
     */
    private val paramOuterPadding = 1 // outer padding of the whole color picker view
    private val paramInnerPadding = 0 // distance between value slider wheel and inner color wheel
    private val paramValueSliderWidth = 1 // width of the value slider

    private lateinit var colorWheelPaint: Paint
    private lateinit var colorViewPaint: Paint
    private lateinit var colorPointerPaint: Paint
    private lateinit var colorPointerCoords: RectF
    private lateinit var valuePointerPaint: Paint
    private lateinit var outerWheelRect: RectF
    private lateinit var innerWheelRect: RectF
    private lateinit var colorViewPath: Path
    private lateinit var colorWheelBitmap: Bitmap
    private var centerX: Int = 0
    private var centerY: Int = 0
    private var valueSliderWidth: Int = 0
    private var innerPadding: Int = 0
    private var outerPadding: Int = 0
    private var outerWheelRadius: Int = 0
    private var innerWheelRadius: Int = 0
    private var colorWheelRadius: Int = 0
    private var gradientRotationMatrix = Matrix()

    /** Currently selected color  */
    private var colorHSV = floatArrayOf(0f, 0f, 1f)
    private var currentColor : Int = 0
        set(value){
            field = value
            onColorChangedListener?.onColorChanged(this, field)
            invalidate()
        }

    private var onColorChangedListener: OnColorChangedListener? = null

    interface OnColorChangedListener {
        fun onColorChanged(colorPicker: ColorPicker, currentColor: Int){}
    }

    fun setOnColorChangedListener(onColorChangedListener: OnColorChangedListener){
        this.onColorChangedListener = onColorChangedListener
    }

    fun setOnColorChangedListener(onColorChangedListener: ((colorPicker: ColorPicker, currentColor: Int) -> Unit)) {
        this.onColorChangedListener = object: OnColorChangedListener {
            override fun onColorChanged(colorPicker: ColorPicker, currentColor: Int) {
                onColorChangedListener(colorPicker, currentColor)
            }
        }
    }

    fun removeOnColorChangedListener() {
        onColorChangedListener = null
    }


    private fun init() {

        colorPointerPaint = Paint()
        colorPointerPaint.style = Style.STROKE
        colorPointerPaint.strokeWidth = 5f
        colorPointerPaint.color = Color.LTGRAY

        valuePointerPaint = Paint()
        valuePointerPaint.style = Style.STROKE
        valuePointerPaint.strokeWidth = 2f

        colorWheelPaint = Paint()
        colorWheelPaint.isAntiAlias = true
        colorWheelPaint.isDither = true

        colorViewPaint = Paint()
        colorViewPaint.isAntiAlias = true

        colorViewPath = Path()
        outerWheelRect = RectF()
        innerWheelRect = RectF()
        colorPointerCoords = RectF()
    }

    //@SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // drawing color wheel

        canvas.drawBitmap(colorWheelBitmap, (centerX - colorWheelRadius).toFloat(), (centerY - colorWheelRadius).toFloat(), null)

        // drawing color view

        colorViewPaint.color = Color.LTGRAY
        canvas.drawPath(colorViewPath, colorViewPaint)

        // drawing color wheel pointer

        val hueAngle = Math.toRadians(colorHSV[0].toDouble()).toFloat()
        val colorPointX = (Math.cos(hueAngle.toDouble()) * colorHSV[1] * colorWheelRadius).toInt() + centerX
        val colorPointY = (-Math.sin(hueAngle.toDouble()) * colorHSV[1] * colorWheelRadius).toInt() + centerY

        val pointerRadius = 0.1f * colorWheelRadius
        val pointerX = (colorPointX - pointerRadius / 2).toInt()
        val pointerY = (colorPointY - pointerRadius / 2).toInt()

        colorPointerCoords.set(pointerX.toFloat(), pointerY.toFloat(), pointerX + pointerRadius, pointerY + pointerRadius)
        canvas.drawOval(colorPointerCoords, colorPointerPaint)

        currentColor = Color.HSVToColor(colorHSV)

    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {

        centerX = width / 2
        centerY = height / 2

        innerPadding = paramInnerPadding * width / 100
        outerPadding = paramOuterPadding * width / 100
        valueSliderWidth = paramValueSliderWidth * width / 100

        outerWheelRadius = minOf(height, width) / 2 - outerPadding
        innerWheelRadius = outerWheelRadius - valueSliderWidth
        colorWheelRadius = innerWheelRadius - innerPadding

        outerWheelRect.set((centerX - outerWheelRadius).toFloat(), (centerY - outerWheelRadius).toFloat(), (centerX + outerWheelRadius).toFloat(), (centerY + outerWheelRadius).toFloat())
        innerWheelRect.set((centerX - innerWheelRadius).toFloat(), (centerY - innerWheelRadius).toFloat(), (centerX + innerWheelRadius).toFloat(), (centerY + innerWheelRadius).toFloat())

        colorWheelBitmap = createColorWheelBitmap(colorWheelRadius * 2, colorWheelRadius * 2)

        gradientRotationMatrix = Matrix()
        gradientRotationMatrix.preRotate(270F, width / 2F, height / 2F)

        colorViewPath.arcTo(outerWheelRect, 270F, -180F)
        colorViewPath.arcTo(innerWheelRect, 90F, 180F)
        colorViewPath.arcTo(outerWheelRect, 270F, 180F)
        colorViewPath.arcTo(innerWheelRect, 90F, -180F)

    }

    private fun createColorWheelBitmap(width: Int, height: Int): Bitmap {

        val bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888)

        val colorCount = 12
        val colorAngleStep = 360 / 12
        val colors = IntArray(colorCount + 1)
        val hsv = floatArrayOf(0f, 1f, 1f)
        for (i in colors.indices) {
            hsv[0] = ((i * colorAngleStep) % 360).toFloat()
            colors[i] = Color.HSVToColor(hsv)
        }
        colors[colorCount] = colors[0]

        // This just reverses the colors so it matches the example picture
        for (i in 0..colors.size / 2){
            val temp = colors[i]
            colors[i] = colors[colors.size - i - 1]
            colors[colors.size - i - 1] = temp
        }

        val sweepGradient = SweepGradient((width / 2).toFloat(), (height / 2).toFloat(), colors, null)
        val radialGradient = RadialGradient((width / 2).toFloat(), (height / 2).toFloat(), colorWheelRadius.toFloat(), 0xFFFFFFFF.toInt(), 0x00FFFFFF, TileMode.CLAMP)
        val composeShader = ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER)

        colorWheelPaint.shader = composeShader

        val canvas = Canvas(bitmap)
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), colorWheelRadius.toFloat(), colorWheelPaint)

        return bitmap

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {

                val x = event.x.toInt()
                val y = event.y.toInt()
                // also needed to flip and match example picture
                val cx = (x - width / 2) * -1
                val cy = y - height / 2
                val d = Math.sqrt((cx * cx + cy * cy).toDouble())

                if (d <= colorWheelRadius) {

                    colorHSV[0] = (Math.toDegrees(Math.atan2(cy.toDouble(), cx.toDouble())) + 180f).toFloat()
                    colorHSV[1] = Math.max(0f, Math.min(1f, (d / colorWheelRadius).toFloat()))

                    invalidate()

                } else if (x >= width / 2 && d >= innerWheelRadius) {

                    colorHSV[2] = Math.max(0.0, Math.min(1.0, Math.atan2(cy.toDouble(), cx.toDouble()) / Math.PI + 0.5f)).toFloat()

                    invalidate()
                }

                return true
            }
        }
        currentColor = Color.HSVToColor(colorHSV)
        return super.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = Bundle()
        state.putFloatArray("color", colorHSV)
        state.putParcelable("super", super.onSaveInstanceState())
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            colorHSV = state.getFloatArray("color")
            super.onRestoreInstanceState(state.getParcelable("super"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

}