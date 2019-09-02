//
//  TwoFingerDoubleTapDetector.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/08/05.
//
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//
// 　This is based on the code of the following site.
// 　https://stackoverflow.com/questions/12414680/how-to-implement-a-two-finger-double-click-in-android
//

package asada0.android.brighterbigger

import android.view.MotionEvent
import android.view.ViewConfiguration

abstract class TwoFingerDoubleTapDetector {
    private var mFirstDownTime: Long = 0L
    private var mSeparateTouches = false
    private var mTwoFingerTapCount: Int = 0

    companion object {
        private val TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 100L
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN ->
                if (mFirstDownTime == 0L || (event.eventTime - mFirstDownTime) > TIMEOUT) {
                    reset(event.downTime)
                }
            MotionEvent.ACTION_POINTER_UP ->
                if (event.pointerCount == 2) {
                    mTwoFingerTapCount++
                } else {
                    mFirstDownTime = 0L
                }
            MotionEvent.ACTION_UP ->
                if (!mSeparateTouches) {
                    mSeparateTouches = true
                } else if (mTwoFingerTapCount == 2 && (event.eventTime - mFirstDownTime) < TIMEOUT) {
                    onTwoFingerDoubleTap()
                    mFirstDownTime = 0L
                    return true
                }
        }
        return false
    }

    abstract fun onTwoFingerDoubleTap()

    private fun reset(time: Long) {
        mFirstDownTime = time
        mSeparateTouches = false
        mTwoFingerTapCount = 0
    }
}
