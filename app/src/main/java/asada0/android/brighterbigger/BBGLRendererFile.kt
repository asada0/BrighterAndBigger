//
//  BBGLRendererFile.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/08/05.
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class BBGLRendererFile(context: Context, activity: Activity): BBGLRenderer(context, activity) {
    override
    val tag: String = "BB-GLRendererFile"
    private var mFileTextureBitmap: Bitmap? = null

    init {
        init()
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        mTexture = BBTexture()
        mOffscreenShader = BBShader(mContext)
        mOffscreenShader!!.setProgram(R.raw.bbvshader, R.raw.bbfshader_2d)
        mOffscreenShader!!.useProgram()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        mFileTextureBitmap ?: return
        mSourceSize = getImageSize()
        mScreenSize = Size(width, height)
        val textureMax = maxTextureSize()
        if (textureMax != 0) mMaxTextureSize = textureMax
        adjustTexture()
    }

    override fun onDrawFrame(gl: GL10) {
        if (mIsImageFrom == MainActivity.ImageFrom.CAMERA) return
        mFileTextureBitmap ?: return
        mTexture!!.bindFileTexture(mFileTextureBitmap!!)
        drawScreen()
    }

    fun setFileTextureBitmap(bitmap: Bitmap) {
        mFileTextureBitmap = bitmap
        adjustTexture()
    }

    /*
    fun getFileTextureBitmap(): Bitmap? {
        return mFileTextureBitmap
    }
    */

    fun unsetFileTextureBitmap() {
        mFileTextureBitmap = null
    }

    private fun getImageSize(): Size {
        if (mFileTextureBitmap == null) {
            return Size(0, 0)
        }
        return Size(mFileTextureBitmap!!.width, mFileTextureBitmap!!.height)
    }
}