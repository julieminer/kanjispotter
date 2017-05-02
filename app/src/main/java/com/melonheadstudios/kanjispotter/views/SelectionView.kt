package com.melonheadstudios.kanjispotter.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.melonheadstudios.kanjispotter.BuildConfig
import com.melonheadstudios.kanjispotter.extensions.pixels
import com.melonheadstudios.kanjispotter.viewmodels.TextSelection

/**
 * kanjispotter
 * Created by jake on 2017-04-30, 8:24 PM
 */
class SelectionView @JvmOverloads constructor(internal var context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    interface SelectionViewDelegate {
        fun selectedSegment(segment: String)
    }

    var selectionsList = ArrayList<TextSelection>()
        set(value) {
            clearData()
            field = value
        }

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

    private var mWidth = 1
    private var mHeight = 1
    private val textSize = 80f

    var delegate: SelectionViewDelegate? = null

    init {
        // TODO get colours from style
        circlePaint.isAntiAlias = true
        circlePaint.color = Color.BLUE
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeJoin = Paint.Join.MITER
        circlePaint.strokeWidth = 4f
        textPaint.color = Color.BLACK
        textPaint.textSize = textSize

        textHighlightPaint.color = Color.GREEN
        textHighlightPaint.textSize = textSize

        textHighlightPaintBackground.isAntiAlias = true
        textHighlightPaintBackground.color = Color.BLUE

        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = Color.GREEN
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = 12f

        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Compute the height required to render the view
        // Assume Width will always be MATCH_PARENT.
        var width = View.MeasureSpec.getSize(widthMeasureSpec)
        var totalX = 0f
        for (selection in selectionsList) {
            selection.calculateRect(totalX, y, textPaint)
            totalX += selection.rect.width()
        }
        if (totalX > 0f) {
            width = totalX.toInt()
        }
        val height = context.pixels(50f).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = maxOf(w, 1)
        mHeight = maxOf(h, 1)
        super.onSizeChanged(mWidth, mHeight, oldw, oldh)
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(mBitmap, 0f, 0f, mBitmapPaint)
        canvas.drawPath(circlePath, circlePaint)
        val x = 0f
        val y = (mHeight.toFloat() * 2) / 3

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
        }
    }

    private fun clearData() {
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
        mPath.close()
        mPath.reset()
        circlePath.close()
        circlePath.reset()
        parent.requestLayout()
        invalidate()
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

    private fun inBox(x1: Int, y1: Int, rect: Rect): Boolean {
        return rect.contains(x1, y1)
    }

    private fun touch_up() {
        mPath.lineTo(mX, mY)
        circlePath.reset()
        mPath.reset()

        val selections = selectionsList.filter { it.selected }
        var selectedString = ""
        for (selected in selections) {
            selectedString += selected.text
        }
        delegate?.selectedSegment(selectedString)
        selections.forEach { it.selected = false }
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