//
//  BBPreference.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/08/18.
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.preference.PreferenceManager

class BBPreference(context: Context) {
    // Constant Values
    companion object {
        val DEFAULT_MONO_COLOR = Color.rgb(170,255,0)
        val DEFAULT_DUO_LIGHT_COLOR = Color.rgb(255,255,0)
        val DEFAULT_DUO_DARK_COLOR = Color.rgb(128, 0, 255)

        const val TROUBLE_NO_TAP_FOCUS_ON_CONTINUOUS = 0x0000000000000001L  // 1st bit
        const val TROUBLE_NO_TAP_FOCUS_ANYWAY = 0x0000000000000002L // 2nd bit
        /*
        const val TROUBLE_RESERVED3 = 0x0000000000000004L // 3rd bit
        const val TROUBLE_RESERVED4 = 0x0000000000000008L // 4th bit
        const val TROUBLE_ALL_CLEAR = 0x7FFFFFFFFFFFFFFFL // all bits
        */
    }

    private var mContext = context
    private var mPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var monoColorsValue: Array<String> = context.resources.getStringArray(R.array.pref_color_dic)
    private var monoColorsName: Array<String> = context.resources.getStringArray(R.array.pref_color_used_list_titles)
    private var mColorsNum: Int
    var mColorsLight: IntArray
    var mColorsDark: IntArray
    private var mColorsName: Array<String?>

    var mVersionCode: Int = 0
        get() {
            return (mPref.getString(mContext.getString(R.string.pref_key_version_code), "0") ?: "0").toInt()
        }
        set(versionCode) {
            field = versionCode
            mPref.edit().putString(mContext.getString(R.string.pref_key_version_code), field.toString()).apply()
        }

    var mUsedColor: Int = 0
        get() {
            return (mPref.getString(mContext.getString(R.string.pref_key_color_used), "0") ?: "0").toInt()
        }
        set(color) {
            field = color
            mPref.edit().putString(mContext.getString(R.string.pref_key_color_used), field.toString()).apply()
        }

    var mMonoMode: Boolean = false
        get() {
            return mPref.getBoolean(mContext.getString(R.string.pref_key_mono_mode), false)
        }
        set(mode) {
            field = mode
            mPref.edit().putBoolean(mContext.getString(R.string.pref_key_mono_mode), field).apply()
        }

    var mCustomMonoColor: Int = DEFAULT_MONO_COLOR
        get() {
            return mPref.getInt(mContext.getString(R.string.pref_key_custom_mono_color), DEFAULT_MONO_COLOR)
        }
        set(color) {
            field = color
            for (i in 0 until mColorsNum) {
                if (monoColorsValue[i] == "mono") {
                    mColorsLight[i] = field
                    mColorsDark[i] = field
                }
            }
            mPref.edit().putInt(mContext.getString(R.string.pref_key_custom_mono_color), field).apply()
        }

    var mCustomDuoColor1: Int = DEFAULT_DUO_LIGHT_COLOR
        get() {
            return mPref.getInt(mContext.getString(R.string.pref_key_custom_duo_color1), DEFAULT_DUO_LIGHT_COLOR)
        }
        set(color) {
            field = color
            for (i in 0 until mColorsNum) {
                if (monoColorsValue[i] == "duo") {
                    mColorsLight[i] = field
                }
            }
            mPref.edit().putInt(mContext.getString(R.string.pref_key_custom_duo_color1), field).apply()
        }

    var mCustomDuoColor2: Int = DEFAULT_DUO_DARK_COLOR
        get() {
            return mPref.getInt(mContext.getString(R.string.pref_key_custom_duo_color2), DEFAULT_DUO_DARK_COLOR)
        }
        set(color) {
            field = color
            for (i in 0 until mColorsNum) {
                if (monoColorsValue[i] == "duo") {
                    mColorsDark[i] = field
                }
            }
            mPref.edit().putInt(mContext.getString(R.string.pref_key_custom_duo_color2), field).apply()
        }

    var mDiscardColorInfo: Boolean = false
        get() {
            return mPref.getBoolean(mContext.getString(R.string.pref_key_discard_color_info), false)
        }
        set(discardColorInfo) {
            field = discardColorInfo
            mPref.edit().putBoolean(mContext.getString(R.string.pref_key_discard_color_info), field).apply()
        }

    var mToneRotation: Boolean = false
        get() {
            return mPref.getBoolean(mContext.getString(R.string.pref_key_tone_rotation), false)
        }
        set(on) {
            field = on
            mPref.edit().putBoolean(mContext.getString(R.string.pref_key_tone_rotation), field).apply()
        }

    var mBigIcons: Boolean = false
        get() {
            return mPref.getBoolean(mContext.getString(R.string.pref_key_big_icons), false)
        }
        set(on) {
            field = on
            mPref.edit().putBoolean(mContext.getString(R.string.pref_key_big_icons), field).apply()
        }

