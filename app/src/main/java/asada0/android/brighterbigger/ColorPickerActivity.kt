//
//  ColorPickerActivity.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/08/05.
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_color_picker.*
import kotlin.math.round
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
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

        color_grid.setOnItemClickListener { _, view, position, _ ->
            color_grid.setSelection(position)
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
            color_grid.getChildAt(mPrevPosition).setBackgroundColor(mColorPickerAdapter.getColor(mPrevPosition))
            mPrevPosition = position
        }

        color_save.setOnClickListener {
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

        color_cancel.setOnClickListener {
            val intent = Intent()
            setResult(SettingsActivity.RESULT_CANCEL, intent)
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mColorPickerAdapter = ColorPickerAdapter(this, color_grid.width, color_grid.height, mPrevColor)
        dispSelectedColor(mPrevColor)
        color_grid.adapter = mColorPickerAdapter
    }

    private fun dispSelectedColor(color:Int) {
        val r = round(Color.red(color) / 255.0f * 100.0f).toInt()
        val g = round(Color.green(color) / 255.0f * 100.0f).toInt()
        val b = round(Color.blue(color) / 255.0f * 100.0f).toInt()
        color_r.text = String.format("R: %3d%%", r)
        color_g.text = String.format("G: %3d%%", g)
        color_b.text = String.format("B: %3d%%", b)
        color_sample.setBackgroundColor(color)
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
