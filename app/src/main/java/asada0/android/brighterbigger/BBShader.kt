//
//  BBShader.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/08/05.
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import java.io.ByteArrayOutputStream
import java.io.InputStream

class BBShader(context: Context) {
    private val tag: String = "BB-Shader"
    private var mContext: Context = context
    private var mProgram = 0
    private var mShaderVertex = 0
    private var mShaderFragment = 0
    private var mVertexSource : String ?= null
    private var mFragmentSource : String ?= null
    private var mShaderHandleMap = HashMap<String, Int>()
    private var mError: BBError = BBError(context)

    @Throws(Exception::class)
    fun setProgram(vertexShader: Int, fragmentShader: Int) {
        if (mProgram != 0) return

        mVertexSource = loadRawString(vertexShader, mContext)
        mFragmentSource = loadRawString(fragmentShader, mContext)
        mShaderVertex = loadShader(GLES20.GL_VERTEX_SHADER, mVertexSource!!)
        mShaderFragment = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentSource!!)

        val program: Int = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, mShaderVertex)
            GLES20.glAttachShader(program, mShaderFragment)
            GLES20. glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                val error: String = GLES20.glGetProgramInfoLog(program)
                deleteProgram()
                throw Exception(error)
            }
        }

        mProgram = program
        mShaderHandleMap.clear()
    }

    fun useProgram() {
        GLES20.glUseProgram(mProgram)
    }

    private fun deleteProgram() {
        GLES20.glDeleteShader(mShaderVertex)
        GLES20.glDeleteShader(mShaderFragment)
        GLES20.glDeleteProgram(mProgram)
        mShaderFragment = 0
        mShaderVertex = 0
        mProgram = 0
    }

    fun getHandle(name: String) : Int {
        if(mShaderHandleMap.containsKey(name)) {
            val ret: Int? = mShaderHandleMap[name]
            if (ret != null) return ret
        }

        var handle: Int = GLES20.glGetAttribLocation(mProgram, name)
        if (handle == -1) {
            handle = GLES20.glGetUniformLocation(mProgram, name)
        }
        if (handle == -1) {
            mError.log(tag, "Attribute is not Found: $name")
        } else {
            mShaderHandleMap[name] = handle
        }
        return handle
    }

    @Throws(Exception::class)
    private fun loadShader(shaderType: Int, source: String): Int {
        val shader: Int = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                val error: String = GLES20.glGetShaderInfoLog(shader)
                GLES20.glDeleteShader(shader)
                throw Exception(error)
            }
        }
        return shader
    }

    private fun loadRawString(rawId: Int, context: Context): String {
        val input: InputStream = context.resources.openRawResource(rawId)
        val baos = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        do {
            val len: Int = input.read(buf)
            if (len != -1) {
                baos.write(buf, 0, len)
            } else {
                break
            }
        } while (true)
        return baos.toString()
    }

    fun setBrightness(brightness: Float) {
        val handle: Int = GLES20.glGetUniformLocation(mProgram, "brightness")
        GLES20.glUniform1f(handle, brightness)
    }

    fun setContrast(contrast: Float) {
        val handle: Int = GLES20.glGetUniformLocation(mProgram, "contrast")
        GLES20.glUniform1f(handle, contrast)
    }

    fun setReverse(reverse: Boolean) {
        val handle: Int = GLES20.glGetUniformLocation(mProgram, "reverse")
        GLES20.glUniform1i(handle, if (reverse) 1 else 0)
    }

    fun setMonoMode(monoMode: Boolean) {
        val handle: Int = GLES20.glGetUniformLocation(mProgram, "monoMode")
        GLES20.glUniform1i(handle, if (monoMode) 1 else 0)
    }

    fun setMonoLightColor(monoLightColor: Int) {
        val r: Float = Color.red(monoLightColor) / 255.0f
        val g: Float = Color.green(monoLightColor) / 255.0f
        val b: Float = Color.blue(monoLightColor) / 255.0f
        val handle: Int = GLES20.glGetUniformLocation(mProgram, "monoLightColor")
        GLES20.glUniform3f(handle, r, g, b)
    }

    fun setMonoDarkColor(monoDarkColor: Int) {
        val r: Float = Color.red(monoDarkColor) / 255.0f
        val g: Float = Color.green(monoDarkColor) / 255.0f
        val b: Float = Color.blue(monoDarkColor) / 255.0f
        val handle: Int = GLES20.glGetUniformLocation(mProgram, "monoDarkColor")
        GLES20.glUniform3f(handle, r, g, b)
    }
}
