package com.recordapp.record.view


import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import com.recordapp.record.R

/**
 * 普通环形进度条
 */
class CircleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mBgPaint: Paint? = null//背景弧线paint
    private var mProgressPaint: Paint? = null//进度Paint
    private var mProgressWidth: Float = 0.toFloat()//进度条宽度
    private var mProgressColor = Color.parseColor("#FB1F72")//进度条颜色
    private var locationStart: Int = 0//起始位置
    private var startAngle: Float = 0.toFloat()//开始角度
    private val maxSecond = 90//90秒
    private var progress = 0//当前进度
    /**
     * 设置进度
     *
     * @param current
     */
    var current: Int = 0
        set(current) {
            field = current
            invalidate()
        }

    private val mHandler = Handler()
    private val task = object : Runnable {
        override fun run() {
            if (progress >= maxSecond) {
                if (mOnAnimProgressListener != null) {
                    mOnAnimProgressListener!!.recordFinish(progress)
                    current = progress
                }
                reset()
            } else {
                if (mOnAnimProgressListener != null) {
                    mOnAnimProgressListener!!.valueUpdate(progress)
                }
                current = progress
                progress++
                mHandler.postDelayed(this, 1000)
            }
        }
    }

    private var mOnAnimProgressListener: OnAnimProgressListener? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView)
        locationStart = typedArray.getInt(R.styleable.CircleProgressView_location_start, 1)
        mProgressWidth =
            typedArray.getDimension(R.styleable.CircleProgressView_progress_width, dp2px(context, 4f).toFloat())
        mProgressColor = typedArray.getColor(R.styleable.CircleProgressView_progress_color, mProgressColor)
        typedArray.recycle()

        //背景圆弧
        mBgPaint = Paint()
        mBgPaint!!.isAntiAlias = true
        mBgPaint!!.strokeWidth = mProgressWidth
        mBgPaint!!.style = Paint.Style.STROKE
        mBgPaint!!.color = Color.parseColor("#eaecf0")
        mBgPaint!!.strokeCap = Paint.Cap.ROUND

        //进度圆弧
        mProgressPaint = Paint()
        mProgressPaint!!.isAntiAlias = true
        mProgressPaint!!.style = Paint.Style.STROKE
        mProgressPaint!!.strokeWidth = mProgressWidth
        mProgressPaint!!.color = mProgressColor
        mProgressPaint!!.strokeCap = Paint.Cap.ROUND

        //进度条起始角度
        if (locationStart == 1) {//左
            startAngle = -180f
        } else if (locationStart == 2) {//上
            startAngle = -90f
        } else if (locationStart == 3) {//右
            startAngle = 0f
        } else if (locationStart == 4) {//下
            startAngle = 90f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        val size = if (width < height) width else height
        setMeasuredDimension(size, size)
    }

    /**
     * oval  // 绘制范围
     * startAngle  // 开始角度
     * sweepAngle  // 扫过角度
     * useCenter   // 是否使用中心
     */
    override fun onDraw(canvas: Canvas) {
        //绘制背景圆弧
        val rectF =
            RectF(mProgressWidth / 2, mProgressWidth / 2, width - mProgressWidth / 2, height - mProgressWidth / 2)
        //        canvas.drawArc(rectF, 0, 360, false, mBgPaint);

        //绘制当前进度
        val sweepAngle = (360 * current / maxSecond).toFloat()
        canvas.drawArc(rectF, startAngle, sweepAngle, false, mProgressPaint!!)
    }


    /**
     * 动画效果
     */
    fun startAnimProgress() {
        mHandler.postDelayed(task, 0)
    }

    fun stopRecord() {
        mHandler.removeCallbacks(task)
    }

    fun pauseRecord() {
        mHandler.removeCallbacks(task)
    }

    fun restartRecord() {
        mHandler.postDelayed(task, 0)
    }

    fun reset() {
        this.progress = 0
        mHandler.removeCallbacks(task)
        invalidate()
    }

    interface OnAnimProgressListener {
        fun valueUpdate(progress: Int)

        fun recordFinish(progress: Int)
    }

    /**
     * 监听进度条进度
     *
     * @param onAnimProgressListener
     */
    fun setOnAnimProgressListener(onAnimProgressListener: OnAnimProgressListener) {
        mOnAnimProgressListener = onAnimProgressListener
    }

    companion object {


        fun dp2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }
}
