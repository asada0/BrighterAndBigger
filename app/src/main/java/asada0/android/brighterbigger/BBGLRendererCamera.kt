//
//  BBGLRendererCamera.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/08/18.
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class BBGLRendererCamera(context: Context, activity: Activity): BBGLRenderer(context, activity) {
    override
    val tag: String = "BB-GLRendererCamera"

    init {
        init()
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        setupTextureCameraShader()
    }

    fun setupTextureCameraShader() {
        if ((mCameraPosition != BBCamera.CameraPosition.BACK) && (mCameraPosition != BBCamera.CameraPosition.FRONT)) {
            return
        }
        if (mCamera != null) {
            mCamera!!.closeCamera()
        }
        mTexture = BBTexture()
        mCamera = BBCamera(mActivity!!, mTexture!!.mTextureHandle)
        mCamera!!.mContinuousFocus = mContinuousFocus
        mCamera!!.mNoTapFocusOnContinuousMode = mNoTapFocusOnContinuousMode
        mCamera!!.mNoTapFocusAnyway = mNoTapFocusAnyway
        if (!mCamera!!.open(mCameraPosition)) {
            mCamera!!.closeCamera()
        }
        // Load and Compile shader
        mOffscreenShader = BBShader(mContext)
        mOffscreenShader!!.setProgram(R.raw.bbvshader, R.raw.bbfshader_ex)
        mOffscreenShader!!.useProgram()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        mCamera ?: return
        mSourceSize = mCamera!!.mCameraSize
        mScreenSize = Size(width, height)
        val textureMax = maxTextureSize()
        if (textureMax != 0) mMaxTextureSize = textureMax
        adjustTexture()
    }

    override fun onDrawFrame(gl: GL10) {
        mCamera ?: return
        if (mIsImageFrom != MainActivity.ImageFrom.CAMERA) return
        mCamera!!.updateTexture()
        mTexture!!.bindCameraTexture()
        drawScreen()
    }

    private fun setCameraPosition(cameraPosition: BBCamera.CameraPosition): Boolean {
        mCamera ?: return false
        if (!mCamera!!.open(cameraPosition)) {
            mCamera!!.closeCamera()
            return false
        }
        mSourceSize = mCamera!!.mCameraSize
        mCameraPosition = cameraPosition
        adjustTexture()
        return true
    }

    fun changeCameraToRear(): Boolean {
        mCamera ?: return false
        return setCameraPosition(BBCamera.CameraPosition.BACK)
    }

    fun changeCameraToFront(): Boolean {
        mCamera ?: return false
        return setCameraPosition(BBCamera.CameraPosition.FRONT)
    }

    fun reOpenCamera(): Boolean {
        mCamera ?: return false
        return setCameraPosition(mCameraPosition)
    }

    fun closeCamera() {
        mCamera ?: return
        mCamera!!.closeCamera()
    }

    fun touchFocus(ratioX: Float, ratioY: Float) {
        mCamera ?: return
        mCamera!!.touchFocus(ratioX, ratioY)
    }

    fun hasLight(): BBCamera.UYN {
        mCamera ?: return BBCamera.UYN.UNKNOWN
        return mCamera!!.mHasLight
    }

    fun torchLight(on: Boolean, immediately: Boolean) {
        mCamera ?: return
        mCamera!!.torchLight(on = on, immediately = immediately)
    }

    fun continuousFocus(on: Boolean) {
        mContinuousFocus = on
        mCamera ?: return
        mCamera!!.continuousFocus(on)
    }

    fun freezeCaptureStart() {
        mCamera ?: return
        mCamera!!.freezeCaptureStart()
    }

    fun freezeCaptureBitmap(): Bitmap? {
        mCamera ?: return null
        return mCamera!!.mFreezeBitmap
    }

    fun freezeRotationMatrix(): Matrix? {
        mCamera ?: return null
        return mCamera!!.freezeRotationMatrix()
    }

    fun noTapFocusOnContinuousMode(on: Boolean) {
        mNoTapFocusOnContinuousMode = on
        mCamera ?: return
        mCamera!!.mNoTapFocusOnContinuousMode = on
    }

    fun noTapFocusAnyway(on: Boolean) {
        mNoTapFocusAnyway = on
        mCamera ?: return
        mCamera!!.mNoTapFocusAnyway = on
    }

}