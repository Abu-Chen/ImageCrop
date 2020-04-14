package com.abu.imagecrop.view.component

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.abu.imagecrop.extension.*
import com.asus.mbsw.vivowatch_2.libs.crop.BitmapGestureHandler
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class CropView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    private val TAG = "CropView"

    var onInitialized: (() -> Unit)? = null

    var observeCropRectOnOriginalBitmapChanged: ((RectF) -> Unit)? = null

    private val cropRectOnOriginalBitmapMatrix = Matrix()
    private val cropRect: RectF = RectF()

    private var bitmap: Bitmap? = null
    private val bitmapMatrix: Matrix = Matrix()

    private val bitmapMinRect = RectF()
    private val bitmapRect = RectF()

    private val viewRect = RectF()

    private var viewWidth = 0f
    private var viewHeight = 0f

    private val emptyPaint = Paint().apply { isAntiAlias = true }

    private val marginInPixelSize = 10

    /**
     * Hold value for scaling bitmap with two finger.
     * Initialize point to avoid memory allocation every time user scale bitmap with fingers.
     */
    private val zoomFocusPoint = FloatArray(2)

    /**
     * This value holds inverted matrix when user scale
     * bitmap image with two finger. This value initialized to
     * avoid memory allocation every time user pinch zoom.
     */
    private val zoomInverseMatrix = Matrix()
    private val gridLineWidthInPixel = 5f

    private val cropPaint = Paint().apply {
        color = Color.RED
        strokeWidth = gridLineWidthInPixel
        style = Paint.Style.STROKE
    }

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
    }

    private val gestureListener = object : BitmapGestureHandler.BitmapGestureListener {
        override fun onDoubleTap(motionEvent: MotionEvent) {

            if (isBitmapScaleExceedMaxLimit(DOUBLE_TAP_SCALE_FACTOR)) {

                val resetMatrix = Matrix()
                val scale = max(
                        cropRect.width() / bitmapRect.width(),
                        cropRect.height() / bitmapRect.height()
                )
                resetMatrix.setScale(scale, scale)

                val translateX = (viewWidth - bitmapRect.width() * scale) / 2f + marginInPixelSize
                val translateY = (viewHeight - bitmapRect.height() * scale) / 2f + marginInPixelSize
                resetMatrix.postTranslate(translateX, translateY)

                bitmapMatrix.animateToMatrix(resetMatrix) {
                    notifyCropRectChanged()
                    invalidate()
                }

                return
            }

            bitmapMatrix.animateScaleToPoint(
                DOUBLE_TAP_SCALE_FACTOR,
                    motionEvent.x,
                    motionEvent.y
            ) {
                notifyCropRectChanged()
                invalidate()
            }
        }

        override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float) {

            /**
             * Return if new calculated bitmap matrix will exceed scale
             * point then early return.
             * Otherwise continue and do calculation and apply to bitmap matrix.
             */
            if (isBitmapScaleExceedMaxLimit(scaleFactor)) {
                return
            }

            zoomInverseMatrix.reset()
            bitmapMatrix.invert(zoomInverseMatrix)

            /**
             * Inverse focus points
             */
            zoomFocusPoint[0] = focusX
            zoomFocusPoint[1] = focusY
            zoomInverseMatrix.mapPoints(zoomFocusPoint)

            /**
             * Scale bitmap matrix
             */
            bitmapMatrix.preScale(
                    scaleFactor,
                    scaleFactor,
                    zoomFocusPoint[0],
                    zoomFocusPoint[1]
            )
            notifyCropRectChanged()

            invalidate()
        }

        override fun onScroll(distanceX: Float, distanceY: Float) {
            bitmapMatrix.postTranslate(-distanceX, -distanceY)
            invalidate()
        }

        override fun onEnd() {
            settleDraggedBitmap()
        }
    }

    private val bitmapGestureHandler = BitmapGestureHandler(context, gestureListener)

    /**
     * Initialize necessary rects, bitmaps, canvas here.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged: w:$w, h:$h, oldw:$oldw, oldh:$oldh")
        initialize()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        bitmapGestureHandler.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(ContextCompat.getColor(context, android.R.color.black))
        bitmap?.let { bitmap ->
            canvas?.drawBitmap(bitmap, bitmapMatrix, emptyPaint)
        }
        Log.d(TAG, "onDraw bitmapMatrix x:${bitmapMatrix.getTranslateX()}, y:${bitmapMatrix.getTranslateY()}")
        canvas?.save()

        drawMask(canvas)

        canvas?.restore()

        drawAim(canvas)
        //canvas?.drawRect(cropRect, cropPaint)
    }

    /**
     * Set bitmap from outside of this view.
     * Calculates bitmap rect and bitmap min rect.
     */
    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap

        Log.d(TAG, "setBitmap: width:${bitmap?.width}, height:${bitmap?.height}")

        bitmapRect.set(
                0f,
                0f,
                this.bitmap?.width?.toFloat() ?: 0f,
                this.bitmap?.height?.toFloat() ?: 0f
        )

        val bitmapMinRectSize = max(bitmapRect.width(), bitmapRect.height()) / MAX_SCALE
        bitmapMinRect.set(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)

        initialize()

        requestLayout()
        invalidate()
    }

    /**
     * Current crop size depending on original bitmap.
     * Returns rectangle as pixel values.
     */
    //TODO
    fun getCropSizeOriginal(): RectF {
        val cropSizeOriginal = RectF()
        cropRectOnOriginalBitmapMatrix.reset()
        bitmapMatrix.invert(cropRectOnOriginalBitmapMatrix)
        cropRectOnOriginalBitmapMatrix.mapRect(cropSizeOriginal, cropRect)

        Log.d(TAG, "getCropSizeOriginal() cropRect:$cropRect")
        Log.d(TAG, "getCropSizeOriginal() cropSizeOriginal:$cropSizeOriginal")

        return cropSizeOriginal
    }

    /**
     * Get cropped bitmap.
     */
    fun getCroppedData(): Bitmap {
        Log.d(TAG, "getCroppedData")
        val croppedBitmapRect = getCropSizeOriginal()
        val sourceRect = RectF(bitmapRect)

        if (sourceRect.intersect(croppedBitmapRect).not()) {
            return bitmap!!
        }

        val cropLeft = if (croppedBitmapRect.left.roundToInt() < sourceRect.left) {
            sourceRect.left.toInt()
        } else {
            croppedBitmapRect.left.roundToInt()
        }

        val cropTop = if (croppedBitmapRect.top.roundToInt() < sourceRect.top) {
            sourceRect.top.toInt()
        } else {
            croppedBitmapRect.top.roundToInt()
        }

        val cropRight = if (croppedBitmapRect.right.roundToInt() > sourceRect.right) {
            sourceRect.right.toInt()
        } else {
            croppedBitmapRect.right.roundToInt()
        }

        val cropBottom = if (croppedBitmapRect.bottom.roundToInt() > sourceRect.bottom) {
            sourceRect.bottom.toInt()
        } else {
            croppedBitmapRect.bottom.roundToInt()
        }

        bitmap?.let {
            val croppedBitmap = Bitmap.createBitmap(
                    it, cropLeft, cropTop, cropRight - cropLeft, cropBottom - cropTop
            )
            return croppedBitmap
        }

        Log.d(TAG, "Bitmap is null.")
        throw IllegalStateException("Bitmap is null.")
    }

    /**
     * Initialize
     */
    private fun initialize() {
        Log.d(TAG, "initialize() measuredWidth: $measuredWidth, measuredHeight: $measuredHeight")
        Log.d(TAG, "initialize() marginInPixelSize: $marginInPixelSize")
        viewWidth = measuredWidth.toFloat() - (marginInPixelSize * 2)

        viewHeight = measuredHeight.toFloat() - (marginInPixelSize * 2)

        viewRect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())

        initializeBitmapMatrix()

        initializeCropRect()

        onInitialized?.invoke()

        invalidate()
    }

    private fun drawMask(canvas: Canvas?) {
        val circularPath = Path()
        circularPath.addCircle(
                cropRect.centerX(), cropRect.centerY(), cropRect.height() / 2f, Path.Direction.CW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas?.clipOutPath(circularPath)
        } else {
            canvas?.clipPath(circularPath, Region.Op.DIFFERENCE)
        }
        canvas?.drawColor(0x88000000.toInt()) //TODO
    }

    private fun drawAim(canvas: Canvas?) {
        //vertical
        canvas?.drawLine(
                width / 2f,
                //viewRect.top,
                cropRect.top,
                width / 2f,
                //viewRect.bottom,
                cropRect.bottom,
                cropPaint
        )
        //horizontal
        canvas?.drawLine(
                //viewRect.left,
                //viewRect.top + viewRect.height() / 2f,
                //viewRect.right,
                //viewRect.top + viewRect.height() / 2f,
                cropRect.left, cropRect.top + cropRect.height() / 2f,
                cropRect.right, cropRect.top + cropRect.height() / 2f,
                cropPaint
        )
    }

    /** portrait only */
    private fun initializeBitmapMatrix() {
        val scale = if (bitmapRect.width() <= bitmapRect.height()) {
            viewWidth / bitmapRect.width() //portrait
        } else {
            viewWidth / bitmapRect.height()
        }
        bitmapMatrix.setScale(scale, scale)
        val translateX = (viewWidth - bitmapRect.width() * scale) / 2f + marginInPixelSize
        val translateY = (viewHeight - bitmapRect.height() * scale) / 2f + marginInPixelSize
        bitmapMatrix.postTranslate(translateX, translateY)

        Log.d(TAG, "initializeBitmapMatrix() viewWidth: $viewWidth, viewHeight: $viewHeight " +
                "\ninitializeBitmapMatrix() bitmapRect.width: ${bitmapRect.width()}, bitmapRect.height: ${bitmapRect.height()}" +
                "initializeBitmapMatrix() scale: ${scale}" +
                "initializeBitmapMatrix() translateX: " + "$translateX, translateY: $translateY")
    }

    /** portrait only */
    private fun initializeCropRect() {
        Log.d(TAG, "initializeCropRect() cropRect:${cropRect}")
        val top = (viewHeight - viewWidth) / 2 + marginInPixelSize
        val left = 0f + marginInPixelSize
        val cropLength = viewWidth
        cropRect.set(left, top, left + cropLength, top + cropLength)
    }

    /**
     * when user drag bitmap too much, we need to settle bitmap matrix
     * back to the possible closest edge.
     */
    private fun settleDraggedBitmap() {
        val draggedBitmapRect = RectF()
        bitmapMatrix.mapRect(draggedBitmapRect, bitmapRect)

        /**
         * Scale dragged matrix if it needs to
         */
        val widthScale = cropRect.width() / draggedBitmapRect.width()
        val heightScale = cropRect.height() / draggedBitmapRect.height()
        var scale = 1.0f

        if (widthScale > 1.0f || heightScale > 1.0f) {
            scale = max(widthScale, heightScale)
        }

        /**
         * Calculate new scaled matrix for dragged bitmap matrix
         */
        val scaledRect = RectF()
        val scaledMatrix = Matrix()
        scaledMatrix.setScale(scale, scale)
        scaledMatrix.mapRect(scaledRect, draggedBitmapRect)


        /**
         * Calculate translateX
         */
        var translateX = 0f
        if (scaledRect.left > cropRect.left) {
            translateX = cropRect.left - scaledRect.left
        }

        if (scaledRect.right < cropRect.right) {
            translateX = cropRect.right - scaledRect.right
        }

        /**
         * Calculate translateX
         */
        var translateY = 0f
        if (scaledRect.top > cropRect.top) {
            translateY = cropRect.top - scaledRect.top
        }

        if (scaledRect.bottom < cropRect.bottom) {
            translateY = cropRect.bottom - scaledRect.bottom
        }

        /**
         * New temp bitmap matrix
         */
        val newBitmapMatrix = bitmapMatrix.clone()

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(translateX, translateY)
        newBitmapMatrix.postConcat(matrix)

        bitmapMatrix.animateToMatrix(newBitmapMatrix) {
            invalidate()
            notifyCropRectChanged()
        }
    }

    /**
     * Pretend a bitmap matrix value if scale factor will be applied to
     * bitmap matrix. , then returns
     * true, false otherwise.
     * @return true If pretended value is exceed max scale value, false otherwise
     */
    private fun isBitmapScaleExceedMaxLimit(scaleFactor: Float): Boolean {
        val bitmapMatrixCopy = bitmapMatrix.clone()
        bitmapMatrixCopy.preScale(scaleFactor, scaleFactor)

        val invertedBitmapMatrix = Matrix()
        bitmapMatrixCopy.invert(invertedBitmapMatrix)

        val invertedBitmapCropRect = RectF()

        invertedBitmapMatrix.mapRect(invertedBitmapCropRect, cropRect)
        return min(
                invertedBitmapCropRect.width(),
                invertedBitmapCropRect.height()
        ) <= bitmapMinRect.width()
    }

    private fun notifyCropRectChanged() {
        observeCropRectOnOriginalBitmapChanged?.invoke(getCropSizeOriginal())
    }

    companion object {
        /**
         * Maximum scale for given bitmap
         */
        private const val MAX_SCALE = 15f

        /**
         * Use this constant, when user double tap to scale
         */
        private const val DOUBLE_TAP_SCALE_FACTOR = 2f

    }
}