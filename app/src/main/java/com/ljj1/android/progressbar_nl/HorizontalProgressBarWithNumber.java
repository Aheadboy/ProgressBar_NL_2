package com.ljj1.android.progressbar_nl;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ProgressBar;
import android.widget.Toast;


/**
 * mProgressBar.setOnTimeOutCallBack(MainActivity.this);////进度条初始化后，必须设置回调对象。否则nullEx
 */
public class HorizontalProgressBarWithNumber extends ProgressBar {

    private static final int DEFAULT_TEXT_SIZE = 10;
    private static final int DEFAULT_TEXT_COLOR = 0XFFFC00D1;
    private static final int DEFAULT_TIME_OUT = 10;//second
    private static final int DEFAULT_COLOR_UNREACHED_COLOR = 0xFFd3d6da;
    private static final int DEFAULT_HEIGHT_REACHED_PROGRESS_BAR = 2;
    private static final int DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR = 2;
    private static final int DEFAULT_SIZE_TEXT_OFFSET = 10;
    private int[] colorChange = {0XFFff0000, 0XFF6600ff, 0xFFffff00, 0XFFFC00D1};
    private int arrLength = colorChange.length;
    private int bigI = 0;
    private static final int MSG_PROGRESS_UPDATE = 0x110;
    private boolean stop = false;
    private boolean start = true;
    private boolean pause = false;
    private boolean timeOuttime = true;
    private int startColor, endColor;
    /**
     * painter of all drawing things
     */
    protected ITimeOutCallback timeOutCallback = null;
    /**
     * painter of all drawing things
     */
    protected Paint mPaint = new Paint();
    /**
     * time out default
     * second
     */
    protected int mTimeOut = DEFAULT_TIME_OUT;
    /**
     * color of progress number
     */
    protected int mTextColor = DEFAULT_TEXT_COLOR;
    /**
     * size of text (sp)
     */
    protected int mTextSize = sp2px(DEFAULT_TEXT_SIZE);

    /**
     * offset of draw progress
     */
    protected int mTextOffset = dp2px(DEFAULT_SIZE_TEXT_OFFSET);

