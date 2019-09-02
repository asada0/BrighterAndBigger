//
//  MainActivity.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/08/20.
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaActionSound
import android.media.SoundPool
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.media.ExifInterface
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Size
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.*
import android.view.accessibility.AccessibilityManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.*
import android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.popup_image_source.view.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class MainActivity : Activity(), SensorEventListener {
    private val tag: String = "BB-MainActivity"

    // Constant Values
    companion object {
        const val SHOW_ALERT = false
        const val BB_PERMISSIONS_CAMERA_STORAGE = 42171
        const val INTENT_RESULT = 80713
        const val UI_HIDDEN_INTERVAL = 10000L // 10 sec
        const val UI_HIDDEN_DURATION = 200L // 0.2 sec
        const val SPIN_HIDDEN_INTERVAL = 1000L // 1 sec

        const val STATUS_INTERVAL = 200L // 0.2 sec
        const val CAPTURE_ANIMATION_TIME = 1000L // 1 sec
        const val BUTTON_COLOR_DURATION = 500L // 0.5 sec
        const val JPEG_QUALITY = 60 // 60%

        const val kBigIconRatio = 1.4f
        const val kBigTextRatio = 1.3f

        const val BRIGHTNESS_MIN = -0.25f
        const val BRIGHTNESS_MAX = 0.25f
        const val BRIGHTNESS_STEP = 0.05f
        const val BRIGHTNESS_DEFAULT = 0.0f
        const val BRIGHTER_ADJUST_TIMES = 20

        const val ZOOM_MIN = 1.0f
        const val ZOOM_MAX_1X = 10.0f // depend on settings
        const val ZOOM_MAX_2X = 20.0f // depend on settings
        const val ZOOM_STEP_1X = 0.1f
        const val ZOOM_STEP_2X = 0.2f
        const val ZOOM_DEFAULT = 1.0f
        const val BIGGER_ADJUST_TIMES = 1

        const val CONTRAST_MIN = 0.0f
        const val CONTRAST_MAX = 1.0f
        const val CONTRAST_STEP = 0.1f
        const val CONTRAST_DEFAULT = 0.0f
        const val CLEARER_ADJUST_TIMES = 10

        const val PROJECTION_MIN = 0.2f
        const val PROJECTION_MAX = 1.0f
        // const val PROJECTION_STEP = 0.01f
        const val PROJECTION_DEFAULT = 0.5f

        const val OFFSET_X_POPUP = 20

        const val SHAKE_SENSITIVITY = 2.5f
        const val SHAKE_INTERVAL_TIME = 500
        const val SHAKE_RESET_TIME = 1000
        const val SHAKE_COUNT_MAX = 2

        const val GLOBAL_LAYOUT_CALLED_MAX = 10
        const val TROUBLE_ID = "TROUBLE_ID"
    }
    enum class PinchGestureFor { NONE, ZOOM, PROJECTION }
    enum class ImageFrom { CAMERA, FREEZE, FILE }

    // Private Variables
    private lateinit var mContext: Context
    private var mViewCamera: GLSurfaceView? = null
    private var mViewFile: GLSurfaceView? = null
    private var mRendererCamera: BBGLRendererCamera? = null
    private var mRendererFile: BBGLRendererFile? = null
    private lateinit var mPref: BBPreference

    private var mUITimerHandler: Handler? = null
    private var mUITimerRunnable: Runnable? = null
    private var mStatusTimerHandler: Handler? = null
    private var mStatusTimerRunnable: Runnable? = null
    private var mSpinTimerHandler: Handler? = null
    private var mSpinTimerRunnable: Runnable? = null
    private var mBroadCastReceiverSave: BroadcastReceiver? = null
    private var mBroadCastReceiverFreeze: BroadcastReceiver? = null
    private var mBroadCastReceiverCameraTrouble: BroadcastReceiver? = null

    private var mAnimeShowSpinners: TranslateAnimation? = null
    private var mAnimeHideSpinners: TranslateAnimation? = null

    private var mSoundShutter: MediaActionSound? = null
    private var mSoundBeep: SoundPool? = null
    private var mSoundCHI: Int = 0
    private var mError: BBError? = null

    private var mIsShowingUI: Boolean = false
    private var mLabelBrightness: TextView? = null
    private var mLabelZoom: TextView? = null
    private var mLabelContrast: TextView? = null

    private var mLabelBrightnessNumber: TextView? = null
    private var mLabelZoomNumber: TextView? = null
    private var mLabelContrastNumber: TextView? = null

    private var mPopupImageSource: PopupWindow? = null
    private var mPopupImageSourceLayout: View? = null
    private var mRadioRearCamera: RadioButton? = null
    private var mRadioFrontCamera: RadioButton? = null
    private var mRadioFile: RadioButton? = null
    private var mPrePointercount = 0
    private var mPreX: Float = 0.0f
    private var mPreY: Float = 0.0f
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mWhichGesture = PinchGestureFor.NONE
    private var mTapDetector: GestureDetector? = null
    private var mDoubleTapTwoFingerDetector: TwoFingerDoubleTapDetector? = null
    private var mFileTextureBitmap: Bitmap? = null
    private var mIntentData: Intent? = null
    private var mIsCallFromRestoreInstanceState: Boolean = false

    private var mPickerBrightness: asada0.android.brighterbigger.numberpicker.NumberPicker? = null
    private var mPickerMagnification: asada0.android.brighterbigger.numberpicker.NumberPicker? = null
    private var mPickerContrast: asada0.android.brighterbigger.numberpicker.NumberPicker? = null

    // Exist Cameras
    private var mHasRearCamera: Boolean = true
    private var mHasFrontCamera: Boolean = true
    private var mDeviceHasCameraFlash: Boolean = true

    // Permissions
    private var mHasCameraPermission: Boolean = false
    private var mHasStoragePermission: Boolean = false
    private var mDoAfterPermission: () -> Unit = {}

    // UI Tools
    private var mButtons = arrayOfNulls<Button>(0)
    private var m4thButtons = arrayOfNulls<Button>(0)
    private var mPickers = arrayOfNulls<asada0.android.brighterbigger.numberpicker.NumberPicker>(0)
    private var mStatusLabels = arrayOfNulls<TextView>(0)
    private var mIcons = arrayOfNulls<ImageView>(0)
    private var mImageViews = arrayOfNulls<ImageView>(0)
    private var mSpinLabels = arrayOfNulls<TextView>(0)
    private var mRadios = arrayOfNulls<Button>(0)

    // UI Defaults in SP
    private val mDefaultButtonSize: Size = Size(40, 30)
    private val mDefaultStatusFontSize: Float = 14.0f
    private val mDefaultIconSize: Size = Size(22, 22)

    // Max Zoom
    private var mZoomMax: Float = ZOOM_MAX_1X
    private var mZoomStep: Float = ZOOM_STEP_1X

    // Shake Control
    private var mShakeTime = 0L
    private var mShakeCount = 0

    // for stopping onGlobalLayout called repeatedly
    private var mNumOfGlobalLayoutCalled = 0

    // Private Variables with setter
    private var mBrightness: Float = BRIGHTNESS_DEFAULT // -0.25 to +0.25
        set(brightness) {
            field = kotlin.math.min(kotlin.math.max(BRIGHTNESS_MIN, brightness), BRIGHTNESS_MAX)
            mRendererCamera!!.mBrightness = field
            mRendererFile!!.mBrightness = field
        }

    private var mContrast: Float = CONTRAST_DEFAULT // 0.0 to 1.0
        set(contrast) {
            field = kotlin.math.min(kotlin.math.max(CONTRAST_MIN, contrast), CONTRAST_MAX)
            mRendererCamera!!.mContrast = field
            mRendererFile!!.mContrast = field
        }

    private var mMagnification: Float = ZOOM_DEFAULT // 1.0 to (10.0 or 20.0)
        set(magnification) {
            field = kotlin.math.min(kotlin.math.max(ZOOM_MIN, magnification), if (!mIsCallFromRestoreInstanceState) mZoomMax else ZOOM_MAX_2X)
            val (panX, panY) = adjustTextureOffset(mPanX, mPanY, field)
            mPanX = panX
            mPanY = panY
            mRendererCamera!!.mMagnification = field
            mRendererFile!!.mMagnification = field
        }

    private var mProjection: Boolean = false
        set(projection) {
            field = projection
            mRendererCamera!!.mProjection = field
            mRendererFile!!.mProjection = field
            button_projection.isSelected = field
        }

    private var mLight: Boolean = false
        set(on) {
            field = on
            mRendererCamera!!.torchLight(on = field, immediately = mLightImmediately)
            button_light.isSelected = field
        }

    private var mLightImmediately: Boolean = false

    private var mPause: Boolean = false
        set(pause) {
            field = pause
            mRendererCamera!!.mPause = field
            mRendererFile!!.mPause = field
            button_pause.isSelected = field
            button_cont_autofocus_off.isEnabled = !field && (mIsImageFrom == ImageFrom.CAMERA) && !mSettingNoTapFocusAnyway
            button_light.isEnabled = !field && (mIsImageFrom == ImageFrom.CAMERA) && cameraHasLight()
        }

    private var mReverse: Boolean = false
        set(reverse) {
            field = reverse
            mRendererCamera!!.mReverse = field
            mRendererFile!!.mReverse = field
            button_reverse.isSelected = field
        }

    private var mPanX: Float = 0.0f
        set(x) {
            field = x
            mRendererCamera!!.mPanX = field
            mRendererFile!!.mPanX = field
        }

    private var mPanY: Float = 0.0f
        set(y) {
            field = y
            mRendererCamera!!.mPanY = field
            mRendererFile!!.mPanY = field
        }

    private var mIsImageFrom: ImageFrom = ImageFrom.CAMERA
        set(from) {
            field = from
            mRendererCamera!!.mIsImageFrom = field
            mRendererFile!!.mIsImageFrom = field
            button_cont_autofocus_off.isEnabled = (field == ImageFrom.CAMERA) && !mPause && !mSettingNoTapFocusAnyway
            button_light.isEnabled = (field == ImageFrom.CAMERA) && !mPause && cameraHasLight()
            button_pause.isEnabled = (field != ImageFrom.FILE)
        }

    // Variables set in Settings
    private var mSettingMonoMode: Boolean = false
        set(monoMode) {
            field = monoMode
            mRendererCamera!!.mMonoMode = field
            mRendererFile!!.mMonoMode = field
        }

    private var mSettingMonoLightColor: Int = Color.WHITE
        set(monoLightColor) {
            field = monoLightColor
            mRendererCamera!!.mMonoLightColor = field
            mRendererFile!!.mMonoLightColor = field
        }

    private var mSettingMonoDarkColor: Int = Color.BLACK
        set(monoDarkColor) {
            field = monoDarkColor
            mRendererCamera!!.mMonoDarkColor = field
            mRendererFile!!.mMonoDarkColor = field
        }

    private var mSettingToneRotation: Boolean = false

    private var mSettingBigIcons: Boolean = false
        set(big) {
            if (field != big) {
                changeButtonAndIconSize(if (big) kBigIconRatio else 1.0f)
                changeStatusFontSize(if (big) kBigTextRatio else 1.0f)
            }
            field = big
        }

    private var mSettingLongPressPause: Boolean = true

    private var mSettingVolumeButtonShutter: Boolean = false

    private var mSettingMaxZoom2X: Boolean = false
        set(x2) {
            if (field != x2) {
                field = x2
                moveMagnificationPicker(ZOOM_DEFAULT)
                mZoomMax = if (!field) ZOOM_MAX_1X else ZOOM_MAX_2X
                mZoomStep = if (!field) ZOOM_STEP_1X else ZOOM_STEP_2X
                setupMagnificationWheel()
                if (mMagnification > mZoomMax) {
                    mMagnification = mZoomMax
                }
                moveMagnificationPicker(mMagnification)
            }
        }

    private var mChangeFocusModeOnNextWindowFocused = true
    private var mSettingContAutoFocusOff: Boolean = false
        set(on) {
            field = on
            if (!mChangeFocusModeOnNextWindowFocused) {
                 mRendererCamera!!.continuousFocus(!on)
            }
            button_cont_autofocus_off.isSelected = on
        }

    private var mSettingProjectionBottomRatio: Float = PROJECTION_DEFAULT
        set(projectionBottomRatio) {
            field = kotlin.math.min(kotlin.math.max(PROJECTION_MIN, projectionBottomRatio), PROJECTION_MAX)
            mRendererCamera!!.mProjectionBottomRatio = field
            mRendererFile!!.mProjectionBottomRatio = field
        }

    private var mSettingNoTapFocusOnContinuousMode = false
        set(on) {
            field = on
            mRendererCamera!!.noTapFocusOnContinuousMode(field)
        }

    private var mSettingNoTapFocusAnyway = false
        set(on) {
            field = on
            mRendererCamera!!.noTapFocusAnyway(field)
        }

    // end define Variables

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Start
        init()
    }

    // App Initialization after permission check
    private fun init() {
        mContext = this.applicationContext
        setContentView(R.layout.activity_main)

        // Version Update
        checkAppVersion(mContext)

        // Setup mError
        mError = BBError(this)

        // Check Camera existence and permission
        mHasRearCamera = hasRearCamera()
        mHasFrontCamera = hasFrontCamera()
        mDeviceHasCameraFlash = hasCameraFlash()
        mHasCameraPermission = hasCameraPermission()
        mHasStoragePermission = hasStoragePermission()

        requestPermission((mHasRearCamera || mHasFrontCamera) && !mHasCameraPermission, !mHasStoragePermission) {}

        // Initialize View and Renderer
        initViewAndRendererCamera()
        initViewAndRendererFile()

        // Setup Gestures
        setupGestures()

        // Setup Color Vision Type and Zoom text label
        setupTextLabels()

        // Setup Settings button
        setupSettingsButton()

        // Setup Projection button
        setupProjectionButton()

        // Setup Light button
        setupLightButton()

        // Setup Disable Cont. Auto Focus button
        setupContAutoFocusOffButton()

        // Setup Pause button
        setupPauseButton()

        // Setup Reverse button
        setupReverseButton()

        // Setup Save buttons
        setupSaveButton()

        // Setup Image Source button
        setupImageSourceButton()

        // Setup Image Source radio buttons
        setupImageSourceRadios()

        // Setup User Interface Tools Array
        setupUIArray()

        // Setup Pickers
        setupPickers()

        // Show U/I
        showUI()
    }

    private fun Number.spToPx(context: Context) = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, this.toFloat(), context.resources.displayMetrics).toInt()

    override fun onResume() {
        super.onResume()

        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        if (gl_surface_view_camera != null) {
            gl_surface_view_camera.onResume()
        }
        if (gl_surface_view_file != null) {
            gl_surface_view_file.onResume()
        }

        // Setup Broadcast
        setupFileSaveIntent()
        setupFreezeIntent()
        setupReceiveCameraTroubleIntent()

        // Setup Timer
        setupTimers()

        // Setup Sound
        setupSound()

        // Read Setting Preferences
        mChangeFocusModeOnNextWindowFocused = true
        preferencesToSettings()

        // Enable Buttons
        enableAllButtons(true)

        // showUI
        showUI()
    }

    override fun onPause() {
        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm.unregisterListener(this)

        if (gl_surface_view_camera != null) {
            gl_surface_view_camera.onPause()
        }
        if (gl_surface_view_file != null) {
            gl_surface_view_file.onPause()
        }

        // Turn off the Light
        mLight = false

        // Close camera
        if (mIsImageFrom == ImageFrom.CAMERA) {
            mRendererCamera!!.closeCamera()
        }

        // Release broadcast
        releaseFileSaveIntent()
        releaseFreezeIntent()
        releaseNoTapFocusOnContinuousModeIntent()

        // Release timers
        releaseTimers()

        // Release sound
        releaseSound()

        super.onPause()
    }

    override fun onDestroy() {
        if (mFileTextureBitmap != null) {
            mFileTextureBitmap!!.recycle()
        }

        // if (mRestartActivityFlag) startActivity(Intent(mContext, MainActivity::class.java))

        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasForcused: Boolean) {
        // Setup Animations
        if (!hasForcused) return
        setupAnimation()
        if (mChangeFocusModeOnNextWindowFocused) {
            mRendererCamera!!.continuousFocus(!mSettingContAutoFocusOff)
            mChangeFocusModeOnNextWindowFocused = false
        }
        showUI()
    }

    override fun onSaveInstanceState(saveInstanceState: Bundle) {
        super.onSaveInstanceState(saveInstanceState)

        saveInstanceState.putFloat("PanX", mPanX)
        saveInstanceState.putFloat("PanY", mPanY)
        saveInstanceState.putFloat("Brightness", mBrightness)
        saveInstanceState.putFloat("Zoom", mMagnification)
        saveInstanceState.putFloat("Contrast", mContrast)
        saveInstanceState.putBoolean("Projection", mProjection)
        saveInstanceState.putBoolean("Light", mLight)
        saveInstanceState.putBoolean("Pause", mPause)
        saveInstanceState.putBoolean("Reverse", mReverse)
        saveInstanceState.putSerializable("CameraPosition", mRendererCamera!!.mCameraPosition)
        saveInstanceState.putInt("MaxTextureSize", mRendererCamera!!.mMaxTextureSize)
        saveInstanceState.putSerializable("IsImageFrom", mIsImageFrom)
        if (mIsImageFrom == ImageFrom.FILE) {
            saveInstanceState.putParcelable("IntentData", mIntentData)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        mPanX = savedInstanceState.getFloat("PanX")
        mPanY = savedInstanceState.getFloat("PanY")
        mIsCallFromRestoreInstanceState = true
        mMagnification = savedInstanceState.getFloat("Zoom")
        mIsCallFromRestoreInstanceState = false
        mBrightness = savedInstanceState.getFloat("Brightness")
        mContrast = savedInstanceState.getFloat("Contrast")
        setupPickers()
        mProjection = savedInstanceState.getBoolean("Projection")
        mLight = savedInstanceState.getBoolean("Light")
        mPause = savedInstanceState.getBoolean("Pause")
        mReverse = savedInstanceState.getBoolean("Reverse")
        mRendererCamera!!.mCameraPosition = savedInstanceState.get("CameraPosition") as BBCamera.CameraPosition
        if (mRendererCamera!!.mCamera == null) {
            mRendererCamera!!.setupTextureCameraShader()
        }
        mRendererCamera!!.mMaxTextureSize = savedInstanceState.getInt("MaxTextureSize")
        mIsImageFrom = savedInstanceState.get("IsImageFrom") as ImageFrom
        if (mIsImageFrom == ImageFrom.FILE) {
            mIntentData = savedInstanceState.getParcelable("IntentData")
            mIsImageFrom = ImageFrom.CAMERA
            mIsCallFromRestoreInstanceState = true
            onActivityResult(INTENT_RESULT, RESULT_OK, mIntentData)
            mIsCallFromRestoreInstanceState = false
        } else if (mIsImageFrom == ImageFrom.FREEZE) {
            mPause = false
            setImageFromCamera()
        }
        enableAllButtons(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        resetPan()
        if (mIsImageFrom != ImageFrom.CAMERA) {
            resetZoom()
        }
        closePopups()
        showUI()
        mNumOfGlobalLayoutCalled = 0
        setupGlobalLayoutListener()
    }

    // Setup Global Layout Listener for small screen devices
    private fun setupGlobalLayoutListener() {
        val observer: ViewTreeObserver  = button_image_source.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (mIsShowingUI) {
                    // Show or not fourth Buttons
                    val newVisibility = if (can4thButtonsShow()) View.VISIBLE else View.GONE
                    if (button_image_source.visibility != newVisibility) {
                        button_cont_autofocus_off.visibility = newVisibility
                        button_image_source.visibility = newVisibility
                        observer.removeOnGlobalLayoutListener(this)
                    }
                } else {
                    observer.removeOnGlobalLayoutListener(this)
                }
                mNumOfGlobalLayoutCalled++
                if (mNumOfGlobalLayoutCalled > GLOBAL_LAYOUT_CALLED_MAX) {
                    observer.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    private fun can4thButtonsShow(): Boolean {
        val buttonHeight = (mDefaultButtonSize.height * (if (mSettingBigIcons) kBigIconRatio else 1.0f)).spToPx(mContext)
        val statusHeight = text_status_brighter.height
        return getDisplaySize().height / 2 > (buttonHeight + statusHeight) * 4
    }

    private fun preferencesToSettings() {
        mPref = BBPreference(this)
        mSettingMonoMode = mPref.mMonoMode
        mSettingMonoLightColor = mPref.mColorsLight[mPref.mUsedColor]
        mSettingMonoDarkColor = mPref.mColorsDark[mPref.mUsedColor]
        mSettingToneRotation = mPref.mToneRotation
        mSettingBigIcons = mPref.mBigIcons
        mSettingLongPressPause = mPref.mLongPressPause
        mSettingVolumeButtonShutter = mPref.mVolumeButtonShutter
        mSettingMaxZoom2X = mPref.mMaxZoom2X
        mSettingContAutoFocusOff = mPref.mStopContAutoFocus
        mSettingProjectionBottomRatio = (mPref.mProjectionBottomRatioInt / 100f) + PROJECTION_MIN
        mSettingNoTapFocusOnContinuousMode = mPref.isOccurredTrouble(BBPreference.TROUBLE_NO_TAP_FOCUS_ON_CONTINUOUS)
        mSettingNoTapFocusAnyway = mPref.isOccurredTrouble(BBPreference.TROUBLE_NO_TAP_FOCUS_ANYWAY)
    }

    private fun settingsToPreferences() {
        mPref = BBPreference(this)
        mPref.mMonoMode = mSettingMonoMode
        mPref.mToneRotation = mSettingToneRotation
        mPref.mBigIcons = mSettingBigIcons
        mPref.mLongPressPause = mSettingLongPressPause
        mPref.mVolumeButtonShutter = mSettingVolumeButtonShutter
        mPref.mMaxZoom2X = mSettingMaxZoom2X
        mPref.mStopContAutoFocus = mSettingContAutoFocusOff
        mPref.mProjectionBottomRatio = mSettingProjectionBottomRatio
        if (mSettingNoTapFocusOnContinuousMode) {
            mPref.mOccurredTrouble = BBPreference.TROUBLE_NO_TAP_FOCUS_ON_CONTINUOUS
        }
        if (mSettingNoTapFocusAnyway) {
            mPref.mOccurredTrouble = BBPreference.TROUBLE_NO_TAP_FOCUS_ANYWAY
        }
    }

    private fun hasRearCamera(): Boolean {
        return this.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    private fun hasFrontCamera(): Boolean {
        return this.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }

    private fun hasCameraFlash(): Boolean {
        return this.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun cameraHasLight(): Boolean {
        return when (mRendererCamera!!.hasLight()) {
            BBCamera.UYN.YES -> true
            BBCamera.UYN.NO -> false
            BBCamera.UYN.UNKNOWN -> mDeviceHasCameraFlash
        }
    }

    private fun requestPermission(fCamera: Boolean, fStorage: Boolean, doAfterPermission: () -> Unit) {
        var mWaitingRationaleCamera = false
        var mWaitingRationaleStorage = false
        val permissions = mutableListOf<String>()
        if (fCamera) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                mWaitingRationaleCamera = true
                val afterAlert1: () -> Unit = {
                    mWaitingRationaleCamera = false
                    if (!mWaitingRationaleStorage && permissions.isNotEmpty()) {
                        // Request CAMERA and/or WRITE_EXTERNAL_STORAGE permission
                        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), BB_PERMISSIONS_CAMERA_STORAGE)
                    }
                    showUI()
                }
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
                        .setMessage(R.string.sCameraReason)
                        .setPositiveButton(R.string.sDismiss) { _, _ -> afterAlert1() }
                        .setOnCancelListener { afterAlert1() }
                        .show()
            }
            permissions.add(Manifest.permission.CAMERA)
        }
        if (fStorage) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                mWaitingRationaleStorage = true
                val afterAlert2: () -> Unit = {
                    mWaitingRationaleStorage = false
                    if (!mWaitingRationaleCamera && permissions.isNotEmpty()) {
                        // Request CAMERA and/or WRITE_EXTERNAL_STORAGE permission
                        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), BB_PERMISSIONS_CAMERA_STORAGE)
                    }
                    showUI()
                }
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
                        .setMessage(R.string.sStorageReason)
                        .setPositiveButton(R.string.sDismiss) { _, _ -> afterAlert2() }
                        .setOnCancelListener { afterAlert2() }
                        .show()
            }
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!mWaitingRationaleCamera && !mWaitingRationaleStorage && permissions.isNotEmpty()) {
            // Request CAMERA and/or WRITE_EXTERNAL_STORAGE permission
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), BB_PERMISSIONS_CAMERA_STORAGE)
        }
        mDoAfterPermission = doAfterPermission
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != BB_PERMISSIONS_CAMERA_STORAGE) return
        permissions.forEachIndexed { index, kind ->
            when (kind) {
                Manifest.permission.CAMERA -> {
                    mHasCameraPermission = (grantResults[index] == PackageManager.PERMISSION_GRANTED)
                    if (!mHasCameraPermission) {
                        mError!!.log(tag, "Camera permission request is refused.")
                    }
                }
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    mHasStoragePermission = (grantResults[index] == PackageManager.PERMISSION_GRANTED)
                    if (!mHasStoragePermission) {
                        mError!!.log(tag, "Storage Read/Write permission request is refused.")
                    }
                }
            }
        }
        // Execute user function if permissions are granted successfully
        mDoAfterPermission()
        mDoAfterPermission = {}
        showUI()
    }

    private fun initViewAndRendererCamera() {
        // Image from Camera
        mViewCamera = gl_surface_view_camera
        mViewCamera!!.setEGLContextClientVersion(2)
        mRendererCamera = BBGLRendererCamera(this.applicationContext, this)
        mViewCamera!!.setRenderer(mRendererCamera)
        when {
            mHasRearCamera -> {
                mRendererCamera!!.mCameraPosition = BBCamera.CameraPosition.BACK
            }
            mHasFrontCamera -> {
                mRendererCamera!!.mCameraPosition = BBCamera.CameraPosition.FRONT
            }
            else -> {
                mRendererCamera!!.mCameraPosition = BBCamera.CameraPosition.NONE
                // Stop continues rendering
                mViewCamera!!.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                mError!!.log(tag, "initViewAndRendererCamera error - Neither rear nor front camera.")
            }
        }
        mViewCamera!!.visibility = View.VISIBLE
    }


    private fun initViewAndRendererFile() {
        // Image from File
        mViewFile = gl_surface_view_file
        mViewFile!!.setEGLContextClientVersion(2)
        mRendererFile = BBGLRendererFile(this.applicationContext, this)
        mViewFile!!.setRenderer(mRendererFile)
        mViewFile!!.visibility = View.INVISIBLE
    }

    private fun adjustZoom(zoom: Float): Float {
        val hvAdjustingRatio = if (mIsImageFrom == ImageFrom.CAMERA) mRendererCamera!!.getHvAdjustingRatio() else mRendererFile!!.getHvAdjustingRatio()
        val minZoom: Float = kotlin.math.min(hvAdjustingRatio.x / hvAdjustingRatio.y, hvAdjustingRatio.y / hvAdjustingRatio.x)
        return kotlin.math.min(kotlin.math.max(minZoom, zoom), mZoomMax)
    }

    private fun setupPickers() {
        setupBrightnessPicker()
        setupMagnificationPicker()
        setupContrastPicker()
        moveBrightnessPicker(mBrightness)
        moveMagnificationPicker(mMagnification)
        moveContrastPicker(mContrast)
    }

    private fun setupBrightnessPicker() {
        mPickerBrightness = picker_brightness
        mPickerBrightness!!.minValue = 0
        mPickerBrightness!!.maxValue = ((BRIGHTNESS_MAX - BRIGHTNESS_MIN) / BRIGHTNESS_STEP).toInt()

        val graduation= Array(mPickerBrightness!!.maxValue - mPickerBrightness!!.minValue + 1){" "}
        for (i in mPickerBrightness!!.minValue until mPickerBrightness!!.maxValue + 1)
            graduation[i] = getString(R.string.sFormatBrighterNumber).format(brightnessToUser(progressToBrightness(i)))
        mPickerBrightness!!.displayedValues = graduation

        mPickerBrightness!!.value = ((BRIGHTNESS_MAX - BRIGHTNESS_MIN) / BRIGHTNESS_STEP).toInt() / 2
        mPickerBrightness!!.wrapSelectorWheel = false
        mPickerBrightness!!.setOnValueChangedListener(object : asada0.android.brighterbigger.numberpicker.NumberPicker.OnValueChangeListener {
            override fun onValueChange(picker: asada0.android.brighterbigger.numberpicker.NumberPicker, oldVal: Int, newVal: Int) {
                mBrightness = progressToBrightness(newVal)
                restartUITimer()
            }
        })
        mPickerBrightness!!.setOnScrollListener(object : asada0.android.brighterbigger.numberpicker.NumberPicker.OnScrollListener {
            override fun onScrollStateChange(view: asada0.android.brighterbigger.numberpicker.NumberPicker, scrollState: Int) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    showSpinNumberOneSec(brighter = true, bigger = false, clearer = false)
                    restartUITimer()
                } else if (scrollState == SCROLL_STATE_IDLE) {
                    showSpinNumberOneSec(brighter = true, bigger = false, clearer = false)
                    updateTextLabels()
                    restartUITimer()
                }
            }
        })
    }

    private fun setupMagnificationPicker() {
        mPickerMagnification = picker_magnification
        mPickerMagnification!!.minValue = 0

        setupMagnificationWheel()

        mPickerMagnification!!.value = 0
        mPickerMagnification!!.wrapSelectorWheel = false
        mPickerMagnification!!.setOnValueChangedListener(object : asada0.android.brighterbigger.numberpicker.NumberPicker.OnValueChangeListener {
            override fun onValueChange(picker: asada0.android.brighterbigger.numberpicker.NumberPicker, oldVal: Int, newVal: Int) {
                mMagnification = progressToZoom(newVal)
                restartUITimer()
            }
        })
        mPickerMagnification!!.setOnScrollListener(object : asada0.android.brighterbigger.numberpicker.NumberPicker.OnScrollListener {
            override fun onScrollStateChange(view: asada0.android.brighterbigger.numberpicker.NumberPicker, scrollState: Int) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    showSpinNumberOneSec(brighter = false, bigger = true, clearer = false)
                    restartSpinTimer()
                    restartUITimer()
                } else if (scrollState == SCROLL_STATE_IDLE) {
                    showSpinNumberOneSec(brighter = false, bigger = true, clearer = false)
                    updateTextLabels()
                    restartUITimer()
                }
            }
        })
    }

    private fun setupMagnificationWheel() {
        mPickerMagnification!!.maxValue = ((mZoomMax - ZOOM_MIN) / mZoomStep).toInt()

        val graduation= Array(mPickerMagnification!!.maxValue - mPickerMagnification!!.minValue + 1){" "}
        for (i in mPickerMagnification!!.minValue until mPickerMagnification!!.maxValue + 1)
            graduation[i] = getString(R.string.sFormatBiggerNumber).format(progressToZoom(i))
        mPickerMagnification!!.displayedValues = graduation
    }

    private fun setupContrastPicker() {
        mPickerContrast = picker_contrast
        mPickerContrast!!.minValue = 0
        mPickerContrast!!.maxValue = ((CONTRAST_MAX - CONTRAST_MIN) / CONTRAST_STEP).toInt()

        val graduation= Array(mPickerContrast!!.maxValue - mPickerContrast!!.minValue + 1){" "}
        for (i in mPickerContrast!!.minValue until mPickerContrast!!.maxValue + 1)
            graduation[i] = getString(R.string.sFormatClearerNumber).format(contrastToUser(progressToContrast(i)))
        mPickerContrast!!.displayedValues = graduation

        mPickerContrast!!.value = 0
        mPickerContrast!!.wrapSelectorWheel = false
        mPickerContrast!!.setOnValueChangedListener(object : asada0.android.brighterbigger.numberpicker.NumberPicker.OnValueChangeListener {
            override fun onValueChange(picker: asada0.android.brighterbigger.numberpicker.NumberPicker, oldVal: Int, newVal: Int) {
                mContrast = progressToContrast(newVal)
                restartUITimer()
            }
        })
        mPickerContrast!!.setOnScrollListener(object : asada0.android.brighterbigger.numberpicker.NumberPicker.OnScrollListener {
            override fun onScrollStateChange(view: asada0.android.brighterbigger.numberpicker.NumberPicker, scrollState: Int) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    showSpinNumberOneSec(brighter = false, bigger = false, clearer = true)
                    restartSpinTimer()
                    restartUITimer()
                } else if (scrollState == SCROLL_STATE_IDLE) {
                    showSpinNumberOneSec(brighter = false, bigger = false, clearer = true)
                    updateTextLabels()
                    restartUITimer()
                }
            }
        })
    }

    // Spinner progress to Internal value
    private fun progressToZoom(progress: Int): Float {
        return ZOOM_MIN + progress * mZoomStep // 0 ~ 90 -> 1.0 ~ 10.0 (when 10 times MAX), 0 ~ 95 -> 1.0 ~ 20.0 (when 20 times MAX)
    }

    private fun progressToBrightness(progress: Int): Float {
        return BRIGHTNESS_MIN + progress * BRIGHTNESS_STEP // 0 ~ 10 -> -0.25 ~ +0.25
    }

    private fun progressToContrast(progress: Int): Float {
        return CONTRAST_MIN + progress * CONTRAST_STEP  // 0 ~ 10 -> 0.0 ~ 1.0
    }

    /*
    private fun progressToProjectionBottomRatio(progress: Int): Float {
        return PROJECTION_MIN + progress * PROJECTION_STEP // 0 ~ 75 -> kProjectionBottomMinRatio ~ 1.0
    }
    */

    // Internal value to Spinner progress
    private fun zoomToProgress(zoom: Float): Int {
        return kotlin.math.round((zoom - ZOOM_MIN) / mZoomStep).toInt() // 1.0 ~ 10.0 -> 0 ~ 90 (when 10 times MAX), 1.0 ~ 20.0 -> 0 ~ 95 (when 20 times MAX)
    }

    private fun brightnessToProgress(brightness: Float): Int {
        return kotlin.math.round((brightness - BRIGHTNESS_MIN) / BRIGHTNESS_STEP).toInt() // -0.25 ~ +0.25 -> 0 ~ 10
    }

    private fun contrastToProgress(contrast: Float): Int {
        return kotlin.math.round((contrast - CONTRAST_MIN) / CONTRAST_STEP).toInt() // 0.0 ~ 1.0 -> 0 ~ 10
    }

    /*
    private fun projectionBottomRatioToProgress(projectionBottomRatio: Float): Int {
        return kotlin.math.round((projectionBottomRatio - PROJECTION_MIN) / PROJECTION_STEP).toInt() // kProjectionBottomMinRatio ~ 1.0 -> 0 ~ 75
    }
    */

    // Internal value to User shown value
    private fun zoomToUser(zoom: Float): Float {
        return zoom * BIGGER_ADJUST_TIMES
    }

    private fun brightnessToUser(brightness: Float): Int {
        return kotlin.math.round(brightness * BRIGHTER_ADJUST_TIMES).toInt()
    }

    private fun contrastToUser(contrast: Float): Int {
        return kotlin.math.round(contrast * CLEARER_ADJUST_TIMES).toInt()
    }

    // Force move spinner
    private fun moveBrightnessPicker(brightness: Float) {
        mPickerBrightness!!.value = brightnessToProgress(brightness)
    }

    private fun moveMagnificationPicker(zoom: Float) {
        mPickerMagnification!!.value = zoomToProgress(zoom)
    }

    private fun moveContrastPicker(contrast: Float) {
        mPickerContrast!!.value = contrastToProgress(contrast)
    }

    private fun setupUIArray() {
        mButtons = arrayOf(button_settings, button_projection, button_light, button_cont_autofocus_off, button_pause, button_reverse, button_camera, button_image_source)
        m4thButtons = arrayOf(button_cont_autofocus_off, button_image_source)
        mPickers = arrayOf(picker_brightness, picker_magnification, picker_contrast)
        mStatusLabels = arrayOf(text_status_brighter, text_status_bigger, text_status_clearer)
        mImageViews = arrayOf(icon_brightness, icon_magnification, icon_contrast, view_under_brightness, view_under_magnification, view_under_contrast, view_under_brightness2, view_under_magnification2, view_under_contrast2)
        mSpinLabels = arrayOf(text_brighter_number, text_bigger_number, text_clearer_number)
        mRadios = arrayOf(mRadioRearCamera, mRadioFrontCamera, mRadioFile)
        mIcons = arrayOf(icon_brightness, icon_magnification, icon_contrast)
    }

    // Setup Setting Button
    private fun setupSettingsButton() {
        button_settings.setOnClickListener {
            settingsToPreferences()
            startActivity(Intent(this, SettingsActivity::class.java))
            beepSound()
        }
    }

    // Setup Projection Button
    private fun setupProjectionButton() {
        button_projection.setOnClickListener {
            mProjection = !mProjection
            restartUITimer()
            beepSound()
        }
    }

    // Setup Torch Button
    private fun setupLightButton() {
        button_light.setOnClickListener {
            mLightImmediately = true
            mLight = !mLight
            mLightImmediately = false
            restartUITimer()
            beepSound()
        }
    }

    // Setup Cont. Auto Focus OFF Button
    private fun setupContAutoFocusOffButton() {
        button_cont_autofocus_off.setOnClickListener {
            button_cont_autofocus_off.isEnabled = false
            mChangeFocusModeOnNextWindowFocused = false
            mSettingContAutoFocusOff = !mSettingContAutoFocusOff
            settingsToPreferences()
            restartUITimer()
            beepSound()
            button_cont_autofocus_off.isEnabled = !mSettingNoTapFocusAnyway
        }
    }

    // Setup Pause Button
    private fun setupPauseButton() {
        button_pause.setOnClickListener {
            if (mIsImageFrom != ImageFrom.FILE) {
                if (!mPause) {
                    mRendererCamera!!.freezeCaptureStart()
                    enableAllButtons(false)
                } else {
                    button_pause.isEnabled = false
                    if (mRendererCamera!!.reOpenCamera()) {
                        setImageFromCamera()
                        mPause = false
                        resetPan()
                        restartUITimer()
                        beepSound()
                    } else {
                        mError!!.log(tag, "Pause error - can not open camera.")
                    }
                    button_pause.isEnabled = true
                }
            }
        }
    }

    // Setup Reverse Button
    private fun setupReverseButton() {
        button_reverse.setOnClickListener {
            mReverse = !mReverse
            restartUITimer()
            beepSound()
        }
    }

    // Setup Save button
    private fun setupSaveButton() {
        // Push Save button
        button_camera.setOnClickListener {
            if (mIsImageFrom == ImageFrom.CAMERA) { // camera
                mRendererCamera!!.captureRequest()
            } else { // file
                mRendererFile!!.captureRequest()
            }
            enableAllButtons(false)
            restartUITimer()
            mSoundShutter!!.play(MediaActionSound.SHUTTER_CLICK)
        }
    }

    // Setup Appear Image Source Popup button
    private fun setupImageSourceButton() {
        mPopupImageSource = PopupWindow(this)
        mPopupImageSourceLayout = layoutInflater.inflate(R.layout.popup_image_source, FrameLayout(this))
        mPopupImageSource = PopupWindow(mPopupImageSourceLayout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        mPopupImageSource!!.contentView = mPopupImageSourceLayout
        mPopupImageSource!!.setBackgroundDrawable(resources.getDrawable(R.drawable.clear, null))
        mPopupImageSource!!.isOutsideTouchable = true // Closes the popup when touch outside
        mPopupImageSource!!.isFocusable = true

        // Push Appear Image Source Popup button
        button_image_source.setOnClickListener {
            val popupView: View = mPopupImageSource!!.contentView
            popupView.measure(0, 0)
            mPopupImageSource!!.showAsDropDown(it, -popupView.measuredWidth - OFFSET_X_POPUP, -(it.height + popupView.measuredHeight) / 2)
            restartUITimer()
            beepSound()
        }
    }

    // Setup Image Source radio buttons in popup
    private fun setupImageSourceRadios() {
        var isDelayRunning = false
        val mDelayHandler = Handler()
        val delayRunnable = Runnable {
            checkRadioSource(rear = false, front = false, file = false)
            enableRadioSource(rear = true, front = true, file = true)
            if (mPopupImageSource!!.isShowing) {
                mPopupImageSource!!.dismiss()
            }
            isDelayRunning = false
        }

        mRadioRearCamera = mPopupImageSourceLayout!!.radio_rear
        mRadioRearCamera!!.setOnClickListener {
            if (!isDelayRunning) {
                checkRadioSource(rear = true, front = false, file = false)
                enableRadioSource(rear = true, front = false, file = false)
                if (mHasCameraPermission) {
                    radioRearCameraPushed()
                } else {
                    requestPermission(fCamera = true, fStorage = false) { radioRearCameraPushed() }
                }
                // keep showing popup for 0.5sec
                isDelayRunning = true
                mDelayHandler.postDelayed(delayRunnable, BUTTON_COLOR_DURATION)
            }
            restartUITimer()
        }

        mRadioFrontCamera = mPopupImageSourceLayout!!.radio_front
        mRadioFrontCamera!!.setOnClickListener {
            if (!isDelayRunning) {
                checkRadioSource(rear = false, front = true, file = false)
                enableRadioSource(rear = false, front = true, file = false)
                if (mHasCameraPermission) {
                    radioFrontCameraPushed()
                } else {
                    requestPermission(fCamera = true, fStorage = false) { radioFrontCameraPushed() }
                }
                // keep showing popup for 0.5sec
                isDelayRunning = true
                mDelayHandler.postDelayed(delayRunnable, BUTTON_COLOR_DURATION)
            }
            restartUITimer()
        }

        mRadioFile = mPopupImageSourceLayout!!.radio_file
        mRadioFile!!.setOnClickListener {
            if (!isDelayRunning) {
                checkRadioSource(rear = false, front = false, file = true)
                enableRadioSource(rear = false, front = false, file = true)
                radioFilePushed()
                // keep showing popup for 0.5sec
                isDelayRunning = true
                mDelayHandler.postDelayed(delayRunnable, BUTTON_COLOR_DURATION)
            }
            restartUITimer()
        }
        // Set enable/disable of the image source buttons depending on the camera support status of the device,
        enableRadioSource(rear = true, front = true, file = true)
    }

    private fun checkRadioSource(rear: Boolean, front: Boolean, file: Boolean) {
        mRadioRearCamera!!.isChecked = rear
        mRadioFrontCamera!!.isChecked = front
        mRadioFile!!.isChecked = file
    }

    private fun enableRadioSource(rear: Boolean, front: Boolean, file: Boolean) {
        mRadioRearCamera!!.isEnabled = rear && mHasRearCamera
        mRadioFrontCamera!!.isEnabled = front && mHasFrontCamera
        mRadioFile!!.isEnabled = file
    }

    private fun radioRearCameraPushed() {
        mLight = false // Camera will be hang up if this assignment actions is done in setImageFromCamera()
        if (!mRendererCamera!!.changeCameraToRear()) {
            mError!!.log(tag, "Camera error - can not change to rear")
            mError!!.show(R.string.sCameraError)
            restartUITimer()
            return
        }
        setImageFromCamera()
        resetZoomAndPan()
        restartUITimer()
        beepSound()
    }

    private fun radioFrontCameraPushed() {
        mLight = false // Camera will be hang up if this assignment actions is done in setImageFromCamera()
        if (!mRendererCamera!!.changeCameraToFront()) {
            mError!!.log(tag, "Camera error - can not change to front")
            mError!!.show(R.string.sCameraError)
            restartUITimer()
            return
        }
        setImageFromCamera()
        resetZoomAndPan()
        restartUITimer()
        beepSound()
    }

    private fun radioFilePushed() {
        if (mPopupImageSource!!.isShowing) {
            mPopupImageSource!!.dismiss()
        }
        mLight = false
        val mimeTypes: Array<String> = arrayOf("image/jpeg", "image/png", "image/gif")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, INTENT_RESULT)
        restartUITimer()
        beepSound()
    }

    // An image has read from Gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == INTENT_RESULT) && (resultCode == RESULT_OK)) {
            if (data != null) {
                mIntentData = data
                val uri = data.data
                if (uri != null) {
                    // Check file size. If space of image > space of display, load reduced image to bitmap
                    var stream: InputStream?
                    val options = BitmapFactory.Options()
                    try {
                        stream = contentResolver.openInputStream(uri)
                    } catch (e: Exception) {
                        mError!!.log(tag, "File read error - File not found(1).")
                        mError!!.show(R.string.sFileLoadError)
                        return
                    }

                    try {
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeStream(stream, null, options)
                    } catch (e: IOException) {
                        stream.close()
                        mError!!.log(tag, "File read error - IOException(1).")
                        mError!!.show(R.string.sFileLoadError)
                        return
                    } catch (e: SecurityException) {
                        stream.close()
                        mError!!.log(tag, "File read error - SecurityException(1).")
                        mError!!.show(R.string.sFileLoadError)
                        return
                    } catch (e: OutOfMemoryError) {
                        stream.close()
                        mError!!.log(tag, "File read error - OutOfMemoryError(1).")
                        mError!!.show(R.string.sFileLoadError)
                        return
                    }

                    val displaySize: Size = getDefaultDisplaySize()
                    val timesSpace = kotlin.math.max((options.outWidth * options.outHeight.toLong()).toFloat() / (displaySize.width * displaySize.height.toLong()), 1.0f)
                    val maxSampleN = kotlin.math.ceil(kotlin.math.sqrt(timesSpace.toDouble())).toInt()
                    var minSampleN = kotlin.math.max(1, maxSampleN)

                    val maxTextureSize = mRendererCamera!!.mMaxTextureSize
                    if (maxTextureSize > 0) {
                        minSampleN = kotlin.math.ceil(kotlin.math.max(options.outWidth, options.outHeight).toFloat() / maxTextureSize).toInt()
                    }
                    val minSampleLog2 = kotlin.math.ceil(kotlin.math.log2(minSampleN.toDouble())).toInt()
                    var maxSampleLog2 = kotlin.math.ceil(kotlin.math.log2(maxSampleN.toDouble())).toInt()
                    maxSampleLog2 = kotlin.math.max(minSampleLog2, maxSampleLog2)
                    // mError!!.log(tag, "image (${options.outWidth}, ${options.outHeight}), display(${displaySize.width}, ${displaySize.height}), maxTextureSize($maxTextureSize), sampleMin: ${2.0.pow(minSampleLog2)}, sampleMax: ${2.0.pow(maxSampleLog2)}")

                    for (i in minSampleLog2..maxSampleLog2) {
                        try {
                            stream = contentResolver.openInputStream(uri)
                        } catch (e: Exception) {
                            mError!!.log(tag, "File read error - File not found(2).")
                            mError!!.show(R.string.sFileLoadError)
                            return
                        }
                        options.inJustDecodeBounds = false

                        try {
                            options.inSampleSize = 2.0.pow(i).toInt()
                            mFileTextureBitmap = BitmapFactory.decodeStream(stream, null, options)
                            if (mFileTextureBitmap == null) {
                                mError!!.log(tag, "File read error - Can not read this file.")
                                mError!!.show(R.string.sFileLoadError)
                                return
                            }
                            break
                        } catch (e: IOException) {
                            stream.close()
                            mError!!.log(tag, "File read error - IOException(2).")
                            mError!!.show(R.string.sFileLoadError)
                            return
                        } catch (e: SecurityException) {
                            stream.close()
                            mError!!.log(tag, "File read error - SecurityException(2).")
                            mError!!.show(R.string.sFileLoadError)
                            return
                        } catch (e: OutOfMemoryError) {
                            if (i < maxSampleLog2) {
                                mError!!.log(tag, "OutOfMemoryError while file reading - SampleSize: ${2.0.pow(i).toInt()}, and will try with SampleSize [${2.0.pow(i + 1).toInt()}], continue.")
                            } else {
                                stream.close()
                                mError!!.log(tag, "File read error - OutOfMemoryError, SampleSize: ${2.0.pow(i).toInt()}")
                                mError!!.show(R.string.sFileLoadError)
                                return
                            }
                        }
                    }

                    // Exif rotation
                    val rotateDegree = exifRotateDegree(uri)
                    if (rotateDegree != 0.0f) {
                        mFileTextureBitmap = rotateImage(mFileTextureBitmap!!, rotateDegree)
                    }

                    setImageFromFile(mFileTextureBitmap!!)
                    if (!mIsCallFromRestoreInstanceState) {
                        resetZoomAndPan()
                    }
                    return
                } else {
                    mError!!.log(tag, "File read error - Uri == null.")
                }
            } else {
                mError!!.log(tag, "File read error - Intent == null.")
            }
            mError!!.show(R.string.sFileLoadError)
        }
    }

    private fun exifRotateDegree(uri: Uri): Float {
        var orientation = 0
        try {
            val exifInterface = ExifInterface(contentResolver.openInputStream(uri)!!)
            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (e: Exception) {
            mError!!.log(tag, "error in exifRotateDegree, ignored.")
        }
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90.0f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180.0f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270.0f
            else -> 0.0f
        }
    }

    private fun rotateImage(bitmap: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        var rotatedImage: Bitmap? = null
        try {
            rotatedImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
        } catch (e: Exception) {
            mError!!.log(tag, "error in rotateImage, ignored.")
        }
        return rotatedImage
    }

    private fun enableAllButtons(enable: Boolean) {
        for (button in mButtons) {
            button!!.isEnabled = enable
        }
        button_pause.isEnabled = (mIsImageFrom != ImageFrom.FILE) && enable
        button_cont_autofocus_off.isEnabled = (mIsImageFrom == ImageFrom.CAMERA) && !mPause && enable && !mSettingNoTapFocusAnyway
        button_light.isEnabled = (mIsImageFrom == ImageFrom.CAMERA) && !mPause && enable && cameraHasLight()
    }

    private fun setupGestures() {
        // Zoom (Pinch in/out event)
        mScaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                if (!mProjection) {
                    mWhichGesture = PinchGestureFor.ZOOM
                } else {
                    val xy = intArrayOf(0, 0)
                    view_projection_pinch_area.getLocationOnScreen(xy)
                    val pinchAreaRect = Rect(xy[0], xy[1], xy[0] + view_projection_pinch_area.width, xy[1] + view_projection_pinch_area.height)
                    if (!pinchAreaRect.contains(detector.focusX.toInt(), detector.focusY.toInt())) {
                        mWhichGesture = PinchGestureFor.ZOOM
                    } else {
                        view_projection_pinch_area!!.visibility = View.VISIBLE
                        mWhichGesture = PinchGestureFor.PROJECTION
                    }
                }
                restartUITimer()
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                super.onScale(detector)
                val scaleFactor: Float = detector.scaleFactor
                when (mWhichGesture) {
                    PinchGestureFor.ZOOM -> {
                        mMagnification = adjustZoom(mMagnification * scaleFactor)
                    }
                    PinchGestureFor.PROJECTION -> {
                        mSettingProjectionBottomRatio = kotlin.math.min(kotlin.math.max(PROJECTION_MIN, mSettingProjectionBottomRatio * scaleFactor), PROJECTION_MAX)
                    }
                    PinchGestureFor.NONE -> {
                    }
                }
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                super.onScaleEnd(detector)
                when (mWhichGesture) {
                    PinchGestureFor.ZOOM -> {
                        moveMagnificationPicker(mMagnification)
                        mLabelZoom!!.text = getString(R.string.sFormatBigger).format(mMagnification)
                        mWhichGesture = PinchGestureFor.NONE
                    }
                    PinchGestureFor.PROJECTION -> {
                        view_projection_pinch_area!!.visibility = View.INVISIBLE
                        mWhichGesture = PinchGestureFor.NONE
                    }
                    PinchGestureFor.NONE -> {
                    }
                }
                updateTextLabels()
                restartUITimer()
            }
        })

        mTapDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(event: MotionEvent): Boolean {
                super.onDoubleTap(event)
                if (mIsShowingUI) {
                    resetAllControls()
                    restartUITimer()
                    beepSound()
                } else {
                    showUI()
                }
                return true
            }

            override fun onLongPress(event: MotionEvent) {
                super.onLongPress(event)
                if (mSettingLongPressPause) {
                    button_pause.performClick()
                }
            }

            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                super.onSingleTapConfirmed(event)
                if (!mIsShowingUI) {
                    showUI()
                    return true
                } else if ((mIsImageFrom == ImageFrom.CAMERA) && !mPause) {
                    val displaySize = getDisplaySize()
                    val ratioXY = PointF(event.x / displaySize.width, event.y / displaySize.height)
                    val zoomPanRatioXY = coordinatesWithZoomPan(ratioXY, mMagnification, mPanX, mPanY)
                    if (!mProjection) {
                        mRendererCamera!!.touchFocus(zoomPanRatioXY.x, zoomPanRatioXY.y)
                    } else {
                        val zoomPanNonProjectionRatioXY = projectionCoordinatesToNonProjectionCoordinates(zoomPanRatioXY, mSettingProjectionBottomRatio)
                        if (zoomPanNonProjectionRatioXY.x == -1.0f && zoomPanNonProjectionRatioXY.y == -1.0f) {
                            mError!!.log(tag, "Touch point is outside.")
                            return false
                        }

                        mRendererCamera!!.touchFocus(zoomPanNonProjectionRatioXY.x, zoomPanNonProjectionRatioXY.y)
                    }
                    return true
                }
                return false
            }
        })

        mDoubleTapTwoFingerDetector = object : TwoFingerDoubleTapDetector() {
            override fun onTwoFingerDoubleTap() {
                if (!mSettingToneRotation) return
                colorRotation(mContext)
                preferencesToSettings()
                restartUITimer()
                beepSound()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        event ?: return false

        // Pinch in/out
        mScaleDetector!!.onTouchEvent(event)

        // Tap, DoubleTap, LongPress event
        mTapDetector!!.onTouchEvent(event)

        // DoubleTap TwoFinger event
        mDoubleTapTwoFingerDetector!!.onTouchEvent(event)

        // if (!mIsShowingUI) return true

        // Pan
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mPreX = event.rawX
                    mPreY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!((mIsImageFrom == ImageFrom.CAMERA) && !mPause) && (mPrePointercount == 1)) {
                        val newX: Float = event.rawX
                        val newY: Float = event.rawY
                        val displaySize: Size = getDisplaySize()
                        val moveX: Float = (newX - mPreX) / displaySize.width
                        val moveY: Float = (newY - mPreY) / displaySize.height
                        val (newPanX, newPanY) = adjustTextureOffset(mPanX + moveX, mPanY - moveY, mMagnification)
                        mPanX = newPanX
                        mPanY = newPanY
                        mPreX = newX
                        mPreY = newY
                        restartUITimer()
                    } else {
                        mPreX = event.rawX
                        mPreY = event.rawY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mPreX = event.rawX
                    mPreY = event.rawY
                    updateTextLabels()
                    restartUITimer()
                }
            }
        }
        mPrePointercount = event.pointerCount

        return false
    }

    // convert raw point to zoomed and panned point
    private fun coordinatesWithZoomPan(posXY: PointF, zoom: Float, panX: Float, panY: Float): PointF {
        val hvAdjustingRatio = if (mIsImageFrom == ImageFrom.CAMERA) mRendererCamera!!.getHvAdjustingRatio() else mRendererFile!!.getHvAdjustingRatio()
        return PointF(0.5f - (0.5f - posXY.x + panX / hvAdjustingRatio.x) / zoom, 0.5f - (0.5f - posXY.y - panY / hvAdjustingRatio.y) / zoom)
    }

    // convert projection point to non projection point
    private fun projectionCoordinatesToNonProjectionCoordinates(projXY: PointF, projectionRatio: Float): PointF {
        val hvAdjustingRatio = if (mIsImageFrom == ImageFrom.CAMERA) mRendererCamera!!.getHvAdjustingRatio() else mRendererFile!!.getHvAdjustingRatio()
        val xTimes = 1.0f / hvAdjustingRatio.x
        val yTimes = 1.0f / hvAdjustingRatio.y
        val xOffset = (1.0f - xTimes) / 2.0f
        val yOffset = (1.0f - yTimes) / 2.0f
        val sp = complement(projXY)
        val cp = PointF(xOffset + sp.x * xTimes, yOffset + sp.y * yTimes)
        val cnz = (1.0f - projectionRatio) * cp.y + projectionRatio
        val cnx = (cp.x + 0.5f * (1.0f - projectionRatio) * cp.y - 0.5f * (1.0f - projectionRatio)) / cnz
        val cny = cp.y / cnz
        val sn = PointF((cnx - xOffset) / xTimes, (cny - yOffset) / yTimes)
        val nonProjXY = complement(sn)
        // check whether touch point is inside real area or not while projection
        if (sn.x < -xOffset / xTimes || 1.0 + xOffset / xTimes < sn.x || sn.y < -yOffset / yTimes || 1.0 + yOffset / yTimes < sn.y) {
            return PointF(-1.0f, -1.0f) // out of camera image
        }
        return nonProjXY
    }

    /*
    // convert non projection point to projection point
    private fun nonProjectionCoordinatesToProjectionCoordinates(nonProjXY: PointF, projectionRatio: Float): PointF {
        val hvAdjustingRatio = if (mIsImageFrom == ImageFrom.CAMERA) mRendererCamera!!.getHvAdjustingRatio() else mRendererFile!!.getHvAdjustingRatio()
        val xTimes = 1.0f / hvAdjustingRatio.x
        val yTimes = 1.0f / hvAdjustingRatio.y
        val xOffset = (1.0f - xTimes) / 2.0f
        val yOffset = (1.0f - yTimes) / 2.0f
        val sn = complement(nonProjXY)
        val cn = PointF(xOffset + sn.x * xTimes, yOffset + sn.y * yTimes)
        val cpz = (1.0f - 1.0f / projectionRatio) * cn.y + 1.0f / projectionRatio
        val cpx = (cn.x + 0.5f * (1.0f - 1.0f / projectionRatio) * cn.y - 0.5f * (1.0f - 1.0f / projectionRatio)) / cpz
        val cpy = cn.y / cpz
        val sp = PointF((cpx - xOffset) / xTimes, (cpy - yOffset) / yTimes)
        val projXY = complement(sp)
        return projXY
    }

    private fun pointWithNavbarAdjustment(projXY: PointF): PointF {
        val screenSize = getDefaultDisplaySize()
        val cameraSize = getDefaultDisplaySizeWithNavbar()
        val divX = screenSize.width.toFloat() / cameraSize.width
        val offsetX = (1.0f - divX) / 2.0f
        val divY = screenSize.height.toFloat() / cameraSize.height
        val offsetY = (1.0f - divY) / 2.0f
        return PointF(offsetX + projXY.x * divX, offsetY + projXY.y * divY)
    }

    private fun pointWithoutNavbarAdjustment(projXY: PointF): PointF {
        val screenSize = getDefaultDisplaySize()
        val cameraSize = getDefaultDisplaySizeWithNavbar()
        val divX = cameraSize.width.toFloat() / screenSize.width
        val offsetX = (divX - 1.0f) / 2.0f
        val divY = cameraSize.height.toFloat() / screenSize.height
        val offsetY = (divY - 1.0f) / 2.0f
        return PointF(projXY.x * divX - offsetX + , projXY.y * divY - offsetY)
    }
    */

    private fun complement(p: PointF): PointF {
        return PointF(1.0f - p.x, 1.0f - p.y)
    }

    private fun setupAnimation() {
        val height: Float = getDisplaySize().height / 2.0f
        mAnimeHideSpinners = TranslateAnimation(0.0f, 0.0f, 0.0f, height)
        mAnimeHideSpinners!!.duration = UI_HIDDEN_DURATION
        mAnimeShowSpinners = TranslateAnimation(0.0f, 0.0f, height, 0.0f)
        mAnimeShowSpinners!!.duration = UI_HIDDEN_DURATION
    }

    private fun showUI() {
        if (!mIsShowingUI) {
            for (picker in mPickers) {
                if (mAnimeShowSpinners != null) picker!!.startAnimation(mAnimeShowSpinners)
                picker!!.visibility = View.VISIBLE
            }
            for (label in mStatusLabels) {
                label!!.visibility = View.VISIBLE
            }
            for (button in mButtons) {
                button!!.visibility = View.VISIBLE
            }
            for (icon in mIcons) {
                icon!!.visibility = View.VISIBLE
            }
            for (image in mImageViews) {
                if (mAnimeShowSpinners != null) image!!.startAnimation(mAnimeShowSpinners)
                image!!.visibility = View.VISIBLE
            }
        }
        val mShow4 = can4thButtonsShow()
        for (button in m4thButtons) {
            button!!.visibility = if (mShow4) View.VISIBLE else View.GONE
        }
        updateTextLabels()
        restartUITimer()
        mIsShowingUI = true
    }

    private fun hideUI() {
        if ((getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager).isTouchExplorationEnabled) return
        if (mIsShowingUI) {
            for (spinner in mPickers) {
                if (mAnimeShowSpinners != null) spinner!!.startAnimation(mAnimeHideSpinners)
                spinner!!.visibility = View.INVISIBLE
            }
            for (label in mStatusLabels) {
                label!!.visibility = View.INVISIBLE
            }
            for (button in mButtons) {
                button!!.visibility = View.INVISIBLE
            }
            for (icon in mIcons) {
                icon!!.visibility = View.INVISIBLE
            }
            for (image in mImageViews) {
                if (mAnimeShowSpinners != null) image!!.startAnimation(mAnimeHideSpinners)
                image!!.visibility = View.INVISIBLE
            }
            closePopups()
        }
        mIsShowingUI = false
    }

    private fun setupTextLabels() {
        mLabelBrightness = text_status_brighter
        mLabelBrightness!!.text = getString(R.string.sFormatBrighter).format(brightnessToUser(mBrightness))

        mLabelZoom = text_status_bigger
        mLabelZoom!!.text = getString(R.string.sFormatBigger).format(zoomToUser(mMagnification))

        mLabelContrast = text_status_clearer
        mLabelContrast!!.text = getString(R.string.sFormatClearer).format(contrastToUser(mContrast))

        mLabelBrightnessNumber = text_brighter_number
        mLabelBrightnessNumber!!.text = getString(R.string.sFormatBrighterNumber).format(brightnessToUser(mBrightness))

        mLabelZoomNumber = text_bigger_number
        mLabelZoomNumber!!.text = getString(R.string.sFormatBiggerNumber).format(zoomToUser(mMagnification))

        mLabelContrastNumber = text_clearer_number
        mLabelContrastNumber!!.text = getString(R.string.sFormatClearerNumber).format(contrastToUser(mContrast))
    }

    private fun showSpinNumberOneSec(brighter: Boolean, bigger: Boolean, clearer: Boolean) {
        mLabelBrightnessNumber!!.visibility = if (brighter) View.VISIBLE else View.INVISIBLE
        mLabelZoomNumber!!.visibility = if (bigger) View.VISIBLE else View.INVISIBLE
        mLabelContrastNumber!!.visibility = if (clearer) View.VISIBLE else View.INVISIBLE
        if (brighter || bigger || clearer)
            restartSpinTimer()
    }

    private fun setupFileSaveIntent() {
        mBroadCastReceiverSave = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val bitmap: Bitmap? = if (mIsImageFrom == ImageFrom.CAMERA) mRendererCamera!!.mCaptureBitmap!! else mRendererFile!!.mCaptureBitmap!!
                if (bitmap == null) {
                    mError!!.log(tag, "Image get error - bitmap == null(1).")
                    mError!!.show(R.string.sFileSaveError)
                }
                if (mHasStoragePermission) {
                    realSave(bitmap)
                } else {
                    requestPermission(fCamera = false, fStorage = true) { realSave(bitmap) }
                }
            }
        }
        registerReceiver(mBroadCastReceiverSave, IntentFilter(BBGLRenderer.INTENT_SAVE))
        startService(Intent(application, MainActivity::class.java))
    }

    private fun releaseFileSaveIntent() {
        if (mBroadCastReceiverSave != null) {
            try {
                unregisterReceiver(mBroadCastReceiverSave)
                mBroadCastReceiverSave = null
            } catch (e: IllegalArgumentException) {
                // Sometimes error is occurred in unregisterReceiver(mBroadCastReceiverSave)
            }
        }
    }

    private fun realSave(bitmap: Bitmap?) {
        if (bitmap == null) {
            mError!!.log(tag, "File save error - bitmap == null(2).")
            mError!!.show(R.string.sFileSaveError)
            enableAllButtons(true)
            return
        }
        if (saveBitmapToFile(bitmap)) {
            fileSaveAnimation(bitmap)
        } else {
            mError!!.show(R.string.sFileSaveError)
        }
        enableAllButtons(true)
    }

    private fun saveBitmapToFile(bitmap: Bitmap?): Boolean {
        if (bitmap == null) {
            mError!!.log(tag, "File save error - bitmap == null(3).")
            mError!!.show(R.string.sFileSaveError)
            return false
        }

        val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!storageDir.isDirectory) {
            storageDir.mkdir()
            if (!storageDir.isDirectory) {
                mError!!.log(tag, "File save error - Cannot make directory.")
                return false
            }
        }

        // Determination of file name
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US)
        val fileName = "bb_${dateFormat.format(Date())}.jpg"
        val file = File(storageDir, fileName)

        // Save JPEG image to file
        try {
            val fos = FileOutputStream(file)
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos)) {
                mError!!.log(tag, "File save error - bitmap.compress failed.")
                return false
            }
            fos.close()
        } catch (e: FileNotFoundException) {
            mError!!.log(tag, "File save error - FileNotFoundException. Maybe don't have permission.")
            return false
        } catch (e: SecurityException) {
            mError!!.log(tag, "File save error - SecurityException")
            return false
        } catch (e: IOException) {
            mError!!.log(tag, "File save error - IOException.")
            return false
        }
        // Request to register to gallery (MediaScan)
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

        mError!!.log(tag, "Saved \"$fileName\".")
        return true
    }

    private fun fileSaveAnimation(bitmap: Bitmap?) {
        if (bitmap == null) {
            mError!!.log(tag, "File save error - bitmap == null(4).")
            mError!!.show(R.string.sFileSaveError)
            return
        }
        val imageview = ImageView(this)
        imageview.setImageBitmap(bitmap)
        imageview.z = 1.0f

        val location = intArrayOf(0, 0)
        button_camera.getLocationInWindow(location)
        val animation = ScaleAnimation(
                1.0f, 0.0f, 1.0f, 0.0f,
                Animation.ABSOLUTE, location[0] + button_camera.measuredWidth / 2.0f,
                Animation.ABSOLUTE, (location[1] + button_camera.measuredHeight / 2.0f))
        animation.duration = CAPTURE_ANIMATION_TIME
        animation.repeatCount = 0
        animation.fillAfter = true
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                frame_layout.removeView(imageview)
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
        frame_layout.addView(imageview, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        imageview.startAnimation(animation)
    }

    private fun setupFreezeIntent() {
        mBroadCastReceiverFreeze = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val bitmap = mRendererCamera!!.freezeCaptureBitmap()
                if (bitmap == null) {
                    mError!!.log(tag, "Image get error - bitmap == null(5).")
                    mError!!.show(R.string.sFreezeError)
                    enableAllButtons(true)
                    return
                }
                var bitmap2: Bitmap? = null
                try {
                    bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mRendererCamera!!.freezeRotationMatrix(), true)
                } catch (e: Exception) {
                }
                if (bitmap2 == null) {
                    mError!!.log(tag, "Image get error - bitmap == null(6).")
                    mError!!.show(R.string.sFreezeError)
                    enableAllButtons(true)
                    return
                }
                setImageFreezing(bitmap2)
            }
        }
        registerReceiver(mBroadCastReceiverFreeze, IntentFilter(BBCamera.INTENT_FREEZE))
        startService(Intent(application, MainActivity::class.java))
    }

    private fun releaseFreezeIntent() {
        if (mBroadCastReceiverFreeze != null) {
            try {
                unregisterReceiver(mBroadCastReceiverFreeze)
                mBroadCastReceiverFreeze = null
            } catch (e: Exception) {
            }
        }
    }

    private fun setupReceiveCameraTroubleIntent() {
        mBroadCastReceiverCameraTrouble = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val troubleID = intent.getLongExtra(TROUBLE_ID, 0)
                if (troubleID == BBPreference.TROUBLE_NO_TAP_FOCUS_ON_CONTINUOUS) {
                    if (mRendererCamera != null) {
                        mError!!.log(tag, "Main - Received No tap focus on continuous mode intent.")
                        mSettingNoTapFocusOnContinuousMode = true
                        settingsToPreferences()
                        AlertDialog.Builder(context)
                                .setTitle(getString(R.string.sNoTapFocusOnContinuousModeTitle))
                                .setMessage(getString(R.string.sNoTapFocusOnContinuousModeMessage))
                                .setPositiveButton(getString(R.string.sDismiss)) { _, _ ->
                                    mRendererCamera!!.noTapFocusOnContinuousMode(true)
                                    mRendererCamera!!.closeCamera()
                                    mRendererCamera!!.reOpenCamera()
                                }
                                .show()
                    }
                } else if (troubleID == BBPreference.TROUBLE_NO_TAP_FOCUS_ANYWAY) {
                    if (mRendererCamera != null) {
                        mError!!.log(tag, "Main - Received No tap focus anyway intent.")
                        button_cont_autofocus_off.isEnabled = false
                        mSettingNoTapFocusAnyway = true
                        settingsToPreferences()
                        AlertDialog.Builder(context)
                                .setTitle(getString(R.string.sNoTapFocusAnywayTitle))
                                .setMessage(getString(R.string.sNoTapFocusAnywayMessage))
                                .setPositiveButton(getString(R.string.sDismiss)) { _, _ ->
                                    mRendererCamera!!.noTapFocusAnyway(true)
                                    mRendererCamera!!.closeCamera()
                                    mRendererCamera!!.reOpenCamera()
                                }
                                .show()
                    }
                }
            }
        }
        registerReceiver(mBroadCastReceiverCameraTrouble, IntentFilter(BBCamera.INTENT_CAMERA_TROUBLE))
        startService(Intent(application, MainActivity::class.java))
    }

    private fun releaseNoTapFocusOnContinuousModeIntent() {
        if (mBroadCastReceiverCameraTrouble != null) {
            try {
                unregisterReceiver(mBroadCastReceiverCameraTrouble)
                mBroadCastReceiverCameraTrouble = null
            } catch (e: Exception) {
            }
        }
    }

    private fun setupTimers() {
        // Hide Tool Panel and Status when there is no touch for 10 seconds
        mUITimerHandler = Handler()
        mUITimerRunnable = Runnable { hideUI() }
        mUITimerHandler!!.postDelayed(mUITimerRunnable, UI_HIDDEN_INTERVAL)

        // Update status on every one second
        mStatusTimerHandler = Handler()
        mStatusTimerRunnable = Runnable {
            updateTextLabels()
            mStatusTimerHandler!!.postDelayed(mStatusTimerRunnable, STATUS_INTERVAL)
        }
        mStatusTimerHandler!!.postDelayed(mStatusTimerRunnable, STATUS_INTERVAL)

        // Hide Spinner display value
        mSpinTimerHandler = Handler()
        mSpinTimerRunnable = Runnable {
            for (label in mSpinLabels)
                label!!.visibility = View.INVISIBLE
        }
    }

    private fun restartUITimer() {
        mUITimerHandler ?: return
        mUITimerRunnable ?: return
        mUITimerHandler!!.removeCallbacks(mUITimerRunnable)
        mUITimerHandler!!.postDelayed(mUITimerRunnable, UI_HIDDEN_INTERVAL)
    }

    private fun restartSpinTimer() {
        mSpinTimerHandler ?: return
        mSpinTimerRunnable ?: return
        mSpinTimerHandler!!.removeCallbacks(mSpinTimerRunnable)
        mSpinTimerHandler!!.postDelayed(mSpinTimerRunnable, SPIN_HIDDEN_INTERVAL)
    }

    private fun releaseTimers() {
        mUITimerHandler ?: return
        mStatusTimerHandler ?: return
        mSpinTimerHandler ?: return
        mUITimerHandler!!.removeCallbacks(mUITimerRunnable)
        mStatusTimerHandler!!.removeCallbacks(mStatusTimerRunnable)
        mSpinTimerHandler!!.removeCallbacks(mSpinTimerRunnable)
        mUITimerHandler = null
        mStatusTimerHandler = null
        mSpinTimerHandler = null
    }

    private fun setupSound() {
        mSoundShutter = MediaActionSound()
        mSoundShutter!!.load(MediaActionSound.SHUTTER_CLICK)

        mSoundBeep = SoundPool.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setMaxStreams(1)
                .build()
        mSoundBeep!!.setOnLoadCompleteListener { _, _, status -> if (status != 0) mError!!.show(R.string.sSoundError) }
        mSoundCHI = mSoundBeep!!.load(this, R.raw.clk1a, 1)
    }

    private fun releaseSound() {
        if (mSoundShutter != null) {
            mSoundShutter!!.release()
            mSoundShutter = null
        }

        if(mSoundBeep != null) {
            mSoundBeep!!.release()
            mSoundBeep = null
        }
    }

    private fun beepSound() {
        if (mSoundBeep != null) {
            mSoundBeep!!.play(mSoundCHI, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    private fun adjustTextureOffset(offsetX: Float, offsetY: Float, zoom: Float): Pair<Float, Float> {

        val hvAdjustingRatio = if (mIsImageFrom == ImageFrom.CAMERA) mRendererCamera!!.getHvAdjustingRatio() else mRendererFile!!.getHvAdjustingRatio()
        val ratioX: Float = kotlin.math.max(1.0f, hvAdjustingRatio.x)
        val ratioY: Float = kotlin.math.max(1.0f, hvAdjustingRatio.y)
        var newOffsetX: Float = kotlin.math.min(kotlin.math.max(0.5f - zoom / 2.0f * ratioX, offsetX), zoom / 2.0f * ratioX - 0.5f)
        var newOffsetY: Float = kotlin.math.min(kotlin.math.max(0.5f - zoom / 2.0f * ratioY, offsetY), zoom / 2.0f * ratioY - 0.5f)
        if (zoom < 1.0f) {
            val plusOffsetX: Float = if (ratioX == 1.0f) (1.0f - zoom) / 2.0f else 0.0f
            val plusOffsetY: Float = if (ratioY == 1.0f) (1.0f - zoom) / 2.0f else 0.0f
            newOffsetX += plusOffsetX
            newOffsetY += plusOffsetY
        }
        return Pair(newOffsetX, newOffsetY)
    }

    private fun updateTextLabels() {
        if (!mIsShowingUI) return

        val displaySize: Size = getDisplaySize()
        val width: Int = displaySize.width
        val height: Int = displaySize.height

        if ((width <= 0) || (height <= 0)) return

        mLabelBrightness!!.text = getString(R.string.sFormatBrighter).format(brightnessToUser(mBrightness))
        mLabelZoom!!.text = getString(R.string.sFormatBigger).format(zoomToUser(mMagnification))
        mLabelContrast!!.text = getString(R.string.sFormatClearer).format(contrastToUser(mContrast))
        mLabelBrightnessNumber!!.text = getString(R.string.sFormatBrighterNumber).format(brightnessToUser(mBrightness))
        mLabelZoomNumber!!.text = getString(R.string.sFormatBiggerNumber).format(zoomToUser(mMagnification))
        mLabelContrastNumber!!.text = getString(R.string.sFormatClearerNumber).format(contrastToUser(mContrast))
    }

    private fun closePopups() {
        mPopupImageSource!!.dismiss()
    }

    private fun getDisplaySize(): Size {
        return(Size(gl_surface_view_camera.width, gl_surface_view_camera.height))
    }

    private fun getDefaultDisplaySize(): Size {
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        return Size(point.x, point.y)
    }

    /*
    private fun getDefaultDisplaySizeWithNavbar(): Size {
        val point = Point()
        windowManager.defaultDisplay.getRealSize(point)
        return Size(point.x, point.y)
    }
    */

    private fun resetAllControls() {
        resetZoomAndPan()
        resetBrightness()
        resetContrast()
        resetBrightnessReverse()
    }

    private fun resetZoom() {
        mMagnification = 1.0f
        moveMagnificationPicker(mMagnification)
    }

    private fun resetPan() {
        mPanX = 0.0f
        mPanY = 0.0f
    }

    private fun resetZoomAndPan() {
        resetZoom()
        resetPan()
    }

    private fun resetBrightness() {
        mBrightness = 0.0f
        moveBrightnessPicker(mBrightness)
    }

    private fun resetContrast() {
        mContrast = 0.0f
        moveContrastPicker(mContrast)
    }

    private fun resetBrightnessReverse() {
        mReverse = false
    }

    private fun setImageFromCamera() {
        mIsImageFrom = ImageFrom.CAMERA
        mRendererFile!!.unsetFileTextureBitmap()
        showCameraView()
        mPause = false
    }

    private fun setImageFreezing(bitmap: Bitmap?) {
        if (bitmap != null) {
            if (mIsImageFrom == ImageFrom.CAMERA) {
                mRendererCamera!!.closeCamera()
                mLight = false
            }
            mIsImageFrom = ImageFrom.FREEZE
            mRendererFile!!.setFileTextureBitmap(bitmap)
            showFileView()
            if (!mIsCallFromRestoreInstanceState) {
                mPause = true
                enableAllButtons(true)
                restartUITimer()
                beepSound()
            }
        }
    }

    private fun setImageFromFile(bitmap: Bitmap?) {
        if (bitmap != null) {
            if (mIsImageFrom == ImageFrom.CAMERA) {
                mRendererCamera!!.closeCamera()
                mLight = false
            }
            mIsImageFrom = ImageFrom.FILE
            mRendererFile!!.setFileTextureBitmap(bitmap)
            showFileView()
            mPause = false
        }
    }

    private fun showCameraView() {
        if (gl_surface_view_camera != null) {
            gl_surface_view_camera.visibility = View.VISIBLE
        }

        if (gl_surface_view_file != null) {
            gl_surface_view_file.visibility = View.INVISIBLE
        }
    }

    private fun showFileView() {
        if (gl_surface_view_camera != null) {
            gl_surface_view_camera.visibility = View.INVISIBLE
        }

        if (gl_surface_view_file != null) {
            gl_surface_view_file.visibility = View.VISIBLE
        }
    }

    private fun changeButtonAndIconSize(times: Float) {
        for (button in mButtons) {
            val layout: ConstraintLayout.LayoutParams =  button!!.layoutParams as ConstraintLayout.LayoutParams
            layout.width = (mDefaultButtonSize.width * times).spToPx(mContext)
            layout.height = (mDefaultButtonSize.height * times).spToPx(mContext)
            button.layoutParams = layout
        }
        for (icon in mIcons) {
            val layout: ConstraintLayout.LayoutParams =  icon!!.layoutParams as ConstraintLayout.LayoutParams
            layout.width = (mDefaultIconSize.width * times).spToPx(mContext)
            layout.height = (mDefaultIconSize.height * times).spToPx(mContext)
            icon.layoutParams = layout
        }
        for (radio in mRadios) {
            radio!!.width =(mDefaultButtonSize.width * times).spToPx(mContext)
            radio.height = (mDefaultButtonSize.height * times).spToPx(mContext)
        }
    }

    private fun changeStatusFontSize(times: Float) {
        for (label in mStatusLabels) {
            label!!.setTextSize(COMPLEX_UNIT_PX, (mDefaultStatusFontSize * times).spToPx(mContext).toFloat())
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
                if (mSettingVolumeButtonShutter) {
                    button_camera.performClick()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun colorRotation(context: Context) {
        val pref = BBPreference(context)
        if (pref.mMonoMode) {
            pref.mUsedColor++
        } else {
            pref.mUsedColor = 0
        }

        val mColorsNum = context.resources.getStringArray(R.array.pref_color_dic).size
        if (pref.mUsedColor < mColorsNum) {
            pref.mDiscardColorInfo = true
            pref.mMonoMode = true
        } else {
            pref.mDiscardColorInfo = false
            pref.mUsedColor = 0
            pref.mMonoMode = false
        }
    }

    private fun checkAppVersion(context: Context) {
        val versionCode : Int = BuildConfig.VERSION_CODE

        val pref = BBPreference(context)
        if (pref.mVersionCode == 0) {
            pref.mVersionCode = versionCode
        } else if (pref.mVersionCode < versionCode) {

            // Preferences migration here....

            pref.mVersionCode = versionCode
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        p0 ?: return
        val x = p0.values[0] / SensorManager.GRAVITY_EARTH
        val y = p0.values[1] / SensorManager.GRAVITY_EARTH
        val z = p0.values[2] / SensorManager.GRAVITY_EARTH
        val force = kotlin.math.sqrt(x * x + y * y + z * z)

        if (force > SHAKE_SENSITIVITY) {
            val now = System.currentTimeMillis()
            if (mShakeTime + SHAKE_INTERVAL_TIME > now) {
                return
            }

            if (mShakeTime + SHAKE_RESET_TIME < now) {
                mShakeCount = 0
            }

            mShakeTime = now
            mShakeCount++

            if (mShakeCount >= SHAKE_COUNT_MAX) {
                if (mIsShowingUI) {
                    if (mPause) {
                        button_pause.performClick()
                    }
                    mProjection = false
                    resetAllControls()
                    restartUITimer()
                    beepSound()
                } else {
                    showUI()
                }
                mShakeCount = 0
            }
        }
    }
}