    var mLongPressPause: Boolean = true
        get() {
            return mPref.getBoolean(mContext.getString(R.string.pref_key_long_press_freeze), true)
        }
        set(on) {
            field = on
            mPref.edit().putBoolean(mContext.getString(R.string.pref_key_long_press_freeze), field).apply()
        }

    var mVolumeButtonShutter: Boolean = false
        get() {
            return mPref.getBoolean(mContext.getString(R.string.pref_key_volume_button_shutter), false)
        }
        set(on) {
            field = on
            mPref.edit().putBoolean(mContext.getString(R.string.pref_key_volume_button_shutter), field).apply()
        }

    var mMaxZoom2X: Boolean = false
        get() {
            return mPref.getBoolean(mContext.getString(R.string.pref_key_max_zoom_2x), false)
        }
        set(on) {
            field = on
            mPref.edit().putBoolean(mContext.getString(R.string.pref_key_max_zoom_2x), field).apply()
        }

    var mStopContAutoFocus: Boolean = false
        get() {
            return mPref.getBoolean(mContext.getString(R.string.pref_key_cont_autofocus_off), false)
        }
        set(on) {
            field = on
            mPref.edit().putBoolean(mContext.getString(R.string.pref_key_cont_autofocus_off), field).apply()
        }

    var mProjectionBottomRatio: Float = MainActivity.PROJECTION_DEFAULT
        get() {
            return mPref.getFloat(mContext.getString(R.string.pref_key_projection_bottom_ratio), MainActivity.PROJECTION_DEFAULT)
        }
        set(ratio) {
            field = ratio
            mPref.edit().putFloat(mContext.getString(R.string.pref_key_projection_bottom_ratio), field).apply()
            mPref.edit().putInt(mContext.getString(R.string.pref_key_projection_bottom_ratio_int), ((field - MainActivity.PROJECTION_MIN) * 100).toInt()).apply()
        }

    var mProjectionBottomRatioInt: Int = ((MainActivity.PROJECTION_DEFAULT - MainActivity.PROJECTION_MIN) * 100).toInt()
        get() {
            return mPref.getInt(mContext.getString(R.string.pref_key_projection_bottom_ratio_int), ((MainActivity.PROJECTION_DEFAULT - MainActivity.PROJECTION_MIN) * 100).toInt())
        }
        set(ratio) {
            field = ratio
            mPref.edit().putInt(mContext.getString(R.string.pref_key_projection_bottom_ratio_int), field).apply()
            mPref.edit().putFloat(mContext.getString(R.string.pref_key_projection_bottom_ratio), (field / 100.0f) + MainActivity.PROJECTION_MIN).apply()
        }

    fun resetAll() {
        // Resetting all preferences
        val defaultPreference = PreferenceManager.getDefaultSharedPreferences(mContext)
        val editor = defaultPreference.edit()
        editor.clear().apply()
    }

    var mOccurredTrouble: Long = 0L
        get() {
            return mPref.getLong(mContext.getString(R.string.pref_key_occured_trouble), 0L)
        }
        set(on) {
            val prev = mPref.getLong(mContext.getString(R.string.pref_key_occured_trouble), 0L)
            field = on or prev
            mPref.edit().putLong(mContext.getString(R.string.pref_key_occured_trouble), field).apply()
        }

    fun isOccurredTrouble(target: Long) :Boolean {
        return this.mOccurredTrouble and target != 0L
    }

    /*
    fun clearOccurredTrouble(target: Long) {
        var prevTrouble = mPref.getLong(mContext.getString(R.string.pref_key_occured_trouble), 0L)
        prevTrouble = prevTrouble and target.inv()
        mPref.edit().putLong(mContext.getString(R.string.pref_key_occured_trouble), prevTrouble).apply()
    }
    */

    init {
        mColorsNum = monoColorsValue.size
        mColorsLight = IntArray(mColorsNum)
        mColorsDark = IntArray(mColorsNum)
        mColorsName = arrayOfNulls(mColorsNum)
        for (i in 0 until mColorsNum) {
            when (monoColorsValue[i]) {
                "mono" -> {
                    mColorsLight[i] = mCustomMonoColor
                    mColorsDark[i] = mCustomMonoColor
                }
                "duo" -> {
                    mColorsLight[i] = mCustomDuoColor1
                    mColorsDark[i] = mCustomDuoColor2
                }
                else -> {
                    mColorsLight[i] = Color.parseColor("#" + monoColorsValue[i].substring(0..5))
                    mColorsDark[i] = Color.parseColor("#" + monoColorsValue[i].substring(7..12))
                }
            }
            mColorsName[i] = monoColorsName[i]
        }
    }
}