//
//  ColorPickerAdapter.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/08/05.
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlin.math.round

class ColorPickerAdapter(activity: Activity, width: Int, height: Int, initColor: Int) : BaseAdapter() {
    companion object {
        const val ROW: Int = 25
        const val COLUMN: Int = 30
    }

    private val mActivity: Activity = activity
    // Color Table
    private val colors = Array(ROW * COLUMN) {Color.HSVToColor(floatArrayOf( 360.0f / ROW * (it % ROW), 1.0f - round(it / ROW * 1.0f) / (COLUMN - 1) , 1.0f))}
    // GridView Size
    private val mWidth: Int = width
    private val mHeight: Int = height
    private val mInitColor: Int = initColor

    override fun getItem(position: Int): Any {
        return colors[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return colors.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var cView = convertView
        if (cView == null) {
            val text = TextView(mActivity)
            // grid size -> cell size
            text.width = mWidth / ROW - 2
            text.height = mHeight / COLUMN - 4
            // init focus
            val color = colors[position]
            if (mInitColor == color) {
                val selectDrawable = GradientDrawable()
                selectDrawable.setStroke(5, Color.rgb(255- Color.red(color), 255- Color.green(color), 255- Color.blue(color)))
                selectDrawable.setColor(color)
                val layerDrawable = LayerDrawable(arrayOf(selectDrawable))
                layerDrawable.setLayerInset(0, 0, 0, 0 , 0)
                text.background = layerDrawable
            } else {
                text.setBackgroundColor(color)
            }
            cView = text
        }
        return cView
    }

    fun getColor(position: Int): Int {
        return colors[position]
    }

    fun getColorToPosition(color: Int): Int {
        return colors.indexOf(color)
    }
}