//
//  BBCamera.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/09/24.
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.util.Size
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.math.max



class BBCamera(activity: Activity, textureID: Int) {
    private val tag: String = "BB-Camera"

    companion object {
        const val AF_TOUCH_WIDTH = 50
        const val AF_TOUCH_HEIGHT = 50
        const val TAG_PREVIEW = "TAG_PREVIEW"
        const val TAG_AUTOFOCUS_TRIGGER = "TAG_AUTOFOCUS_TRIGGER"
        const val INTENT_FREEZE = "INTENT_READY_TO_FREEZE"
        const val INTENT_CAMERA_TROUBLE = "INTENT_CAMERA_TROUBLE"
        const val TIMER_FOCUSING = 7000L
        const val TIMER_TAP_TO_PASSIVE_FOCUS = 5000L
        const val MAX_PREVIEW_WIDTH = 1920
        const val MAX_PREVIEW_HEIGHT = 1080
    }

    enum class CameraPosition { NONE, BACK, FRONT }
    enum class UYN { UNKNOWN, YES, NO }
    enum class FocusState { NONE, BEGIN, TRIGGER, IDLE, CANCEL, END }

    private var mActivity: Activity? = activity
    private var mTextureID: Int = textureID
    private var mCamera: CameraDevice? = null
    private var mPreviewSession: CameraCaptureSession? = null
    private var mPreviewBuilder: CaptureRequest.Builder? = null
    private var mBackgroundHandler: Handler? = null
    private var mPreviewCaptureCallbackHandler: CameraCaptureSession.CaptureCallback? = null
    private var mFocusCaptureCallbackHandler: CameraCaptureSession.CaptureCallback? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mCameraFpsRange: Range<Int> = Range(10, 10)
    private var mError = BBError(activity)
    private var mCameraOpenCloseSemaphore = Semaphore(1)
    private var mMeteringAreaAFSupported = true
    private var mMeteringAreaAESupported = true
    private var mFreezeBuilder: CaptureRequest.Builder? = null
    private var mImageReader: ImageReader? = null
    private var mSensorArraySize: Rect = Rect(0, 0, 0, 0)
    private var mFocusTimerHandler: Handler? = null
    private var mFocusTimerRunnable: Runnable? = null
    private var mManualFocusing = false
        set(on) {
            field = on
            if (on) focusTimerStart() else focusTimerCancel()
        }
    private var mFocusState: FocusState = FocusState.NONE
    private var mTorchLight = false
    private var mNonPassiveFocusTimer = 0L

    var mCameraSize: Size = Size(0, 0)
    var mCameraPosition: CameraPosition = CameraPosition.BACK
    var mSensorOrientation: Int = 0
    var mHasLight: UYN = UYN.UNKNOWN
    var mFreezeBitmap: Bitmap? = null
    var mContinuousFocus = true
    var mNoTapFocusOnContinuousMode = false
    var mNoTapFocusAnyway = false
    var mlastStatus = -1 // for debug