    /**
     * height of reached progress bar
     */
    protected int mReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_REACHED_PROGRESS_BAR);

    /**
     * color of reached bar
     */
    protected int mReachedBarColor = DEFAULT_TEXT_COLOR;
    /**
     * color of unreached bar
     */
    protected int mUnReachedBarColor = DEFAULT_COLOR_UNREACHED_COLOR;
    /**
     * height of unreached progress bar
     */
    protected int mUnReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR);
    /**
     * view width except padding
     */
    protected int mRealWidth;

    protected boolean mIfDrawText = true;
    protected boolean mTextGone = false;

    protected static final int VISIBLE = 0;
    protected static final int GONE = 3;

    public HorizontalProgressBarWithNumber(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalProgressBarWithNumber(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        obtainStyledAttributes(attrs);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);
        mHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);

        mRealWidth = getMeasuredWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            float textHeight = (mPaint.descent() - mPaint.ascent());
            result = (int) (getPaddingTop() + getPaddingBottom() + Math.max(
                    Math.max(mReachedProgressBarHeight,
                            mUnReachedProgressBarHeight), Math.abs(textHeight)));
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * get the styled attributes
     *
     * @param attrs
     */
    private void obtainStyledAttributes(AttributeSet attrs) {
        // init values from custom attributes
        final TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalProgressBarWithNumber);

        mTimeOut = attributes
                .getInteger(
                        R.styleable.HorizontalProgressBarWithNumber_progress_time_out,
                        DEFAULT_TIME_OUT);

        mTextColor = attributes
                .getColor(
                        R.styleable.HorizontalProgressBarWithNumber_progress_text_color,
                        DEFAULT_TEXT_COLOR);
        mTextSize = (int) attributes.getDimension(
                R.styleable.HorizontalProgressBarWithNumber_progress_text_size,
                mTextSize);

        mReachedBarColor = attributes
                .getColor(
                        R.styleable.HorizontalProgressBarWithNumber_progress_reached_color,
                        mTextColor);
        mUnReachedBarColor = attributes
                .getColor(
                        R.styleable.HorizontalProgressBarWithNumber_progress_unreached_color,
                        DEFAULT_COLOR_UNREACHED_COLOR);
        mReachedProgressBarHeight = (int) attributes
                .getDimension(
                        R.styleable.HorizontalProgressBarWithNumber_progress_reached_bar_height,
                        mReachedProgressBarHeight);
        mUnReachedProgressBarHeight = (int) attributes
                .getDimension(
                        R.styleable.HorizontalProgressBarWithNumber_progress_unreached_bar_height,
                        mUnReachedProgressBarHeight);
        mTextOffset = (int) attributes
                .getDimension(
                        R.styleable.HorizontalProgressBarWithNumber_progress_text_offset,
                        mTextOffset);

        int textVisible = attributes
                .getInt(R.styleable.HorizontalProgressBarWithNumber_progress_text_visibility,
                        VISIBLE);
        startColor = attributes.getColor(R.styleable.HorizontalProgressBarWithNumber_startColor, Color.RED);
        endColor = attributes.getColor(R.styleable.HorizontalProgressBarWithNumber_endColor, Color.GREEN);

        if (textVisible != VISIBLE) {
            mIfDrawText = false;
        }
        if (textVisible == GONE) {
            mTextGone = true;
        }
        attributes.recycle();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getHeight() / 2);

        boolean noNeedBg = false;
        float radio = getProgress() * 1.0f / getMax();
        float progressPosX = (int) (mRealWidth * radio);
        String text = getProgress() + "%";
        // mPaint.getTextBounds(text, 0, text.length(), mTextBound);

        float textWidth = mPaint.measureText(text);
        if (mTextGone) textWidth = 0;
        float textHeight = (mPaint.descent() + mPaint.ascent()) / 2;

        if (progressPosX + textWidth > mRealWidth) {
            progressPosX = mRealWidth - textWidth;
            noNeedBg = true;
        }

        // draw reached bar
        float endX = progressPosX - mTextOffset / 2;
        if (endX > 0) {
            mPaint.setColor(mReachedBarColor);
            //设置渐变色区域
            LinearGradient shader = new LinearGradient(0, 0, endX, 0, new int[]{startColor, endColor}, null, Shader.TileMode.CLAMP);
            mPaint.setShader(shader);
            mPaint.setStrokeWidth(mReachedProgressBarHeight);
            canvas.drawLine(0, 0, endX, 0, mPaint);
        }
        // draw progress bar
        // measure text bound
        if (mIfDrawText) {
            mPaint.setColor(mTextColor);
            canvas.drawText(text, progressPosX, -textHeight, mPaint);
        }

        // draw unreached bar
        if (!noNeedBg) {
            float start = progressPosX + mTextOffset / 2 + textWidth;
            mPaint.setColor(mUnReachedBarColor);
            mPaint.setStrokeWidth(mUnReachedProgressBarHeight);
            canvas.drawLine(start, 0, mRealWidth, 0, mPaint);
        }

        canvas.restore();

    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp 2 px
     *
     * @param spVal
     * @return
     */
    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());

    }

    public int getIncrese(
    ) {
        if (bigI == arrLength - 1) {
            return bigI = bigI + 1 - arrLength;
        } else {
            return bigI = bigI + 1;
        }
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int progress = HorizontalProgressBarWithNumber.this.getProgress();
            if (stop) {
                setProgress(0);
            }
            if (start) {
                HorizontalProgressBarWithNumber.this.setProgress(++progress);
            }
            if (pause) {
                HorizontalProgressBarWithNumber.this.setProgress(progress);
            }
            if (progress >= 100) {
                mHandler.removeMessages(MSG_PROGRESS_UPDATE);
                if (timeOuttime && timeOutCallback != null) {
                    timeOutCallback.timeOut(mTimeOut);
                    timeOuttime = !timeOuttime;
                }
            }
            mHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, mTimeOut * 1000 / 100);
        }
    };

    public void stop() {
        stop = true;
        start = false;
        pause = false;
    }

    public void pause() {
        stop = false;
        start = false;
        pause = true;
    }

    public void start() {
        stop = false;
        start = true;
        pause = false;
        timeOuttime = true;
    }

    public void setOnTimeOutCallBack(ITimeOutCallback to) {
        this.timeOutCallback = to;
    }
}
