package com.melonheadstudios.kanjispotter.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.viewmodels.TextSelection

/**
 * kanjispotter
 * Created by jake on 2017-04-30, 8:24 PM
 */
class SelectionView @JvmOverloads constructor(internal var context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {
    var selectionsList = ArrayList<TextSelection>()
    private val TOUCH_TOLERANCE = 4f
    private var mPaint = Paint()
    private val textPaint = Paint()
    private val textHighlightPaint = Paint()
    private val textHighlightPaintBackground = Paint()
    private val mBitmapPaint: Paint = Paint(Paint.DITHER_FLAG)
    private val circlePaint: Paint = Paint()
    private val circlePath: Path = Path()

    private val mPath: Path = Path()
    private var mBitmap: Bitmap
    private var mCanvas: Canvas

    private var mX = 0f
    private var mY = 0f

    init {
        circlePaint.isAntiAlias = true
        circlePaint.color = Color.BLUE
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeJoin = Paint.Join.MITER
        circlePaint.strokeWidth = 4f
        textPaint.color = Color.BLACK
        textPaint.textSize = 136f

        textHighlightPaint.color = Color.GREEN
        textHighlightPaint.textSize = 136f

        textHighlightPaintBackground.isAntiAlias = true
        textHighlightPaintBackground.color = Color.BLUE

        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = Color.GREEN
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = 12f

        mBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(mBitmap, 0f, 0f, mBitmapPaint)
        canvas.drawPath(mPath, mPaint)
        canvas.drawPath(circlePath, circlePaint)
        val x = 0f
        val y = (height / 2).toFloat()

        var totalX = x
        for (selection in selectionsList) {
            selection.calculateRect(totalX, y, textPaint)
            if (selection.selected) {
                canvas.drawRect(selection.rect, textHighlightPaintBackground)
                canvas.drawText(selection.text, x + totalX, y, textHighlightPaint)
            } else {
                canvas.drawText(selection.text, x + totalX, y, textPaint)
            }
            totalX += x + selection.rect.width()
            if (BuildConfig.DEBUG) {
                canvas.drawRect(selection.rect, mPaint)
            }
        }
    }

    private fun touch_start(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touch_move(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y

            circlePath.reset()
            circlePath.addCircle(mX, mY, 30f, Path.Direction.CW)
        }

        selectionsList
                .filter { inBox(x.toInt(), y.toInt(), it.rect) }
                .forEach { it.selected = true }
    }

    fun inBox(x1: Int, y1: Int, rect: Rect): Boolean {
        return rect.contains(x1, y1)
    }

    private fun touch_up() {
        mPath.lineTo(mX, mY)
        circlePath.reset()
        mCanvas.drawPath(mPath, mPaint)
        mPath.reset()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touch_start(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touch_move(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touch_up()
                invalidate()
            }
        }
        return true
    }
}