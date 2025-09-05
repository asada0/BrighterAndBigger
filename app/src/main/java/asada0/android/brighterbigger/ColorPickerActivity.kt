//
//  ColorPickerActivity.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2023/10/08.
//  Copyright 2010-2023 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.math.round
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.widget.GridView
import android.widget.TextView
import kotlin.math.max

class ColorPickerActivity : AppCompatActivity() {
    private lateinit var mColorPickerAdapter: ColorPickerAdapter
    private var mSelectedColor: Int = 0
    private var mPrevColor: Int = 0
    private var mPrevPosition: Int = -1
    private lateinit var mPref: BBPreference
    private var mReqCode = SettingsActivity.REQUEST_CODE_MONO_COLOR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPref = BBPreference(this)
        mPrevColor = intent.getIntExtra("prevColor", 0)
        mReqCode = intent.getIntExtra("reqCode", SettingsActivity.REQUEST_CODE_MONO_COLOR)
        setContentView(R.layout.activity_color_picker)
        dispSelectedColor(mPrevColor)

        val colorGrid = findViewById<GridView>(R.id.color_grid)
        colorGrid.setOnItemClickListener { _, view, position, _ ->
            colorGrid.setSelection(position)
            mSelectedColor = mColorPickerAdapter.getColor(position)
            dispSelectedColor(mSelectedColor)

            // border on the selected color
            val color = mColorPickerAdapter.getColor(position)
            val selectDrawable = GradientDrawable()
            selectDrawable.setStroke(5, Color.rgb(255- Color.red(color), 255- Color.green(color), 255- Color.blue(color)))
            selectDrawable.setColor(color)
            val layerDrawable = LayerDrawable(arrayOf(selectDrawable))
            layerDrawable.setLayerInset(0, 0, 0, 0 , 0)
            view.background = layerDrawable

            // remove border on the previous selected color
            if (mPrevPosition == -1) {
                mPrevPosition = max(mColorPickerAdapter.getColorToPosition(mPrevColor), 0)
            }
            colorGrid.getChildAt(mPrevPosition).setBackgroundColor(mColorPickerAdapter.getColor(mPrevPosition))
            mPrevPosition = position
        }

        val colorSave = findViewById<TextView>(R.id.color_save)
        colorSave.setOnClickListener {
            mPrevColor = mSelectedColor
            when(mReqCode) {
                SettingsActivity.REQUEST_CODE_MONO_COLOR -> {
                    mPref.mCustomMonoColor = mSelectedColor
                }
                SettingsActivity.REQUEST_CODE_DUO_COLOR1 -> {
                    mPref.mCustomDuoColor1 = mSelectedColor
                }
                SettingsActivity.REQUEST_CODE_DUO_COLOR2 -> {
                    mPref.mCustomDuoColor2 = mSelectedColor
                }
            }

            val intent = Intent()
            setResult(SettingsActivity.RESULT_OK, intent)
            finish()
        }

        val colorCancel = findViewById<TextView>(R.id.color_cancel)
        colorCancel.setOnClickListener {
            val intent = Intent()
            setResult(SettingsActivity.RESULT_CANCEL, intent)
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val colorGrid = findViewById<GridView>(R.id.color_grid)
        mColorPickerAdapter = ColorPickerAdapter(this, colorGrid.width, colorGrid.height, mPrevColor)
        dispSelectedColor(mPrevColor)
        colorGrid.adapter = mColorPickerAdapter
    }

    private fun dispSelectedColor(color:Int) {
        val colorR = findViewById<TextView>(R.id.color_r)
        val colorG = findViewById<TextView>(R.id.color_g)
        val colorB = findViewById<TextView>(R.id.color_b)
        val colorSample = findViewById<View>(R.id.color_sample)
        val r = round(Color.red(color) / 255.0f * 100.0f).toInt()
        val g = round(Color.green(color) / 255.0f * 100.0f).toInt()
        val b = round(Color.blue(color) / 255.0f * 100.0f).toInt()
        colorR.text = String.format("R: %3d%%", r)
        colorG.text = String.format("G: %3d%%", g)
        colorB.text = String.format("B: %3d%%", b)
        colorSample.setBackgroundColor(color)
    }

    override fun onSaveInstanceState(saveInstanceState: Bundle) {
        super.onSaveInstanceState(saveInstanceState)
        //saveInstanceState.putInt("PrevColor", mPrevColor)
        saveInstanceState.putInt("SelectColor", mSelectedColor)
        saveInstanceState.putInt("PrevPosition", mPrevPosition)
        saveInstanceState.putInt("ReqCode", mReqCode)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //mPrevColor = savedInstanceState.getInt("PrevColor")
        mSelectedColor = savedInstanceState.getInt("SelectColor")
        mPrevPosition = savedInstanceState.getInt("PrevPosition")
        mReqCode = savedInstanceState.getInt("ReqCode")
        println(mSelectedColor)
        if (mSelectedColor!= 0) {
            mPrevColor = mSelectedColor
        }
    }
}
