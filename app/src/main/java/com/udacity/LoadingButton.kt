package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    private var buttonIdleColor: Int = 0
    private var buttonLoadingFillColor: Int = 0
    private var buttonLoadingCircleColor: Int = 0
    private var textColor: Int = 0

    private var  valueAnimator: ValueAnimator
    private var loadingIndicator: Double = 0.0
    private var animationDuration: Long = 3000

    private var downloadDone = false

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
    }

    fun downloadCompleted () {
        downloadDone = true
    }

    init {
        isClickable = true

        valueAnimator = ValueAnimator.ofFloat(0f, 100f).apply {
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { progress ->
                loadingIndicator = (progress.animatedValue as Float).toDouble()
                if (downloadDone && loadingIndicator in 99.0..100.0) {
                    cancel()
                    buttonState = ButtonState.Completed
                    downloadDone = false
                } else if (loadingIndicator in 99.0..100.0) {
                    cancel()
                    duration += 2000
                    start()
                }
                invalidate()
            }
        }

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonIdleColor = getColor(R.styleable.LoadingButton_idleColor, 0)
            buttonLoadingFillColor = getColor(R.styleable.LoadingButton_loadingFillColor, 0)
            buttonLoadingCircleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
        }
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 60f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    override fun performClick(): Boolean {
        super.performClick()

        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Loading
            valueAnimator.duration = animationDuration
            valueAnimator.start()
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = buttonIdleColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        if (buttonState == ButtonState.Loading) {
            paint.color = buttonLoadingFillColor
            canvas.drawRect(
                0f, 0f,
                (width * (loadingIndicator / 100)).toFloat(), height.toFloat(), paint
            )
            paint.color = buttonLoadingCircleColor
            canvas.drawArc(
                (width/2+140).toFloat(),
                (height/2-30).toFloat(),
                (width/2+200).toFloat(),
                (height/2 + 30).toFloat(),
                0f,
                (loadingIndicator * 360 / 100).toFloat(),
                true,
                paint
            )
        }

        val text = if (buttonState == ButtonState.Loading) resources.getString(R.string.loading) else resources.getString(R.string.download)
        paint.color = textColor
        canvas.drawText(
            text, (width/2).toFloat(), (height/2+15).toFloat(),
            paint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}