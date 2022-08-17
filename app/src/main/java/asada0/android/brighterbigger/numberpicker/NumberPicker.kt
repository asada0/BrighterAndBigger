@file:Suppress("KDocUnresolvedReference")

package asada0.android.brighterbigger.numberpicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.*
import android.view.LayoutInflater.Filter
import android.view.accessibility.AccessibilityEvent
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import asada0.android.brighterbigger.R
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A widget that enables the user to select a number from a predefined range.
 */
@Suppress("SpellCheckingInspection")
class NumberPicker
/**
 * Create a new number picker
 *
 * @param context the application environment.
 * @param attrs a collection of attributes.
 * @param defStyle The default style to apply to this view.
 */
@JvmOverloads constructor(
        /**
         * The context of this widget.
         */
        mContext: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(mContext, attrs) {

    /**
     * The text for showing the current value.
     */
    private val mSelectedText: EditText

    /**
     * The center X position of the selected text.
     */
    private var mSelectedTextCenterX: Float = 0.toFloat()

    /**
     * The center Y position of the selected text.
     */
    private var mSelectedTextCenterY: Float = 0.toFloat()

    /**
     * The min height of this widget.
     */
    private var mMinHeight: Int = 0

    /**
     * The max height of this widget.
     */
    private var mMaxHeight: Int = 0

    /**
     * The max width of this widget.
     */
    private var mMinWidth: Int = 0

    /**
     * The max width of this widget.
     */
    private var mMaxWidth: Int = 0

    /**
     * Flag whether to compute the max width.
     */
    private val mComputeMaxWidth: Boolean

    /**
     * The align of the selected text.
     */
    private var selectedTextAlign = DEFAULT_TEXT_ALIGN

    /**
     * The color of the selected text.
     */
    private var mSelectedTextColor = DEFAULT_TEXT_COLOR

    /**
     * The size of the selected text.
     */
    private var mSelectedTextSize = DEFAULT_TEXT_SIZE

    /**
     * Flag whether the selected text should strikethroughed.
     */
    private var selectedTextStrikeThru: Boolean = false

    /**
     * Flag whether the selected text should underlined.
     */
    private var selectedTextUnderline: Boolean = false

    /**
     * The align of the text.
     */
    private var textAlign = DEFAULT_TEXT_ALIGN

    /**
     * The color of the text.
     */
    private var mTextColor = DEFAULT_TEXT_COLOR

    /**
     * The size of the text.
     */
    private var mTextSize = DEFAULT_TEXT_SIZE

    /**
     * Flag whether the text should strikethroughed.
     */
    private var textStrikeThru: Boolean = false

    /**
     * Flag whether the text should underlined.
     */
    private var textUnderline: Boolean = false

    /**
     * The typeface of the text.
     */
    private var mTypeface: Typeface? = null

    /**
     * The width of the gap between text elements if the selector wheel.
     */
    private var mSelectorTextGapWidth: Int = 0

    /**
     * The height of the gap between text elements if the selector wheel.
     */
    private var mSelectorTextGapHeight: Int = 0

    /**
     * The values to be displayed instead the indices.
     */
    /**
     * Gets the values to be displayed instead of string values.
     *
     * @return The displayed values.
     */
    /**
     * Sets the values to be displayed.
     *
     * @param displayedValues The displayed values.
     *
     * **Note:** The length of the displayed values array
     * must be equal to the range of selectable numbers which is equal to
     * [.getMaxValue] - [.getMinValue] + 1.
     */
    // Allow text entry rather than strictly numeric entry.
    var displayedValues: Array<String>? = null
        set(displayedValues) {
            if (this.displayedValues === displayedValues) {
                return
            }
            field = displayedValues
            if (this.displayedValues != null) {
                mSelectedText.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
            } else {
                mSelectedText.setRawInputType(InputType.TYPE_CLASS_NUMBER)
            }
            updateInputTextView()
            initializeSelectorWheelIndices()
            tryComputeMaxWidth()
        }

    /**
     * Lower value of the range of numbers allowed for the NumberPicker
     */
    private var mMinValue = DEFAULT_MIN_VALUE

    /**
     * Upper value of the range of numbers allowed for the NumberPicker
     */
    private var mMaxValue = DEFAULT_MAX_VALUE

    /**
     * Current value of this NumberPicker
     */
    private var mValue: Int = 0

    /**
     * Listener to be notified upon current value click.
     */
    private var mOnClickListener: OnClickListener? = null

    /**
     * Listener to be notified upon current value change.
     */
    private var mOnValueChangeListener: OnValueChangeListener? = null

    /**
     * Listener to be notified upon scroll state change.
     */
    private var mOnScrollListener: OnScrollListener? = null

    /**
     * Formatter for for displaying the current value.
     */
    private var mFormatter: Formatter? = null

    /**
     * The speed for updating the value form long press.
     */
    private var mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL

    /**
     * Cache for the string representation of selector indices.
     */
    private val mSelectorIndexToStringCache = SparseArray<String>()

    /**
     * The number of items show in the selector wheel.
     */
    private var mWheelItemCount = DEFAULT_WHEEL_ITEM_COUNT

    /**
     * The real number of items show in the selector wheel.
     */
    private var mRealWheelItemCount = DEFAULT_WHEEL_ITEM_COUNT

    /**
     * The index of the middle selector item.
     */
    private var mWheelMiddleItemIndex = mWheelItemCount / 2

    /**
     * The selector indices whose value are show by the selector.
     */
    private var selectorIndices = IntArray(mWheelItemCount)

    /**
     * The [Paint] for drawing the selector.
     */
    private val mSelectorWheelPaint: Paint

    /**
     * The size of a selector element (text + gap).
     */
    private var mSelectorElementSize: Int = 0

    /**
     * The initial offset of the scroll selector.
     */
    private var mInitialScrollOffset = Integer.MIN_VALUE

    /**
     * The current offset of the scroll selector.
     */
    private var mCurrentScrollOffset: Int = 0

    /**
     * The [Scroller] responsible for flinging the selector.
     */
    private val mFlingScroller: Scroller

    /**
     * The [Scroller] responsible for adjusting the selector.
     */
    private val mAdjustScroller: Scroller

    /**
     * The [Locale] responsible for setting the locale.
     */
    private var mLocale: Locale? = null

    /**
     * The previous X coordinate while scrolling the selector.
     */
    private var mPreviousScrollerX: Int = 0

    /**
     * The previous Y coordinate while scrolling the selector.
     */
    private var mPreviousScrollerY: Int = 0

    /**
     * Handle to the reusable command for setting the input text selection.
     */
    private var mSetSelectionCommand: SetSelectionCommand? = null

    /**
     * Handle to the reusable command for changing the current value from long press by one.
     */
    private var mChangeCurrentByOneFromLongPressCommand: ChangeCurrentByOneFromLongPressCommand? = null

    /**
     * The X position of the last down event.
     */
    private var mLastDownEventX: Float = 0.toFloat()

    /**
     * The Y position of the last down event.
     */
    private var mLastDownEventY: Float = 0.toFloat()

    /**
     * The X position of the last down or move event.
     */
    private var mLastDownOrMoveEventX: Float = 0.toFloat()

    /**
     * The Y position of the last down or move event.
     */
    private var mLastDownOrMoveEventY: Float = 0.toFloat()

    /**
     * Determines speed during touch scrolling.
     */
    private var mVelocityTracker: VelocityTracker? = null

    /**
     * @see ViewConfiguration.getScaledTouchSlop
     */
    private val mTouchSlop: Int

    /**
     * @see ViewConfiguration.getScaledMinimumFlingVelocity
     */
    private val mMinimumFlingVelocity: Int

    /**
     * @see ViewConfiguration.getScaledMaximumFlingVelocity
     */
    private var mMaximumFlingVelocity: Int = 0

    /**
     * Flag whether the selector should wrap around.
     */
    private var mWrapSelectorWheel: Boolean = false

    /**
     * User choice on whether the selector wheel should be wrapped.
     */
    private var mWrapSelectorWheelPreferred = true

    /**
     * Divider for showing item to be selected while scrolling
     */
    private var mDividerDrawable: Drawable? = null

    /**
     * The color of the divider.
     */
    private var mDividerColor = DEFAULT_DIVIDER_COLOR

    /**
     * The distance between the two dividers.
     */
    private var mDividerDistance: Int = 0

    /**
     * The thickness of the divider.
     */
    private var mDividerThickness: Int = 0

    /**
     * The top of the top divider.
     */
    private var mTopDividerTop: Int = 0

    /**
     * The bottom of the bottom divider.
     */
    private var mBottomDividerBottom: Int = 0

    /**
     * The left of the top divider.
     */
    private var mLeftDividerLeft: Int = 0

    /**
     * The right of the right divider.
     */
    private var mRightDividerRight: Int = 0

    /**
     * The current scroll state of the number picker.
     */
    private var mScrollState = OnScrollListener.SCROLL_STATE_IDLE

    /**
     * The keycode of the last handled DPAD down event.
     */
    private var mLastHandledDownDpadKeyCode = -1

    /**
     * Flag whether the selector wheel should hidden until the picker has focus.
     */
    private val mHideWheelUntilFocused: Boolean

    /**
     * The width of this widget.
     */
    private val mWidth: Float

    /**
     * The height of this widget.
     */
    private val mHeight: Float

    /**
     * The orientation of this widget.
     */
    private var mOrientation: Int = 0

    /**
     * The order of this widget.
     */
    /**
     * Should sort numbers in ascending or descending order.
     * @param order Pass [.ASCENDING] or [.ASCENDING].
     * Default value is [.DESCENDING].
     */
    private var order: Int = 0

    /**
     * Flag whether the fading edge should enabled.
     */
    private var isFadingEdgeEnabled = true

    /**
     * The strength of fading edge while drawing the selector.
     */
    private var fadingEdgeStrength = DEFAULT_FADING_EDGE_STRENGTH

    /**
     * Flag whether the scroller should enabled.
     */
    private var isScrollerEnabled = true

    /**
     * The line spacing multiplier of the text.
     */
    private var lineSpacingMultiplier = DEFAULT_LINE_SPACING_MULTIPLIER

    /**
     * The coefficient to adjust (divide) the max fling velocity.
     */
    private var mMaxFlingVelocityCoefficient = DEFAULT_MAX_FLING_VELOCITY_COEFFICIENT

    /**
     * The number formatter for current locale.
     */
    private var mNumberFormatter: NumberFormat? = null

    /**
     * The view configuration of this widget.
     */
    private val mViewConfiguration: ViewConfiguration

    private val maxTextSize: Float
        get() = max(mTextSize, mSelectedTextSize)

    /**
     * Gets whether the selector wheel wraps when reaching the min/max value.
     *
     * @return True if the selector wheel wraps.
     *
     * @see .getMinValue
     * @see .getMaxValue
     */
    /**
     * Sets whether the selector wheel shown during flinging/scrolling should
     * wrap around the [NumberPicker.getMinValue] and
     * [NumberPicker.getMaxValue] values.
     *
     *
     * By default if the range (max - min) is more than the number of items shown
     * on the selector wheel the selector wheel wrapping is enabled.
     *
     *
     *
     * **Note:** If the number of items, i.e. the range (
     * [.getMaxValue] - [.getMinValue]) is less than
     * the number of items shown on the selector wheel, the selector wheel will
     * not wrap. Hence, in such a case calling this method is a NOP.
     *
     *
     * @param wrapSelectorWheel Whether to wrap.
     */
    var wrapSelectorWheel
        get() = mWrapSelectorWheel
        set(wrapSelectorWheel) {
            mWrapSelectorWheelPreferred = wrapSelectorWheel
            updateWrapSelectorWheel()
        }

    private val isWrappingAllowed: Boolean
        get() = mMaxValue - mMinValue >= selectorIndices.size - 1

    /**
     * Returns the value of the picker.
     *
     * @return The value.
     */
    /**
     * Set the current value for the number picker.
     *
     *
     * If the argument is less than the [NumberPicker.getMinValue] and
     * [NumberPicker.getWrapSelectorWheel] is `false` the
     * current value is set to the [NumberPicker.getMinValue] value.
     *
     *
     *
     * If the argument is less than the [NumberPicker.getMinValue] and
     * [NumberPicker.getWrapSelectorWheel] is `true` the
     * current value is set to the [NumberPicker.getMaxValue] value.
     *
     *
     *
     * If the argument is less than the [NumberPicker.getMaxValue] and
     * [NumberPicker.getWrapSelectorWheel] is `false` the
     * current value is set to the [NumberPicker.getMaxValue] value.
     *
     *
     *
     * If the argument is less than the [NumberPicker.getMaxValue] and
     * [NumberPicker.getWrapSelectorWheel] is `true` the
     * current value is set to the [NumberPicker.getMinValue] value.
     *
     *
     * @param value The current value.
     * @see .setWrapSelectorWheel
     * @see .setMinValue
     * @see .setMaxValue
     */
    var value: Int
        get() = mValue
        set(value) = setValueInternal(value, false)

    /**
     * Returns the min value of the picker.
     *
     * @return The min value
     */
    /**
     * Sets the min value of the picker.
     *
     * @param minValue The min value inclusive.
     *
     * **Note:** The length of the displayed values array
     */
    //        if (minValue < 0) {
    //            throw new IllegalArgumentException("minValue must be >= 0");
    //        }
    var minValue: Int
        get() = mMinValue
        set(minValue) {
            mMinValue = minValue
            if (mMinValue > mValue) {
                mValue = mMinValue
            }
            wrapSelectorWheel = isWrappingAllowed
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }

    /**
     * Returns the max value of the picker.
     *
     * @return The max value.
     */
    /**
     * Sets the max value of the picker.
     *
     * @param maxValue The max value inclusive.
     *
     * **Note:** The length of the displayed values array
     */
    var maxValue: Int
        get() = mMaxValue
        set(maxValue) {
            if (maxValue < 0) {
                throw IllegalArgumentException("maxValue must be >= 0")
            }
            mMaxValue = maxValue
            if (mMaxValue < mValue) {
                mValue = mMaxValue
            }

            updateWrapSelectorWheel()
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }

    private val isHorizontalMode: Boolean
        get() = orientation == HORIZONTAL

    private val isAscendingOrder: Boolean
        get() = order == ASCENDING

    private var dividerColor: Int
        get() = mDividerColor
        set(@ColorInt color) {
            mDividerColor = color
            mDividerDrawable = ColorDrawable(color)
        }

    private var wheelItemCount: Int
        get() = mWheelItemCount
        set(count) {
            if (count < 1) {
                throw IllegalArgumentException("Wheel item count must be >= 1")
            }
            mRealWheelItemCount = count
            mWheelItemCount = if (count < DEFAULT_WHEEL_ITEM_COUNT) DEFAULT_WHEEL_ITEM_COUNT else count
            mWheelMiddleItemIndex = mWheelItemCount / 2
            selectorIndices = IntArray(mWheelItemCount)
        }

    /**
     * Set the formatter to be used for formatting the current value.
     *
     *
     * Note: If you have provided alternative values for the values this
     * formatter is never invoked.
     *
     *
     */
    private var formatter: Formatter?
        get() = mFormatter
        set(formatter) {
            if (formatter === mFormatter) {
                return
            }
            mFormatter = formatter
            initializeSelectorWheelIndices()
            updateInputTextView()
        }

    private var selectedTextColor: Int
        get() = mSelectedTextColor
        set(@ColorInt color) {
            mSelectedTextColor = color
            mSelectedText.setTextColor(mSelectedTextColor)
        }

    private var selectedTextSize: Float
        get() = mSelectedTextSize
        set(textSize) {
            mSelectedTextSize = textSize
            mSelectedText.textSize = pxToSp(mSelectedTextSize)
        }

    private var textColor: Int
        get() = mTextColor
        set(@ColorInt color) {
            mTextColor = color
            mSelectorWheelPaint.color = mTextColor
        }

    var textSize: Float
        get() = spToPx(mTextSize)
        set(textSize) {
            mTextSize = textSize
            mSelectorWheelPaint.textSize = mTextSize
        }

    private var typeface: Typeface?
        get() = mTypeface
        set(typeface) {
            mTypeface = typeface
            if (mTypeface != null) {
                mSelectedText.typeface = mTypeface
                mSelectorWheelPaint.typeface = mTypeface
            } else {
                mSelectedText.typeface = Typeface.MONOSPACE
                mSelectorWheelPaint.typeface = Typeface.MONOSPACE
            }
        }

    // @Retention(SOURCE)
    // @IntDef(VERTICAL, HORIZONTAL)
    annotation class Orientation

    /**
     * Use a custom NumberPicker formatting callback to use two-digit minutes
     * strings like "01". Keeping a static formatter etc. is the most efficient
     * way to do this; it avoids creating temporary objects on every call to
     * format().
     */
    /*
    private class TwoDigitFormatter internal constructor() : Formatter {
        internal val mBuilder = StringBuilder()

        internal var mZeroDigit: Char = ' '
        internal var mFmt: java.util.Formatter? = null

        internal val mArgs = arrayOfNulls<Any>(1)

        internal var mLocale: Locale? = null

        init {
            mLocale = Locale.getDefault()
            init(mLocale)
        }

        private fun init(locale: Locale?) {
            mFmt = createFormatter(locale)
            mZeroDigit = getZeroDigit(locale)
        }

        override fun format(value: Int): String {
            var currentLocale = Locale.getDefault()
            // to force the locale value set by using setter method
            if (mLocale != currentLocale) {
                currentLocale = mLocale
            }
            if (mZeroDigit != getZeroDigit(currentLocale)) {
                init(currentLocale)
            }
            mArgs[0] = value
            mBuilder.delete(0, mBuilder.length)
            mFmt!!.format("%02d", *mArgs)
            return mFmt!!.toString()
        }

        private fun getZeroDigit(locale: Locale?): Char {
            // return LocaleData.get(locale).zeroDigit;
            return DecimalFormatSymbols(locale).zeroDigit
        }

        /*
        // to force the locale value set by using setter method
        internal fun setLocale(locale: Locale?) {
            if (this.mLocale != null && mLocale == locale) {
                return
            }
            this.mLocale = locale
            init(mLocale)
        }
        */

        private fun createFormatter(locale: Locale?): java.util.Formatter {
            return Formatter(mBuilder, locale)
        }
    }
    */

    /**
     * Interface to listen for changes of the current value.
     */
    interface OnValueChangeListener {

        /**
         * Called upon a change of the current value.
         *
         * @param picker The NumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int)
    }

    /**
     * Interface to listen for the picker scroll state.
     */
    interface OnScrollListener {

        // @IntDef(SCROLL_STATE_IDLE, SCROLL_STATE_TOUCH_SCROLL, SCROLL_STATE_FLING)
        // @Retention(SOURCE)
        annotation class ScrollState

        /**
         * Callback invoked while the number picker scroll state has changed.
         *
         * @param view        The view whose scroll state is being reported.
         * @param scrollState The current scroll state. One of
         * [.SCROLL_STATE_IDLE],
         * [.SCROLL_STATE_TOUCH_SCROLL] or
         * [.SCROLL_STATE_IDLE].
         */
        fun onScrollStateChange(view: NumberPicker, @ScrollState scrollState: Int)

        companion object {

            /**
             * The view is not scrolling.
             */
            const val SCROLL_STATE_IDLE = 0

            /**
             * The user is scrolling using touch, and his finger is still on the screen.
             */
            const val SCROLL_STATE_TOUCH_SCROLL = 1

            /**
             * The user had previously been scrolling using touch and performed a fling.
             */
            const val SCROLL_STATE_FLING = 2
        }
    }

    /**
     * Interface used to format current value into a string for presentation.
     */
    interface Formatter {

        /**
         * Formats a string representation of the current value.
         *
         * @param value The currently selected value.
         * @return A formatted string representation.
         */
        fun format(value: Int): String
    }

    init {
        mNumberFormatter = NumberFormat.getInstance()
        mLocale = Locale.getDefault()

        val attributes = mContext.obtainStyledAttributes(attrs,
                R.styleable.NumberPicker, defStyle, 0)

        val selectionDivider = attributes.getDrawable(
                R.styleable.NumberPicker_np_divider)
        if (selectionDivider != null) {
            selectionDivider.callback = this
            if (selectionDivider.isStateful) {
                selectionDivider.state = drawableState
            }
            mDividerDrawable = selectionDivider
        } else {
            mDividerColor = attributes.getColor(R.styleable.NumberPicker_np_dividerColor,
                    mDividerColor)
            dividerColor = mDividerColor
        }

        val displayMetrics = resources.displayMetrics
        val defDividerDistance = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_DEFAULT_DIVIDER_DISTANCE.toFloat(), displayMetrics).toInt()
        val defDividerThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_DEFAULT_DIVIDER_THICKNESS.toFloat(), displayMetrics).toInt()
        mDividerDistance = attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_dividerDistance, defDividerDistance)
        mDividerThickness = attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_dividerThickness, defDividerThickness)

        order = attributes.getInt(R.styleable.NumberPicker_np_order, ASCENDING)
        mOrientation = attributes.getInt(R.styleable.NumberPicker_np_orientation, VERTICAL)

        mWidth = attributes.getDimensionPixelSize(R.styleable.NumberPicker_np_width,
                SIZE_UNSPECIFIED).toFloat()
        mHeight = attributes.getDimensionPixelSize(R.styleable.NumberPicker_np_height,
                SIZE_UNSPECIFIED).toFloat()

        setWidthAndHeight()

        mComputeMaxWidth = true

        mValue = attributes.getInt(R.styleable.NumberPicker_np_value, mValue)
        mMaxValue = attributes.getInt(R.styleable.NumberPicker_np_max, mMaxValue)
        mMinValue = attributes.getInt(R.styleable.NumberPicker_np_min, mMinValue)

        selectedTextAlign = attributes.getInt(R.styleable.NumberPicker_np_selectedTextAlign,
                selectedTextAlign)
        mSelectedTextColor = attributes.getColor(R.styleable.NumberPicker_np_selectedTextColor,
                mSelectedTextColor)
        mSelectedTextSize = attributes.getDimension(R.styleable.NumberPicker_np_selectedTextSize,
                spToPx(mSelectedTextSize))
        selectedTextStrikeThru = attributes.getBoolean(
                R.styleable.NumberPicker_np_selectedTextStrikeThru, selectedTextStrikeThru)
        selectedTextUnderline = attributes.getBoolean(
                R.styleable.NumberPicker_np_selectedTextUnderline, selectedTextUnderline)
        textAlign = attributes.getInt(R.styleable.NumberPicker_np_textAlign, textAlign)
        mTextColor = attributes.getColor(R.styleable.NumberPicker_np_textColor, mTextColor)
        mTextSize = attributes.getDimension(R.styleable.NumberPicker_np_textSize,
                spToPx(mTextSize))
        textStrikeThru = attributes.getBoolean(
                R.styleable.NumberPicker_np_textStrikeThru, textStrikeThru)
        textUnderline = attributes.getBoolean(
                R.styleable.NumberPicker_np_textUnderline, textUnderline)
        mTypeface = Typeface.create(attributes.getString(R.styleable.NumberPicker_np_typeface),
                Typeface.NORMAL)
        mFormatter = stringToFormatter(attributes.getString(R.styleable.NumberPicker_np_formatter))
        isFadingEdgeEnabled = attributes.getBoolean(R.styleable.NumberPicker_np_fadingEdgeEnabled,
                isFadingEdgeEnabled)
        fadingEdgeStrength = attributes.getFloat(R.styleable.NumberPicker_np_fadingEdgeStrength,
                fadingEdgeStrength)
        isScrollerEnabled = attributes.getBoolean(R.styleable.NumberPicker_np_scrollerEnabled,
                isScrollerEnabled)
        mWheelItemCount = attributes.getInt(R.styleable.NumberPicker_np_wheelItemCount,
                mWheelItemCount)
        lineSpacingMultiplier = attributes.getFloat(
                R.styleable.NumberPicker_np_lineSpacingMultiplier, lineSpacingMultiplier)
        mMaxFlingVelocityCoefficient = attributes.getInt(
                R.styleable.NumberPicker_np_maxFlingVelocityCoefficient,
                mMaxFlingVelocityCoefficient)
        mHideWheelUntilFocused = attributes.getBoolean(
                R.styleable.NumberPicker_np_hideWheelUntilFocused, false)

        // By default Linearlayout that we extend is not drawn. This is
        // its draw() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // draw() method to be called. Therefore, we declare we will draw.
        setWillNotDraw(false)

        val inflater = mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.number_picker_material, this, true)

        // input text
        mSelectedText = findViewById(R.id.np__numberpicker_input)
        mSelectedText.isEnabled = false
        mSelectedText.isFocusable = false
        mSelectedText.imeOptions = EditorInfo.IME_ACTION_NONE

        // create the selector wheel paint
        val paint = Paint()
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        mSelectorWheelPaint = paint

        selectedTextColor = mSelectedTextColor
        textColor = mTextColor
        textSize = mTextSize
        selectedTextSize = mSelectedTextSize
        typeface = mTypeface
        formatter = mFormatter
        updateInputTextView()

        value = mValue
        maxValue = mMaxValue
        minValue = mMinValue

        wheelItemCount = mWheelItemCount

        mWrapSelectorWheel = attributes.getBoolean(R.styleable.NumberPicker_np_wrapSelectorWheel,
                mWrapSelectorWheel)
        wrapSelectorWheel = mWrapSelectorWheel

        if (mWidth != SIZE_UNSPECIFIED.toFloat() && mHeight != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = mWidth / mMinWidth
            scaleY = mHeight / mMaxHeight
        } else if (mWidth != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = mWidth / mMinWidth
            scaleY = mWidth / mMinWidth
        } else if (mHeight != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = mHeight / mMaxHeight
            scaleY = mHeight / mMaxHeight
        }

        // initialize constants
        mViewConfiguration = ViewConfiguration.get(mContext)
        mTouchSlop = mViewConfiguration.scaledTouchSlop
        mMinimumFlingVelocity = mViewConfiguration.scaledMinimumFlingVelocity
        mMaximumFlingVelocity = mViewConfiguration.scaledMaximumFlingVelocity / mMaxFlingVelocityCoefficient

        // create the fling and adjust scrollers
        mFlingScroller = Scroller(mContext, null, true)
        mAdjustScroller = Scroller(mContext, DecelerateInterpolator(2.5f))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // If not explicitly specified this view is important for accessibility.
            if (importantForAccessibility == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Should be focusable by default, as the text view whose visibility changes is focusable
            if (focusable == View.FOCUSABLE_AUTO) {
                focusable = View.FOCUSABLE
                isFocusableInTouchMode = true
            }
        }

        attributes.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val msrdWdth = measuredWidth
        val msrdHght = measuredHeight

        // Input text centered horizontally.
        val inptTxtMsrdWdth = mSelectedText.measuredWidth
        val inptTxtMsrdHght = mSelectedText.measuredHeight
        val inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2
        val inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2
        val inptTxtRight = inptTxtLeft + inptTxtMsrdWdth
        val inptTxtBottom = inptTxtTop + inptTxtMsrdHght
        mSelectedText.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom)
        mSelectedTextCenterX = mSelectedText.x + mSelectedText.measuredWidth / 2
        mSelectedTextCenterY = mSelectedText.y + mSelectedText.measuredHeight / 2

        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel()
            initializeFadingEdges()

            val dividerDistance = 2 * mDividerThickness + mDividerDistance
            if (isHorizontalMode) {
                mLeftDividerLeft = (width - mDividerDistance) / 2 - mDividerThickness
                mRightDividerRight = mLeftDividerLeft + dividerDistance
            } else {
                mTopDividerTop = (height - mDividerDistance) / 2 - mDividerThickness
                mBottomDividerBottom = mTopDividerTop + dividerDistance
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Try greedily to fit the max width and height.
        val newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth)
        val newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
        // Flag if we are measured with width or height less than the respective min.
        val widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth, measuredWidth,
                widthMeasureSpec)
        val heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight, measuredHeight,
                heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }

    /**
     * Move to the final position of a scroller. Ensures to force finish the scroller
     * and if it is not at its final position a scroll of the selector wheel is
     * performed to fast forward to the final position.
     *
     * @param scroller The scroller to whose final position to get.
     * @return True of the a move was performed, i.e. the scroller was not in final position.
     */
    private fun moveToFinalScrollerPosition(scroller: Scroller): Boolean {
        scroller.forceFinished(true)
        if (isHorizontalMode) {
            var amountToScroll = scroller.finalX - scroller.currX
            val futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize
            var overshootAdjustment = mInitialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize
                    } else {
                        overshootAdjustment += mSelectorElementSize
                    }
                }
                amountToScroll += overshootAdjustment
                scrollBy(amountToScroll, 0)
                return true
            }
        } else {
            var amountToScroll = scroller.finalY - scroller.currY
            val futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize
            var overshootAdjustment = mInitialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize
                    } else {
                        overshootAdjustment += mSelectorElementSize
                    }
                }
                amountToScroll += overshootAdjustment
                scrollBy(0, amountToScroll)
                return true
            }
        }
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                removeAllCallbacks()
                // Make sure we support flinging inside scrollables.
                parent.requestDisallowInterceptTouchEvent(true)

                if (isHorizontalMode) {
                    mLastDownEventX = event.x
                    mLastDownOrMoveEventX = mLastDownEventX
                    if (!mFlingScroller.isFinished) {
                        mFlingScroller.forceFinished(true)
                        mAdjustScroller.forceFinished(true)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    } else if (!mAdjustScroller.isFinished) {
                        mFlingScroller.forceFinished(true)
                        mAdjustScroller.forceFinished(true)
                    } else if (mLastDownEventX >= mLeftDividerLeft && mLastDownEventX <= mRightDividerRight) {
                        if (mOnClickListener != null) {
                            mOnClickListener!!.onClick(this)
                        }
                    } else if (mLastDownEventX < mLeftDividerLeft) {
                        postChangeCurrentByOneFromLongPress(false)
                    } else if (mLastDownEventX > mRightDividerRight) {
                        postChangeCurrentByOneFromLongPress(true)
                    }
                } else {
                    mLastDownEventY = event.y
                    mLastDownOrMoveEventY = mLastDownEventY
                    if (!mFlingScroller.isFinished) {
                        mFlingScroller.forceFinished(true)
                        mAdjustScroller.forceFinished(true)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    } else if (!mAdjustScroller.isFinished) {
                        mFlingScroller.forceFinished(true)
                        mAdjustScroller.forceFinished(true)
                    } else if (mLastDownEventY >= mTopDividerTop && mLastDownEventY <= mBottomDividerBottom) {
                        if (mOnClickListener != null) {
                            mOnClickListener!!.onClick(this)
                        }
                    } else if (mLastDownEventY < mTopDividerTop) {
                        postChangeCurrentByOneFromLongPress(false)
                    } else if (mLastDownEventY > mBottomDividerBottom) {
                        postChangeCurrentByOneFromLongPress(true)
                    }
                }
                return true
            }
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        if (!isScrollerEnabled) {
            return false
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                if (isHorizontalMode) {
                    val currentMoveX = event.x
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownX = abs(currentMoveX - mLastDownEventX).toInt()
                        if (deltaDownX > mTouchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveX = (currentMoveX - mLastDownOrMoveEventX).toInt()
                        scrollBy(deltaMoveX, 0)
                        invalidate()
                    }
                    mLastDownOrMoveEventX = currentMoveX
                } else {
                    val currentMoveY = event.y
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownY = abs(currentMoveY - mLastDownEventY).toInt()
                        if (deltaDownY > mTouchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveY = (currentMoveY - mLastDownOrMoveEventY).toInt()
                        scrollBy(0, deltaMoveY)
                        invalidate()
                    }
                    mLastDownOrMoveEventY = currentMoveY
                }
            }
            MotionEvent.ACTION_UP -> {
                removeChangeCurrentByOneFromLongPress()
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumFlingVelocity.toFloat())
                if (isHorizontalMode) {
                    val initialVelocity = velocityTracker.xVelocity.toInt()
                    if (abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventX = event.x.toInt()
                        val deltaMoveX = abs(eventX - mLastDownEventX).toInt()
                        if (deltaMoveX <= mTouchSlop) {
                            val selectorIndexOffset = eventX / mSelectorElementSize - mWheelMiddleItemIndex
                            when {
                                selectorIndexOffset > 0 -> changeValueByOne(true)
                                selectorIndexOffset < 0 -> changeValueByOne(false)
                                else -> ensureScrollWheelAdjusted()
                            }
                        } else {
                            ensureScrollWheelAdjusted()
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    }
                } else {
                    val initialVelocity = velocityTracker.yVelocity.toInt()
                    if (abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventY = event.y.toInt()
                        val deltaMoveY = abs(eventY - mLastDownEventY).toInt()
                        if (deltaMoveY <= mTouchSlop) {
                            val selectorIndexOffset = eventY / mSelectorElementSize - mWheelMiddleItemIndex
                            when {
                                selectorIndexOffset > 0 -> changeValueByOne(true)
                                selectorIndexOffset < 0 -> changeValueByOne(false)
                                else -> ensureScrollWheelAdjusted()
                            }
                        } else {
                            ensureScrollWheelAdjusted()
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    }
                }
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
            }
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        when (val keyCode = event.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> removeAllCallbacks()
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_UP -> when (event.action) {
                KeyEvent.ACTION_DOWN -> if (mWrapSelectorWheel || if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                            value < maxValue
                        else
                            value > minValue) {
                    requestFocus()
                    mLastHandledDownDpadKeyCode = keyCode
                    removeAllCallbacks()
                    if (mFlingScroller.isFinished) {
                        changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                    }
                    return true
                }
                KeyEvent.ACTION_UP -> if (mLastHandledDownDpadKeyCode == keyCode) {
                    mLastHandledDownDpadKeyCode = -1
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTrackballEvent(event)
    }

    override fun computeScroll() {
        if (!isScrollerEnabled) {
            return
        }

        var scroller = mFlingScroller
        if (scroller.isFinished) {
            scroller = mAdjustScroller
            if (scroller.isFinished) {
                return
            }
        }
        scroller.computeScrollOffset()
        if (isHorizontalMode) {
            val currentScrollerX = scroller.currX
            if (mPreviousScrollerX == 0) {
                mPreviousScrollerX = scroller.startX
            }
            scrollBy(currentScrollerX - mPreviousScrollerX, 0)
            mPreviousScrollerX = currentScrollerX
        } else {
            val currentScrollerY = scroller.currY
            if (mPreviousScrollerY == 0) {
                mPreviousScrollerY = scroller.startY
            }
            scrollBy(0, currentScrollerY - mPreviousScrollerY)
            mPreviousScrollerY = currentScrollerY
        }
        if (scroller.isFinished) {
            onScrollerFinished(scroller)
        } else {
            postInvalidate()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mSelectedText.isEnabled = enabled
    }

    override fun scrollBy(x: Int, y: Int) {
        if (!isScrollerEnabled) {
            return
        }
        val selectorIndices = selectorIndices
        val startScrollOffset = mCurrentScrollOffset
        val gap: Int
        if (isHorizontalMode) {
            if (isAscendingOrder) {
                if (!mWrapSelectorWheel && x > 0
                        && selectorIndices[mWheelMiddleItemIndex] <= mMinValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
                if (!mWrapSelectorWheel && x < 0
                        && selectorIndices[mWheelMiddleItemIndex] >= mMaxValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
            } else {
                if (!mWrapSelectorWheel && x > 0
                        && selectorIndices[mWheelMiddleItemIndex] >= mMaxValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
                if (!mWrapSelectorWheel && x < 0
                        && selectorIndices[mWheelMiddleItemIndex] <= mMinValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
            }

            mCurrentScrollOffset += x
            gap = mSelectorTextGapWidth
        } else {
            if (isAscendingOrder) {
                if (!mWrapSelectorWheel && y > 0
                        && selectorIndices[mWheelMiddleItemIndex] <= mMinValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
                if (!mWrapSelectorWheel && y < 0
                        && selectorIndices[mWheelMiddleItemIndex] >= mMaxValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
            } else {
                if (!mWrapSelectorWheel && y > 0
                        && selectorIndices[mWheelMiddleItemIndex] >= mMaxValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
                if (!mWrapSelectorWheel && y < 0
                        && selectorIndices[mWheelMiddleItemIndex] <= mMinValue) {
                    mCurrentScrollOffset = mInitialScrollOffset
                    return
                }
            }

            mCurrentScrollOffset += y
            gap = mSelectorTextGapHeight
        }

        while (mCurrentScrollOffset - mInitialScrollOffset > gap) {
            mCurrentScrollOffset -= mSelectorElementSize
            if (isAscendingOrder) {
                decrementSelectorIndices(selectorIndices)
            } else {
                incrementSelectorIndices(selectorIndices)
            }
            setValueInternal(selectorIndices[mWheelMiddleItemIndex], true)
            if (!mWrapSelectorWheel && selectorIndices[mWheelMiddleItemIndex] < mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -gap) {
            mCurrentScrollOffset += mSelectorElementSize
            if (isAscendingOrder) {
                incrementSelectorIndices(selectorIndices)
            } else {
                decrementSelectorIndices(selectorIndices)
            }
            setValueInternal(selectorIndices[mWheelMiddleItemIndex], true)
            if (!mWrapSelectorWheel && selectorIndices[mWheelMiddleItemIndex] > mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }

        if (startScrollOffset != mCurrentScrollOffset) {
            if (isHorizontalMode) {
                onScrollChanged(mCurrentScrollOffset, 0, startScrollOffset, 0)
            } else {
                onScrollChanged(0, mCurrentScrollOffset, 0, startScrollOffset)
            }
        }
    }

    private fun computeScrollOffset(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) mCurrentScrollOffset else 0
    }

    private fun computeScrollRange(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) (mMaxValue - mMinValue + 1) * mSelectorElementSize else 0
    }

    private fun computeScrollExtent(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) width else height
    }

    override fun computeHorizontalScrollOffset(): Int {
        return computeScrollOffset(isHorizontalMode)
    }

    override fun computeHorizontalScrollRange(): Int {
        return computeScrollRange(isHorizontalMode)
    }

    override fun computeHorizontalScrollExtent(): Int {
        return computeScrollExtent(isHorizontalMode)
    }

    override fun computeVerticalScrollOffset(): Int {
        return computeScrollOffset(!isHorizontalMode)
    }

    override fun computeVerticalScrollRange(): Int {
        return computeScrollRange(!isHorizontalMode)
    }

    override fun computeVerticalScrollExtent(): Int {
        return computeScrollExtent(isHorizontalMode)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mNumberFormatter = NumberFormat.getInstance()
    }

    /**
     * Set listener to be notified on click of the current value.
     *
     * @param onClickListener The listener.
     */
    override fun setOnClickListener(onClickListener: OnClickListener?) {
        mOnClickListener = onClickListener
    }

    /**
     * Sets the listener to be notified on change of the current value.
     *
     * @param onValueChangedListener The listener.
     */
    fun setOnValueChangedListener(onValueChangedListener: OnValueChangeListener) {
        mOnValueChangeListener = onValueChangedListener
    }

    /**
     * Set listener to be notified for scroll state changes.
     *
     * @param onScrollListener The listener.
     */
    fun setOnScrollListener(onScrollListener: OnScrollListener) {
        mOnScrollListener = onScrollListener
    }

    private fun getPaintCenterY(fontMetrics: Paint.FontMetrics?): Float {
        return if (fontMetrics == null) {
            0f
        } else abs(fontMetrics.top + fontMetrics.bottom) / 2
    }

    /**
     * Computes the max width if no such specified as an attribute.
     */
    private fun tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return
        }
        mSelectorWheelPaint.textSize = maxTextSize
        var maxTextWidth = 0
        if (displayedValues == null) {
            var maxDigitWidth = 0f
            for (i in 0..9) {
                val digitWidth = mSelectorWheelPaint.measureText(formatNumber(i))
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth
                }
            }
            var numberOfDigits = 0
            var current = mMaxValue
            while (current > 0) {
                numberOfDigits++
                current /= 10
            }
            maxTextWidth = (numberOfDigits * maxDigitWidth).toInt()
        } else {
            val valueCount = displayedValues!!.size
            for (i in 0 until valueCount) {
                val textWidth = mSelectorWheelPaint.measureText(displayedValues!![i])
                if (textWidth > maxTextWidth) {
                    maxTextWidth = textWidth.toInt()
                }
            }
        }
        maxTextWidth += mSelectedText.paddingLeft + mSelectedText.paddingRight
        if (mMaxWidth != maxTextWidth) {
            mMaxWidth = if (maxTextWidth > mMinWidth) {
                maxTextWidth
            } else {
                mMinWidth
            }
            invalidate()
        }
    }

    /**
     * Whether or not the selector wheel should be wrapped is determined by user choice and whether
     * the choice is allowed. The former comes from [.setWrapSelectorWheel], the
     * latter is calculated based on min & max value set vs selector's visual length. Therefore,
     * this method should be called any time any of the 3 values (i.e. user choice, min and max
     * value) gets updated.
     */
    private fun updateWrapSelectorWheel() {
        mWrapSelectorWheel = isWrappingAllowed && mWrapSelectorWheelPreferred
    }

    private fun getFadingEdgeStrength(isHorizontalMode: Boolean): Float {
        return if (isHorizontalMode && isFadingEdgeEnabled) fadingEdgeStrength else 0.0f
    }

    override fun getTopFadingEdgeStrength(): Float {
        return getFadingEdgeStrength(!isHorizontalMode)
    }

    override fun getBottomFadingEdgeStrength(): Float {
        return getFadingEdgeStrength(!isHorizontalMode)
    }

    override fun getLeftFadingEdgeStrength(): Float {
        return getFadingEdgeStrength(isHorizontalMode)
    }

    override fun getRightFadingEdgeStrength(): Float {
        return getFadingEdgeStrength(isHorizontalMode)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeAllCallbacks()
    }

    @CallSuper
    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val selectionDivider = mDividerDrawable
        if (selectionDivider != null && selectionDivider.isStateful
                && selectionDivider.setState(drawableState)) {
            invalidateDrawable(selectionDivider)
        }
    }

    @CallSuper
    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        if (mDividerDrawable != null) {
            mDividerDrawable!!.jumpToCurrentState()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // save canvas
        canvas.save()

        val showSelectorWheel = if (mHideWheelUntilFocused) hasFocus() else true
        var x: Float
        var y: Float
        if (isHorizontalMode) {
            x = mCurrentScrollOffset.toFloat()
            y = (mSelectedText.baseline + mSelectedText.top).toFloat()
            if (mRealWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(mLeftDividerLeft, 0, mRightDividerRight, bottom)
            }
        } else {
            x = ((right - left) / 2).toFloat()
            y = mCurrentScrollOffset.toFloat()
            if (mRealWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(0, mTopDividerTop, right, mBottomDividerBottom)
            }
        }

        // draw the selector wheel
        val selectorIndices = selectorIndices
        for (i in selectorIndices.indices) {
            if (i == mWheelMiddleItemIndex) {
                mSelectorWheelPaint.textAlign = Paint.Align.values()[selectedTextAlign]
                mSelectorWheelPaint.textSize = mSelectedTextSize
                mSelectorWheelPaint.color = mSelectedTextColor
                mSelectorWheelPaint.isStrikeThruText = selectedTextStrikeThru
                mSelectorWheelPaint.isUnderlineText = selectedTextUnderline
            } else {
                mSelectorWheelPaint.textAlign = Paint.Align.values()[textAlign]
                mSelectorWheelPaint.textSize = mTextSize
                mSelectorWheelPaint.color = mTextColor
                mSelectorWheelPaint.isStrikeThruText = textStrikeThru
                mSelectorWheelPaint.isUnderlineText = textUnderline
            }

            val selectorIndex = selectorIndices[if (isAscendingOrder)
                i
            else
                selectorIndices.size - i - 1]
            val scrollSelectorValue = mSelectorIndexToStringCache.get(selectorIndex)
            // Do not draw the middle item if input is visible since the input
            // is shown only if the wheel is static and it covers the middle
            // item. Otherwise, if the user starts editing the text via the
            // IME he may see a dimmed version of the old value intermixed
            // with the new one.
            if (showSelectorWheel && i != mWheelMiddleItemIndex || i == mWheelMiddleItemIndex && mSelectedText.visibility != View.VISIBLE) {
                var textY = y
                if (!isHorizontalMode) {
                    textY += getPaintCenterY(mSelectorWheelPaint.fontMetrics)
                }
                drawText(scrollSelectorValue, x, textY, mSelectorWheelPaint, canvas)
            }

            if (isHorizontalMode) {
                x += mSelectorElementSize.toFloat()
            } else {
                y += mSelectorElementSize.toFloat()
            }
        }

        // restore canvas
        canvas.restore()

        // draw the dividers
        if (showSelectorWheel && mDividerDrawable != null) {
            if (isHorizontalMode) {
                val bottom = bottom

                // draw the left divider
                val leftOfLeftDivider = mLeftDividerLeft
                val rightOfLeftDivider = leftOfLeftDivider + mDividerThickness
                mDividerDrawable!!.setBounds(leftOfLeftDivider, 0, rightOfLeftDivider, bottom)
                mDividerDrawable!!.draw(canvas)

                // draw the right divider
                val rightOfRightDivider = mRightDividerRight
                val leftOfRightDivider = rightOfRightDivider - mDividerThickness
                mDividerDrawable!!.setBounds(leftOfRightDivider, 0, rightOfRightDivider, bottom)
                mDividerDrawable!!.draw(canvas)
            } else {
                val right = right

                // draw the top divider
                val topOfTopDivider = mTopDividerTop
                val bottomOfTopDivider = topOfTopDivider + mDividerThickness
                mDividerDrawable!!.setBounds(0, topOfTopDivider, right, bottomOfTopDivider)
                mDividerDrawable!!.draw(canvas)

                // draw the bottom divider
                val bottomOfBottomDivider = mBottomDividerBottom
                val topOfBottomDivider = bottomOfBottomDivider - mDividerThickness
                mDividerDrawable!!.setBounds(0, topOfBottomDivider, right, bottomOfBottomDivider)
                mDividerDrawable!!.draw(canvas)
            }
        }
    }

    private fun drawText(text: String, x: Float, y: Float, paint: Paint, canvas: Canvas) {
        var y2 = y
        if (text.contains("\n")) {
            val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val height = abs(paint.descent() + paint.ascent()) * lineSpacingMultiplier
            val diff = (lines.size - 1) * height / 2
            y2 -= diff
            for (line in lines) {
                canvas.drawText(line, x, y2, paint)
                y2 += height
            }
        } else {
            canvas.drawText(text, x, y2, paint)
        }
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = NumberPicker::class.java.name
        event.isScrollable = isScrollerEnabled
        val scroll = (mMinValue + mValue) * mSelectorElementSize
        val maxScroll = (mMaxValue - mMinValue) * mSelectorElementSize
        if (isHorizontalMode) {
            event.scrollX = scroll
            event.maxScrollX = maxScroll
        } else {
            event.scrollY = scroll
            event.maxScrollY = maxScroll
        }
    }

    /**
     * Makes a measure spec that tries greedily to use the max value.
     *
     * @param measureSpec The measure spec.
     * @param maxSize The max value for the size.
     * @return A measure spec greedily imposing the max size.
     */
    private fun makeMeasureSpec(measureSpec: Int, maxSize: Int): Int {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec
        }
        val size = MeasureSpec.getSize(measureSpec)
        return when (val mode = MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> measureSpec
            MeasureSpec.AT_MOST -> MeasureSpec.makeMeasureSpec(min(size, maxSize), MeasureSpec.EXACTLY)
            MeasureSpec.UNSPECIFIED -> MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY)
            else -> throw IllegalArgumentException("Unknown measure mode: $mode")
        }
    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec. Tries to respect the min size, unless a different size
     * is imposed by the constraints.
     *
     * @param minSize The minimal desired size.
     * @param measuredSize The currently measured size.
     * @param measureSpec The current measure spec.
     * @return The resolved size and state.
     */
    private fun resolveSizeAndStateRespectingMinSize(minSize: Int, measuredSize: Int,
                                                     measureSpec: Int): Int {
        return if (minSize != SIZE_UNSPECIFIED) {
            val desiredWidth = max(minSize, measuredSize)
            resolveSizeAndState(desiredWidth, measureSpec, 0)
        } else {
            measuredSize
        }
    }

    /**
     * Resets the selector indices and clear the cached string representation of
     * these indices.
     */
    private fun initializeSelectorWheelIndices() {
        mSelectorIndexToStringCache.clear()
        val selectorIndices = selectorIndices
        val current = value
        for (i in this.selectorIndices.indices) {
            var selectorIndex = current + (i - mWheelMiddleItemIndex)
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            selectorIndices[i] = selectorIndex
            ensureCachedScrollSelectorValue(selectorIndices[i])
        }
    }

    /**
     * Sets the current value of this NumberPicker.
     *
     * @param current The new value of the NumberPicker.
     * @param notifyChange Whether to notify if the current value changed.
     */
    private fun setValueInternal(current: Int, notifyChange: Boolean) {
        var current2 = current
        if (mValue == current2) {
            return
        }
        // Wrap around the values if we go past the start or end
        if (mWrapSelectorWheel) {
            current2 = getWrappedSelectorIndex(current)
        } else {
            current2 = max(current2, mMinValue)
            current2 = min(current2, mMaxValue)
        }
        val previous = mValue
        mValue = current2
        // If we're flinging, we'll update the text view at the end when it becomes visible
        if (mScrollState != OnScrollListener.SCROLL_STATE_FLING) {
            updateInputTextView()
        }
        if (notifyChange) {
            notifyChange(previous, current2)
        }
        initializeSelectorWheelIndices()
        updateAccessibilityDescription()
        invalidate()
    }

    /**
     * Updates the accessibility values of the view,
     * to the currently selected value
     */
    private fun updateAccessibilityDescription() {
        this.contentDescription = value.toString()
    }

    /**
     * Changes the current value by one which is increment or
     * decrement based on the passes argument.
     * decrement the current value.
     *
     * @param increment True to increment, false to decrement.
     */
    private fun changeValueByOne(increment: Boolean) {
        if (!moveToFinalScrollerPosition(mFlingScroller)) {
            moveToFinalScrollerPosition(mAdjustScroller)
        }
        smoothScroll(increment, 1)
    }

    /**
     * Starts a smooth scroll
     *
     * @param increment True to increment, false to decrement.
     * @param steps The steps to scroll.
     */
    private fun smoothScroll(increment: Boolean, steps: Int) {
        if (isHorizontalMode) {
            mPreviousScrollerX = 0
            if (increment) {
                mFlingScroller.startScroll(0, 0, -mSelectorElementSize * steps, 0, SNAP_SCROLL_DURATION)
            } else {
                mFlingScroller.startScroll(0, 0, mSelectorElementSize * steps, 0, SNAP_SCROLL_DURATION)
            }
        } else {
            mPreviousScrollerY = 0
            if (increment) {
                mFlingScroller.startScroll(0, 0, 0, -mSelectorElementSize * steps, SNAP_SCROLL_DURATION)
            } else {
                mFlingScroller.startScroll(0, 0, 0, mSelectorElementSize * steps, SNAP_SCROLL_DURATION)
            }
        }
        invalidate()
    }

    private fun initializeSelectorWheel() {
        initializeSelectorWheelIndices()
        val selectorIndices = selectorIndices
        val totalTextSize = (selectorIndices.size - 1) * mTextSize.toInt() + mSelectedTextSize.toInt()
        val textGapCount = selectorIndices.size.toFloat()
        if (isHorizontalMode) {
            val totalTextGapWidth = (right - left - totalTextSize).toFloat()
            mSelectorTextGapWidth = (totalTextGapWidth / textGapCount).toInt()
            mSelectorElementSize = maxTextSize.toInt() + mSelectorTextGapWidth
            mInitialScrollOffset = mSelectedTextCenterX.toInt() - mSelectorElementSize * mWheelMiddleItemIndex
        } else {
            val totalTextGapHeight = (bottom - top - totalTextSize).toFloat()
            mSelectorTextGapHeight = (totalTextGapHeight / textGapCount).toInt()
            mSelectorElementSize = maxTextSize.toInt() + mSelectorTextGapHeight
            mInitialScrollOffset = mSelectedTextCenterY.toInt() - mSelectorElementSize * mWheelMiddleItemIndex
        }
        mCurrentScrollOffset = mInitialScrollOffset
        updateInputTextView()
    }

    private fun initializeFadingEdges() {
        if (isHorizontalMode) {
            isHorizontalFadingEdgeEnabled = true
            setFadingEdgeLength((right - left - mTextSize.toInt()) / 2)
        } else {
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength((bottom - top - mTextSize.toInt()) / 2)
        }
    }

    /**
     * Callback invoked upon completion of a given `scroller`.
     */
    private fun onScrollerFinished(scroller: Scroller) {
        if (scroller == mFlingScroller) {
            ensureScrollWheelAdjusted()
            updateInputTextView()
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
        } else if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            updateInputTextView()
        }
    }

    /**
     * Handles transition to a given `scrollState`
     */
    private fun onScrollStateChange(scrollState: Int) {
        if (mScrollState == scrollState) {
            return
        }
        mScrollState = scrollState
        if (mOnScrollListener != null) {
            mOnScrollListener!!.onScrollStateChange(this, scrollState)
        }
    }

    /**
     * Flings the selector with the given `velocity`.
     */
    private fun fling(velocity: Int) {
        if (isHorizontalMode) {
            mPreviousScrollerX = 0
            if (velocity > 0) {
                mFlingScroller.fling(0, 0, velocity, 0, 0, Integer.MAX_VALUE, 0, 0)
            } else {
                mFlingScroller.fling(Integer.MAX_VALUE, 0, velocity, 0, 0, Integer.MAX_VALUE, 0, 0)
            }
        } else {
            mPreviousScrollerY = 0
            if (velocity > 0) {
                mFlingScroller.fling(0, 0, 0, velocity, 0, 0, 0, Integer.MAX_VALUE)
            } else {
                mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocity, 0, 0, 0, Integer.MAX_VALUE)
            }
        }

        invalidate()
    }

    /**
     * @return The wrapped index `selectorIndex` value.
     */
    private fun getWrappedSelectorIndex(selectorIndex: Int): Int {
        if (selectorIndex > mMaxValue) {
            return mMinValue + (selectorIndex - mMaxValue) % (mMaxValue - mMinValue) - 1
        } else if (selectorIndex < mMinValue) {
            return mMaxValue - (mMinValue - selectorIndex) % (mMaxValue - mMinValue) + 1
        }
        return selectorIndex
    }

    /**
     * Increments the `selectorIndices` whose string representations
     * will be displayed in the selector.
     */
    private fun incrementSelectorIndices(selectorIndices: IntArray) {
        for (i in 0 until selectorIndices.size - 1) {
            selectorIndices[i] = selectorIndices[i + 1]
        }
        var nextScrollSelectorIndex = selectorIndices[selectorIndices.size - 2] + 1
        if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
            nextScrollSelectorIndex = mMinValue
        }
        selectorIndices[selectorIndices.size - 1] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    /**
     * Decrements the `selectorIndices` whose string representations
     * will be displayed in the selector.
     */
    private fun decrementSelectorIndices(selectorIndices: IntArray) {
        for (i in selectorIndices.size - 1 downTo 1) {
            selectorIndices[i] = selectorIndices[i - 1]
        }
        var nextScrollSelectorIndex = selectorIndices[1] - 1
        if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
            nextScrollSelectorIndex = mMaxValue
        }
        selectorIndices[0] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    /**
     * Ensures we have a cached string representation of the given `
     * selectorIndex` to avoid multiple instantiations of the same string.
     */
    private fun ensureCachedScrollSelectorValue(selectorIndex: Int) {
        val cache = mSelectorIndexToStringCache
        var scrollSelectorValue: String? = cache.get(selectorIndex)
        if (scrollSelectorValue != null) {
            return
        }
        scrollSelectorValue = if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
            ""
        } else {
            if (displayedValues != null) {
                val displayedValueIndex = selectorIndex - mMinValue
                displayedValues!![displayedValueIndex]
            } else {
                formatNumber(selectorIndex)
            }
        }
        cache.put(selectorIndex, scrollSelectorValue)
    }

    private fun formatNumber(value: Int): String {
        return if (mFormatter != null) mFormatter!!.format(value) else formatNumberWithLocale(value)
    }

    /**
     * Updates the view of this NumberPicker. If displayValues were specified in
     * the string corresponding to the index specified by the current value will
     * be returned. Otherwise, the formatter specified in [.setFormatter]
     * will be used to format the number.
     *
     * @return Whether the text was updated.
     */
    private fun updateInputTextView(): Boolean {
        /*
         * If we don't have displayed values then use the current number else
         * find the correct value in the displayed values for the current
         * number.
         */
        val text = if (displayedValues == null)
            formatNumber(mValue)
        else
            displayedValues!![mValue - mMinValue]
        if (!TextUtils.isEmpty(text)) {
            val beforeText = mSelectedText.text
            if (text != beforeText.toString()) {
                mSelectedText.setText(text)
                return true
            }
        }

        return false
    }

    /**
     * Notifies the listener, if registered, of a change of the value of this
     * NumberPicker.
     */
    private fun notifyChange(previous: Int, current: Int) {
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener!!.onValueChange(this, previous, mValue)
        }
    }

    /**
     * Posts a command for changing the current value by one.
     *
     * @param increment Whether to increment or decrement the value.
     */
    private fun postChangeCurrentByOneFromLongPress(increment: Boolean, delayMillis: Long = ViewConfiguration.getLongPressTimeout().toLong()) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = ChangeCurrentByOneFromLongPressCommand()
        } else {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
        mChangeCurrentByOneFromLongPressCommand!!.setStep(increment)
        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis)
    }

    /**
     * Removes the command for changing the current value by one.
     */
    private fun removeChangeCurrentByOneFromLongPress() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
    }

    /**
     * Removes all pending callback from the message queue.
     */
    private fun removeAllCallbacks() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
        if (mSetSelectionCommand != null) {
            mSetSelectionCommand!!.cancel()
        }
    }

    /**
     * @return The selected index given its displayed `value`.
     */
    /*
    private fun getSelectedPos(value: String): Int {
        var value = value
        if (displayedValues == null) {
            try {
                return Integer.parseInt(value)
            } catch (e: NumberFormatException) {
                // Ignore as if it's not a number we don't care
            }

        } else {
            for (i in displayedValues!!.indices) {
                // Don't force the user to type in jan when ja will do
                value = value.toLowerCase()
                if (displayedValues!![i].toLowerCase().startsWith(value)) {
                    return mMinValue + i
                }
            }

            /*
             * The user might have typed in a number into the month field i.e.
             * 10 instead of OCT so support that too.
             */
            try {
                return Integer.parseInt(value)
            } catch (e: NumberFormatException) {
                // Ignore as if it's not a number we don't care
            }

        }
        return mMinValue
    }
    */

    /**
     * Posts a [SetSelectionCommand] from the given
     * `selectionStart` to `selectionEnd`.
     */
    /*
    private fun postSetSelectionCommand(selectionStart: Int, selectionEnd: Int) {
        if (mSetSelectionCommand == null) {
            mSetSelectionCommand = SetSelectionCommand(mSelectedText)
        } else {
            mSetSelectionCommand!!.post(selectionStart, selectionEnd)
        }
    }
    */

    /**
     * Ensures that the scroll wheel is adjusted i.e. there is no offset and the
     * middle element is in the middle of the widget.
     *
     * @return Whether an adjustment has been made.
     */
    private fun ensureScrollWheelAdjusted(): Boolean {
        // adjust to the closest value
        var delta = mInitialScrollOffset - mCurrentScrollOffset
        if (delta != 0) {
            if (abs(delta) > mSelectorElementSize / 2) {
                delta += if (delta > 0) -mSelectorElementSize else mSelectorElementSize
            }
            if (isHorizontalMode) {
                mPreviousScrollerX = 0
                mAdjustScroller.startScroll(0, 0, delta, 0, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
            } else {
                mPreviousScrollerY = 0
                mAdjustScroller.startScroll(0, 0, 0, delta, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
            }
            invalidate()
            return true
        }
        return false
    }

    /**
     * Command for setting the input text selection.
     */
    private class SetSelectionCommand(private val mInputText: EditText) : Runnable {

        private var mSelectionStart: Int = 0
        private var mSelectionEnd: Int = 0

        /** Whether this runnable is currently posted.  */
        private var mPosted: Boolean = false

        /*
        fun post(selectionStart: Int, selectionEnd: Int) {
            mSelectionStart = selectionStart
            mSelectionEnd = selectionEnd
            if (!mPosted) {
                mInputText.post(this)
                mPosted = true
            }
        }
        */

        fun cancel() {
            if (mPosted) {
                mInputText.removeCallbacks(this)
                mPosted = false
            }
        }

        override fun run() {
            mPosted = false
            mInputText.setSelection(mSelectionStart, mSelectionEnd)
        }
    }

    /**
     * Command for changing the current value from a long press by one.
     */
    internal inner class ChangeCurrentByOneFromLongPressCommand : Runnable {
        private var mIncrement: Boolean = false

        fun setStep(increment: Boolean) {
            mIncrement = increment
        }

        override fun run() {
            changeValueByOne(mIncrement)
            postDelayed(this, mLongPressUpdateInterval)
        }
    }

    private fun formatNumberWithLocale(value: Int): String {
        return mNumberFormatter!!.format(value.toLong())
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    /*
    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }
    */

    private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                resources.displayMetrics)
    }

    private fun pxToSp(px: Float): Float {
        return px / resources.displayMetrics.scaledDensity
    }

    private fun stringToFormatter(formatter: String?): Formatter? {
        return if (TextUtils.isEmpty(formatter)) {
            null
        } else object : Formatter {
            override fun format(value: Int): String {
                return String.format(mLocale!!, formatter!!, value)
            }
        }

    }

    private fun setWidthAndHeight() {
        if (isHorizontalMode) {
            mMinHeight = SIZE_UNSPECIFIED
            mMaxHeight = dpToPx(DEFAULT_MIN_WIDTH.toFloat()).toInt()
            mMinWidth = dpToPx(DEFAULT_MAX_HEIGHT.toFloat()).toInt()
            mMaxWidth = SIZE_UNSPECIFIED
        } else {
            mMinHeight = SIZE_UNSPECIFIED
            mMaxHeight = dpToPx(DEFAULT_MAX_HEIGHT.toFloat()).toInt()
            mMinWidth = dpToPx(DEFAULT_MIN_WIDTH.toFloat()).toInt()
            mMaxWidth = SIZE_UNSPECIFIED
        }
    }

    /*
    private fun setDividerDistance(distance: Int) {
        mDividerDistance = distance
    }
    */

    /*
    private fun setDividerThickness(thickness: Int) {
        mDividerThickness = thickness
    }
    */

    override fun setOrientation(@Orientation orientation: Int) {
        mOrientation = orientation
        setWidthAndHeight()
    }

    /*
    private fun setFormatter(formatter1: String) {
        if (TextUtils.isEmpty(formatter1)) {
            return
        }

        formatter = stringToFormatter(formatter1)
    }
    */

    /*
    @JvmOverloads
    fun setTypeface(string: String, style: Int = Typeface.NORMAL) {
        if (TextUtils.isEmpty(string)) {
            return
        }
        typeface = Typeface.create(string, style)
    }
    */

    override fun getOrientation(): Int {
        return mOrientation
    }

    companion object {

        const val VERTICAL = LinearLayout.VERTICAL
        const val HORIZONTAL = LinearLayout.HORIZONTAL

        const val ASCENDING = 0

        private const val CENTER = 1

        /**
         * The default update interval during long press.
         */
        private const val DEFAULT_LONG_PRESS_UPDATE_INTERVAL: Long = 300

        /**
         * The default coefficient to adjust (divide) the max fling velocity.
         */
        private const val DEFAULT_MAX_FLING_VELOCITY_COEFFICIENT = 8

        /**
         * The the duration for adjusting the selector wheel.
         */
        private const val SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800

        /**
         * The duration of scrolling while snapping to a given position.
         */
        private const val SNAP_SCROLL_DURATION = 300

        /**
         * The default strength of fading edge while drawing the selector.
         */
        private const val DEFAULT_FADING_EDGE_STRENGTH = 0.9f

        /**
         * The default unscaled height of the divider.
         */
        private const val UNSCALED_DEFAULT_DIVIDER_THICKNESS = 2

        /**
         * The default unscaled distance between the dividers.
         */
        private const val UNSCALED_DEFAULT_DIVIDER_DISTANCE = 48

        /**
         * Constant for unspecified size.
         */
        private const val SIZE_UNSPECIFIED = -1

        /**
         * The default color of divider.
         */
        private const val DEFAULT_DIVIDER_COLOR = -0x1000000

        /**
         * The default max value of this widget.
         */
        private const val DEFAULT_MAX_VALUE = 100

        /**
         * The default min value of this widget.
         */
        private const val DEFAULT_MIN_VALUE = 1

        /**
         * The default wheel item count of this widget.
         */
        private const val DEFAULT_WHEEL_ITEM_COUNT = 3

        /**
         * The default max height of this widget.
         */
        private const val DEFAULT_MAX_HEIGHT = 180

        /**
         * The default min width of this widget.
         */
        private const val DEFAULT_MIN_WIDTH = 64

        /**
         * The default align of text.
         */
        private const val DEFAULT_TEXT_ALIGN = CENTER

        /**
         * The default color of text.
         */
        private const val DEFAULT_TEXT_COLOR = -0x1000000

        /**
         * The default size of text.
         */
        private const val DEFAULT_TEXT_SIZE = 25f

        /**
         * The default line spacing multiplier of text.
         */
        private const val DEFAULT_LINE_SPACING_MULTIPLIER = 1f

        /*
        private val sTwoDigitFormatter = TwoDigitFormatter()
        */

        /**
         * Utility to reconcile a desired size and state, with constraints imposed
         * by a MeasureSpec.  Will take the desired size, unless a different size
         * is imposed by the constraints.  The returned value is a compound integer,
         * with the resolved size in the [.MEASURED_SIZE_MASK] bits and
         * optionally the bit [.MEASURED_STATE_TOO_SMALL] set if the resulting
         * size is smaller than the size the view wants to be.
         *
         * @param size How big the view wants to be
         * @param measureSpec Constraints imposed by the parent
         * @return Size information bit mask as defined by
         * [.MEASURED_SIZE_MASK] and [.MEASURED_STATE_TOO_SMALL].
         */
        fun resolveSizeAndState(size: Int, measureSpec: Int, childMeasuredState: Int): Int {
            var result = size
            val specMode = MeasureSpec.getMode(measureSpec)
            val specSize = MeasureSpec.getSize(measureSpec)
            when (specMode) {
                MeasureSpec.UNSPECIFIED -> result = size
                MeasureSpec.AT_MOST -> result = if (specSize < size) {
                    specSize or View.MEASURED_STATE_TOO_SMALL
                } else {
                    size
                }
                MeasureSpec.EXACTLY -> result = specSize
            }
            return result or (childMeasuredState and View.MEASURED_STATE_MASK)
        }

        /**
         * The numbers accepted by the input text's [Filter]
         */
        /*
        private val DIGIT_CHARACTERS = charArrayOf(
                // Latin digits are the common case
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                // Arabic-Indic
                '\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668', '\u0669',
                // Extended Arabic-Indic
                '\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5', '\u06f6', '\u06f7', '\u06f8', '\u06f9',
                // Hindi and Marathi (Devanagari script)
                '\u0966', '\u0967', '\u0968', '\u0969', '\u096a', '\u096b', '\u096c', '\u096d', '\u096e', '\u096f',
                // Bengali
                '\u09e6', '\u09e7', '\u09e8', '\u09e9', '\u09ea', '\u09eb', '\u09ec', '\u09ed', '\u09ee', '\u09ef',
                // Kannada
                '\u0ce6', '\u0ce7', '\u0ce8', '\u0ce9', '\u0cea', '\u0ceb', '\u0cec', '\u0ced', '\u0cee', '\u0cef',
                // Negative
                '-')
        */
    }

}
/**
 * Create a new number picker.
 *
 * @param context The application environment.
 */
/**
 * Create a new number picker.
 *
 * @param context The application environment.
 * @param attrs A collection of attributes.
 */
/**
 * Posts a command for changing the current value by one.
 *
 * @param increment Whether to increment or decrement the value.
 */