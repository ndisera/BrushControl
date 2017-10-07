package edu.utah.a4530.cs.brushcontrol

import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** Brush color will match the chosen color */
        colorPicker.setOnColorChangedListener { _, currentColor ->
            lineView.currentColor = currentColor
        }

        /** Brush cap type will match the lines cap type in the chosen square */
        capView.setOnRectChosenListener { _, chosen ->
            when {
                chosen[0] -> lineView.capType = Paint.Cap.SQUARE
                chosen[1] -> lineView.capType = Paint.Cap.ROUND
                chosen[2] -> lineView.capType = Paint.Cap.BUTT
            }
        }

        /** Brush join type will match the chosen square's join type */
        joinView.setOnRectChosenListener { _, chosen ->
            when {
                chosen[0] -> lineView.joinType = Paint.Join.ROUND
                chosen[1] -> lineView.joinType = Paint.Join.BEVEL
                chosen[2] -> lineView.joinType = Paint.Join.MITER
            }
        }

        /** Seek Bar modifies brush width */
        widthBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                lineView.brushWidth = widthBar.progress.toFloat()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

    }
}