    fun open(cameraPosition: CameraPosition): Boolean {
        if (mCamera != null) {
            closeCamera()
            mCamera = null
        }
        mPreviewBuilder = null
        mFreezeBuilder = null
        mPreviewSession = null

        // Camera Position
        mCameraPosition = cameraPosition

        val targetFacing = when (mCameraPosition) {
            CameraPosition.BACK -> CameraCharacteristics.LENS_FACING_BACK
            CameraPosition.FRONT -> CameraCharacteristics.LENS_FACING_FRONT
            else -> {
                mError.log(tag, "Camera error - Neither front nor rear camera.")
                return false
            }
        }
        val manager = mActivity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (cameraId: String in manager.cameraIdList) {
            val characteristics: CameraCharacteristics = manager.getCameraCharacteristics(cameraId)
            val facing: Int? = characteristics.get(CameraCharacteristics.LENS_FACING)

            if (facing != null && facing == targetFacing) {
                val map: StreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) as StreamConfigurationMap
                // Choose camera resolution
                val texSizes: Array<Size> = map.getOutputSizes(SurfaceTexture::class.java)
                // val jpgSizes: Array<Size> = map.getOutputSizes(ImageFormat.JPEG)

                mCameraSize = chooseResolution(texSizes)!!
                // Choose camera frequency
                val ranges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                mCameraFpsRange = chooseFPS(ranges!!)!!

                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                mSensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!
                mMeteringAreaAFSupported = (characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)!! >= 1)
                mMeteringAreaAESupported = (characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)!! >= 1)
                mHasLight = if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)!!) UYN.YES else UYN.NO
                // val deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                mFocusState = FocusState.NONE
                mManualFocusing = false

                // Start thread
                val thread = HandlerThread("OpenCVSCamera")
                thread.start()
                val backgroundHandler = Handler(thread.looper)

                mCameraOpenCloseSemaphore.acquire()
                try {
                    manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                        override fun onOpened(camera: CameraDevice) {
                            mCamera = camera
                            createCaptureSession()
                        }
                        override fun onDisconnected(camera: CameraDevice) {
                            mError.log(tag, "Camera error - onDisconnected in open.")
                            closeCameraForce()
                        }
                        override fun onError(camera: CameraDevice, error: Int) {
                            mError.log(tag, "Camera error - onError[$error] in open.")
                            closeCameraForce()
                            // for Xperia Z3
                            if (!mNoTapFocusAnyway && error == ERROR_CAMERA_IN_USE && mManualFocusing) {
                                sendNoTapFocusAnywayIntent()
                            }
                        }
                    }, backgroundHandler)
                } catch (e: CameraAccessException) {
                    mError.log(tag, "Camera error - CameraAccessException in open.")
                    mCameraOpenCloseSemaphore.release()
                    return false
                } catch (e: IllegalStateException) {
                    mError.log(tag, "Camera error - IllegalStateException in open.")
                    mCameraOpenCloseSemaphore.release()
                    return false
                } catch (e: SecurityException) {
                    mError.log(tag, "Camera error - SecurityException in open.")
                    mCameraOpenCloseSemaphore.release()
                    return false
                } catch (e: Exception) {
                    mError.log(tag, "Camera error - Exception in open.")
                    mCameraOpenCloseSemaphore.release()
                    return false
                }
                return true
            }
        }
        return false
    }

    fun closeCamera() {
        mCamera ?: return
        mCameraOpenCloseSemaphore.acquire()
        closeCameraForce()
    }

    fun closeCameraForce() {
        mCamera ?: return
        focusTimerCancel()
        if (mPreviewSession != null) mPreviewSession!!.close()
        mCamera!!.close()
        if (mImageReader != null) mImageReader!!.close()
        mCameraOpenCloseSemaphore.release()
    }

    private fun createCaptureSession() {
        // for Preview
        mSurfaceTexture = SurfaceTexture(mTextureID)
        mSurfaceTexture!!.setDefaultBufferSize(mCameraSize.width, mCameraSize.height)
        val surface = Surface(mSurfaceTexture)
        try {
            mPreviewBuilder = mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        } catch (e: Exception) {
            mError.log(tag, "Camera error - Exception in createCaptureSession/createCaptureRequest(1)")
            closeCameraForce()
        }
        mPreviewBuilder!!.addTarget(surface)
        // End for Preview
        // for Freeze capture
        mImageReader = ImageReader.newInstance(mCameraSize.width, mCameraSize.height, ImageFormat.JPEG, 2)
        mImageReader!!.setOnImageAvailableListener({ reader ->
            try {
                val image = reader.acquireLatestImage()
                if (image != null) {
                    val imageBuffer = image.planes[0].buffer
                    val imageBytes = ByteArray(imageBuffer.remaining())
                    imageBuffer.get(imageBytes)
                    image.close()
                    mFreezeBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    sendReadyToFreezeIntent()
                }
            } catch (e: Exception) {
                mError.log(tag, "Camera error - Exception in freezeCapture")
            }
        }, mBackgroundHandler)

        try {
            mFreezeBuilder = mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        } catch (e: Exception) {
            mError.log(tag, "Camera error - Exception in createCaptureSession/createCaptureRequest(2)")
            return
        }
        mFreezeBuilder!!.addTarget(mImageReader!!.surface)

        try {
            mCamera!!.createCaptureSession(listOf(surface, mImageReader!!.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    mPreviewSession = session
                    val thread = HandlerThread("CameraPreview")
                    thread.start()
                    mBackgroundHandler = Handler(thread.looper)
                    updatePreview(withSemaphore = true)
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    mError.log(tag, "Camera error - in CameraCaptureSession.onConfigureFailed().")
                    closeCameraForce()
                }
            }, null)
        } catch (e: Exception) {
            mError.log(tag, "Camera error - Exception in createCaptureSession/createCaptureSession")
            closeCameraForce()
        }
        // End for Freeze capture
    }

    private fun updatePreview(withSemaphore: Boolean) {
        // callbacks
        mPreviewCaptureCallbackHandler = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                val afState = result.get(CaptureResult.CONTROL_AF_STATE) ?: return
                // if (afState != mlastStatus) mError.log(tag, "FocusP: $afState")
                mlastStatus = afState
                // for Xperia. If tap focus is executed even once in continuous autofocus mode, then continuous autofocus can no longer be performed.
                if (mNoTapFocusOnContinuousMode) return
                if (mContinuousFocus && (afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureRequest.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED)) {
                    if (mNonPassiveFocusTimer > 0L) {
                        if (System.currentTimeMillis() - mNonPassiveFocusTimer > TIMER_TAP_TO_PASSIVE_FOCUS) {
                            mError.log(tag, "non passive focus lasted for 5 seconds")
                            mNonPassiveFocusTimer = 0L
                            sendNoTapFocusOnContinuousModeIntent()
                        }
                    } else {
                        mNonPassiveFocusTimer = System.currentTimeMillis()
                    }
                } else mNonPassiveFocusTimer = 0L
            }
        }
        if (mPreviewBuilder != null) {
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_AF_MODE, if (mContinuousFocus) CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE else CaptureRequest.CONTROL_AF_MODE_AUTO)
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_AE_LOCK, false)
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, mCameraFpsRange)
            mPreviewBuilder!!.set(CaptureRequest.FLASH_MODE, if (mTorchLight) CaptureRequest.FLASH_MODE_TORCH else CaptureRequest.FLASH_MODE_OFF)
            mPreviewBuilder!!.setTag(TAG_PREVIEW)
        }
        if (mFreezeBuilder != null) {
            mFreezeBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            mFreezeBuilder!!.set(CaptureRequest.CONTROL_AF_MODE, if (mContinuousFocus) CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE else CaptureRequest.CONTROL_AF_MODE_AUTO)
            mFreezeBuilder!!.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            mFreezeBuilder!!.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
            mFreezeBuilder!!.set(CaptureRequest.CONTROL_AE_LOCK, false)
            mFreezeBuilder!!.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, mCameraFpsRange)
            mFreezeBuilder!!.set(CaptureRequest.FLASH_MODE, if (mTorchLight) CaptureRequest.FLASH_MODE_TORCH else CaptureRequest.FLASH_MODE_OFF)
        }
        if (mPreviewSession != null) {
            try {
                mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build(), mPreviewCaptureCallbackHandler, mBackgroundHandler)
            } catch (e: Exception) {
                mError.log(tag, "Camera error - Exception in updatePreview/setRepeatingRequest")
                closeCameraForce()
            }
        } else {
            mError.log(tag, "mPreviewSession == null in updatePreview")
        }
        if (withSemaphore) mCameraOpenCloseSemaphore.release()
    }

    fun updateTexture(): SurfaceTexture? {
        if (mSurfaceTexture != null) {
            mSurfaceTexture!!.updateTexImage()
        }
        return mSurfaceTexture
    }

    fun torchLight(on: Boolean, immediately: Boolean) {
        mTorchLight = on
        if (immediately) {
            if (mManualFocusing) autoFocusCancel() else updatePreview(withSemaphore = false)
        }
    }

    private fun isCameraDisplayTwisted(cameraSize: Size, displaySize: Size): Boolean {
        // Twisted or Not?: Camera orientation and Display dimension
        //  portrait & landscape | landscape & landscape -> true
        //  portrait & portrait | landscape & landscape -> false
        val sourceAspect = cameraSize.width.toFloat() / cameraSize.height
        val displayAspect = displaySize.width.toFloat() / displaySize.height
        return ((sourceAspect > 1.0f) && (displayAspect < 1.0f)) || ((sourceAspect < 1.0f) && (displayAspect > 1.0f))
    }

    // Choose a Camera resolution
    private fun chooseResolution(supportedSizes: Array<Size>): Size? {
        var displaySize: Size = getDisplaySize()
        if (isCameraDisplayTwisted(supportedSizes[0], displaySize)) {
            displaySize = Size(displaySize.height, displaySize.width)
        }
        val guaranteed: List<Size> = supportedSizes.filter { it.width <= MAX_PREVIEW_WIDTH && it.height <= MAX_PREVIEW_HEIGHT }
        val larger: List<Size> = guaranteed.filter { displaySize.width <= it.width && displaySize.height <= it.height }
        val noLarger: List<Size> = guaranteed.minus(larger)
        if (larger.isNotEmpty()) {
            // Choose a resolution whose width and height are larger than that of the display and which has a shortest distance between "width difference" and "height difference".
            return larger.minBy { (it.width - displaySize.width) * (it.width - displaySize.width) + (it.height - displaySize.height) * (it.height - displaySize.height) }
        }
        // If either the width or the height is smaller than that of the display, choose a resolution which has a shortest distance between "width difference" and "height difference".
        return noLarger.minBy { (it.width - displaySize.width) * (it.width - displaySize.width) + (it.height - displaySize.height) * (it.height - displaySize.height) }
    }

    // Choose a Camera FPS
    private fun chooseFPS(supportedFPSs: Array<Range<Int>>): Range<Int>? {
        val above10: List<Range<Int>> = supportedFPSs.filter { it.upper >= 10 }
        if (above10.isNotEmpty()) {
            // Choose FPS with the smallest upper above 10 FPS. If there are two or more, choose the one with the biggest lower
            return above10.minWith(Comparator { a, b -> (a.upper - b.upper) * 1000 + (b.lower - a.lower) })
        }
        // If there is no upper with 10 FPS or more, choose the one with the biggest upper. If there are two or more, choose the one with the biggest lower
        return supportedFPSs.maxWith(Comparator { a, b -> (a.upper - b.upper) * 1000 + (a.lower - b.lower) })
    }

    private fun getDisplaySize(): Size {
        val display: Display = (mActivity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        return Size(size.x, size.y)
    }

    private fun getDisplayRotation(): Int {
        return when ((mActivity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0 // Display rotation: 0 (Portrait on Smartphone)
            Surface.ROTATION_90 -> 1 // Display rotation: 90 (Landscape Left on Smartphone)
            Surface.ROTATION_180 -> 2 // Display rotation: 180 (Portrait Upside down on Smartphone)
            Surface.ROTATION_270 -> 3 // Display rotation: 270 (Landscape Right on Smartphone)
            else -> 0
        }
    }

    fun touchFocus(touchX: Float, touchY: Float) {
        mPreviewBuilder ?: return
        mPreviewSession ?: return
        if (mContinuousFocus && mNoTapFocusOnContinuousMode) return
        if (mNoTapFocusAnyway) return
        if (!mMeteringAreaAFSupported) return
        if (mManualFocusing) {
            mError.log(tag, "Manual focus already running, ignored.")
            return
        }
        // callbacks
        mFocusCaptureCallbackHandler = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                val afState = result.get(CaptureResult.CONTROL_AF_STATE) ?: return
                // if (afState != mlastStatus) mError.log(tag, "FocusF: $afState")
                mlastStatus = afState
                if (mManualFocusing) {
                    focusProgress(request.tag!!, afState, touchX, touchY)
                    if (mFocusState == FocusState.END) {
                        mFocusState = FocusState.NONE
                        mManualFocusing = false
                        // mError.log(tag, "Focus($afState) -> updatePreview()")
                        updatePreview(withSemaphore = false)
                    }
                }
            }
            override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
                super.onCaptureFailed(session, request, failure)
                mFocusState = FocusState.NONE
                mManualFocusing = false
                mError.log(tag, "Camera error - onCaptureFailed[$failure] in touchFocus")
                updatePreview(withSemaphore = false)
            }
        }

        mManualFocusing = true
        mFocusState = FocusState.BEGIN
        focusProgress("", 0, touchX, touchY)
    }

    private fun focusProgress(requestTag: Any, afState: Int, touchX: Float, touchY: Float) {
        try {
            when (mFocusState) {
                FocusState.BEGIN -> {
                    val focusArea = createAutoFocusAreaRect(touchX, touchY)
                    val afRegions = arrayOf(focusArea)
                    // set an AF region
                    if (mMeteringAreaAFSupported) {
                        mPreviewBuilder!!.set(CaptureRequest.CONTROL_AF_REGIONS, afRegions)
                    }
                    // set an AE region
                    if (mMeteringAreaAESupported) {
                        mPreviewBuilder!!.set(CaptureRequest.CONTROL_AE_REGIONS, afRegions)
                    }
                    mPreviewBuilder!!.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                    mPreviewBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
                    mPreviewBuilder!!.setTag(TAG_AUTOFOCUS_TRIGGER)
                    mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build(), mFocusCaptureCallbackHandler, mBackgroundHandler)
                    mFocusState = FocusState.TRIGGER
                }
                FocusState.TRIGGER -> {
                    if (requestTag == TAG_AUTOFOCUS_TRIGGER) {
                        mPreviewBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
                        mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build(), mFocusCaptureCallbackHandler, mBackgroundHandler)
                        mFocusState = FocusState.IDLE
                    }
                }
                FocusState.IDLE -> {
                    if (afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureRequest.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                        mFocusState = FocusState.END
                    }
                }
                FocusState.CANCEL -> {
                    mPreviewBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
                    mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build(), mFocusCaptureCallbackHandler, mBackgroundHandler)
                    mFocusState = FocusState.END
                }
                else -> {
                }
            }
        } catch (e: Exception) {
            mError.log(tag, "Camera error - Exception in focusProgress")
            mFocusState = FocusState.END
        }
        // CONTROL_AF_STATE constants
        // CaptureRequest.CONTROL_AF_STATE_INACTIVE 0: Focus Inactive
        // CaptureRequest.CONTROL_AF_STATE_PASSIVE_SCAN 1: Cont. Auto Focus Scanning
        // CaptureRequest.CONTROL_AF_STATE_PASSIVE_FOCUSED 2: Cont. Auto Focus Focused
        // CaptureRequest.CONTROL_AF_STATE_ACTIVE_SCAN 3: Touch Auto Focus Scanning
        // CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED 4: Touch Auto Focus Locked
        // CaptureRequest.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED 5: Touch Auto Focus Failed
        // CaptureRequest.CONTROL_AF_STATE_PASSIVE_UNFOCUSED 6: Cont. Auto Focus Unfocused
    }

    private fun createAutoFocusAreaRect(ratioX: Float, ratioY: Float): MeteringRectangle? {
        // xy coordinates of camera sensor
        val sensorPos = convertDisplayPointToSensorPoint(ratioX, ratioY)
        return MeteringRectangle(
                max(0, sensorPos.x - AF_TOUCH_WIDTH / 2),
                max(0, sensorPos.y - AF_TOUCH_HEIGHT / 2),
                AF_TOUCH_WIDTH,
                AF_TOUCH_HEIGHT,
                MeteringRectangle.METERING_WEIGHT_MAX - 1)
    }

    // Convert xy coordinates of display to sensor
    private fun convertDisplayPointToSensorPoint(ratioX: Float, ratioY: Float): Point {
        val screenSize = getDisplaySize()
        val sensorSize = if (mSensorOrientation % 180 == 0) Size(mSensorArraySize.width(), mSensorArraySize.height()) else Size(mSensorArraySize.height(), mSensorArraySize.width())

        val hvAdjustingRatioX = max((sensorSize.width.toFloat() / sensorSize.height) / (screenSize.width.toFloat() / screenSize.height), 1.0f)
        val hvAdjustingRatioY = max((screenSize.width.toFloat() / screenSize.height) / (sensorSize.width.toFloat() / sensorSize.height), 1.0f)
        val xTimes = 1.0f / hvAdjustingRatioX
        val yTimes = 1.0f / hvAdjustingRatioY
        val xOffset = (1.0f - xTimes) / 2.0f
        val yOffset = (1.0f - yTimes) / 2.0f

        val ratioXN = if (mCameraPosition != CameraPosition.FRONT) ratioX else 1.0f - ratioX

        val sensorRatioX = kotlin.math.min(max(0.0f, xOffset + ratioXN * xTimes), 1.0f)
        val sensorRatioY = kotlin.math.min(max(0.0f, yOffset + ratioY * yTimes), 1.0f)

        val sensorWidth = mSensorArraySize.width()
        val sensorHeight = mSensorArraySize.height()
        val sensorX: Float
        val sensorY: Float

        val displayRotation = getDisplayRotation()
        when ((mSensorOrientation - displayRotationToDegree(displayRotation) + 360) % 360) {
            90 -> {
                sensorX = sensorWidth * sensorRatioY
                sensorY = sensorHeight * (1.0f - sensorRatioX)
            }
            180 -> {
                sensorX = sensorWidth * (1.0f - sensorRatioX)
                sensorY = sensorHeight * (1.0f - sensorRatioY)
            }
            270 -> {
                sensorX = sensorWidth * (1.0f - sensorRatioY)
                sensorY = sensorHeight * sensorRatioX
            }
            else -> {
                sensorX = sensorWidth * sensorRatioX
                sensorY = sensorHeight * sensorRatioY
            }
        }
        return Point(sensorX.toInt(), sensorY.toInt())
    }

    private fun displayRotationToDegree(rotation: Int): Int {
        return rotation * 90
    }

    fun freezeCaptureStart() {
        if (mPreviewSession != null && mFreezeBuilder != null) {
            try {
                mPreviewSession!!.capture(mFreezeBuilder!!.build(), null, mBackgroundHandler)
            } catch (e: Exception) {
                mError.log(tag, "Camera error - Exception in freezeCaptureStart(1)")
                mFreezeBitmap = null
                sendReadyToFreezeIntent()
            }
        } else {
            mError.log(tag, "Camera error - Exception in freezeCaptureStart(2)")
            mFreezeBitmap = null
            sendReadyToFreezeIntent()
        }
    }

    fun freezeRotationMatrix(): Matrix {
        val matrix = Matrix()
        val displayRotation = getDisplayRotation()
        val rotation: Int = (mSensorOrientation - displayRotationToDegree(displayRotation) + 360) % 360
        if (mCameraPosition == CameraPosition.FRONT) {
            if (mSensorOrientation % 180 == 90)
                matrix.postScale(1.0f, -1.0f, mCameraSize.width / 2.0f, mCameraSize.height / 2.0f)
            else
                matrix.postScale(-1.0f, 1.0f, mCameraSize.width / 2.0f, mCameraSize.height / 2.0f)
        }
        matrix.postRotate(rotation.toFloat(), mCameraSize.width / 2.0f, mCameraSize.height / 2.0f)
        return matrix
    }

    private fun sendReadyToFreezeIntent() {
        mActivity!!.sendBroadcast(Intent(INTENT_FREEZE))
    }

    private fun sendNoTapFocusOnContinuousModeIntent() {
        val intent = Intent(INTENT_CAMERA_TROUBLE)
        intent.putExtra(MainActivity.TROUBLE_ID, BBPreference.TROUBLE_NO_TAP_FOCUS_ON_CONTINUOUS)
        mActivity!!.sendBroadcast(intent)
    }

    private fun sendNoTapFocusAnywayIntent() {
        val intent = Intent(INTENT_CAMERA_TROUBLE)
        intent.putExtra(MainActivity.TROUBLE_ID, BBPreference.TROUBLE_NO_TAP_FOCUS_ANYWAY)
        mActivity!!.sendBroadcast(intent)
    }

    private fun focusTimerStart() {
        mFocusTimerHandler = Handler()
        mFocusTimerRunnable = Runnable {
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
            try {
                mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build(), mFocusCaptureCallbackHandler, mBackgroundHandler)
            } catch (e: Exception) {
                mError.log(tag, "Camera error - Exception in focusTimerStart runnable")
            }
            mFocusState = FocusState.END
            mError.log(tag, "Focus timeout!")
        }
        mFocusTimerHandler!!.postDelayed(mFocusTimerRunnable!!, TIMER_FOCUSING)
    }

    private fun focusTimerCancel() {
        mFocusTimerHandler ?: return
        mFocusTimerRunnable ?: return
        mFocusTimerHandler!!.removeCallbacks(mFocusTimerRunnable!!)
        mFocusTimerHandler = null
    }

    private fun autoFocusCancel() {
        mPreviewBuilder ?: return
        mPreviewSession ?: return
        if (mManualFocusing) {
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            try {
                mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build(), mFocusCaptureCallbackHandler, mBackgroundHandler)
            } catch (e: Exception) {
                mError.log(tag, "Camera error - Exception in autoFocusCancel")
            }
            mFocusState = FocusState.CANCEL
            mError.log(tag, "Focus cancelled!")
        }
    }

    fun continuousFocus(on: Boolean) {
        if (on != mContinuousFocus) {
            mContinuousFocus = on
            if (on) {
                closeCamera()
                open(mCameraPosition)
            } else {
                if (mManualFocusing) autoFocusCancel() else updatePreview(withSemaphore = false)
            }
        }
    }
}
