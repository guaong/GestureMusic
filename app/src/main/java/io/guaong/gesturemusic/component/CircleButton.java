package io.guaong.gesturemusic.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import io.guaong.gesturemusic.config.ColorConfig;
import io.guaong.gesturemusic.util.WindowUtil;

/**
 * Created by 关桐 on 2017/8/7.
 * 圆形外框按钮
 */

public abstract class CircleButton extends View {

    private Paint mPaint;
    private DrawFilter mDrawFilter;

    /* 用于绘制ClickedMenu按钮 */
    private float leftTopX, leftTopY, leftBottomX, leftBottomY;
    private float rightTopX, rightTopY, rightBottomX, rightBottomY;

    private int r;
    private int mWidth, mHeight;
    private int mCircleCenterX, mCircleCenterY;

    private float mTextSize;

    private final int PAINT_COLOR = ColorConfig.BUTTON_CUPCAKE_COLOR;

    public CircleButton(Context context) {
        super(context);
    }

    public CircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        // 初始绘制波纹的画笔
        mPaint = new Paint();
        // 去除画笔锯齿
        mPaint.setAntiAlias(true);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        if (w <= 160){
            r = Math.min(mHeight, mWidth) / 2 - 5;
        }else {
            r = (Math.min(mHeight, mWidth) / 2 - 5) * 2 / 3;
        }
        leftTopX = (float) (mWidth / 2 - r);
        leftTopY = (float)(mHeight / 2 - r);
        leftBottomX = leftTopX;
        leftBottomY = mHeight - leftTopY;
        rightTopX = mWidth - leftTopX;
        rightTopY = leftTopY;
        rightBottomX = rightTopX;
        rightBottomY = leftBottomY;

        mCircleCenterX = mWidth / 2;
        mCircleCenterY = mHeight / 2 ;

        mTextSize = WindowUtil.pxToSp(getContext(), WindowUtil.dipToPx(getContext(), r / 2));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /* 精确和填充模式时的宽高值 */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int flag = Math.min(widthSize / 5, heightSize / 5);
        /* 自适应时的宽高值 */
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = flag;
        }
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = widthSize;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(PAINT_COLOR);
        // 设置画笔粗细
        if (mWidth <= 160){
            mPaint.setStrokeWidth(3);
        }else {
            mPaint.setStrokeWidth(4.5f);
        }
    }

    /**
     * 子类必须自定义方法
     * 改变按钮外观
     */
    public abstract void changeStatus(int status);

    /**
     * 获得下一状态
     */
    public abstract int getNextStatus();

    /**
     * 绘制点击过的按钮
     */
    protected void drawClickedMenuBtn(Canvas canvas){
        // 屏幕分辨率，兼容1080*720及以下，1920*1080及以上
        if (mWidth <= 160){
            canvas.drawLine(leftTopX + 40, leftTopY + 40, rightBottomX - 40, rightBottomY - 40, mPaint);
            canvas.drawLine(rightTopX - 40, rightTopY + 40, leftBottomX + 40, leftBottomY - 40, mPaint);
        }else {
            canvas.drawLine(leftTopX + 60, leftTopY + 60, rightBottomX - 60, rightBottomY - 60, mPaint);
            canvas.drawLine(rightTopX - 60, rightTopY + 60, leftBottomX + 60, leftBottomY - 60, mPaint);
        }

    }

    /**
     * 画外框的圆
     */
    protected void drawCircle(Canvas canvas){
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, r, mPaint);
    }

    /**
     * 绘制里面文字
     */
    protected void drawText(Canvas canvas, String text){
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(1f);
        mPaint.setTextSize(mTextSize);
        Rect targetRect = new Rect((int)leftTopX, (int)leftTopY, (int)rightBottomX, (int)rightBottomY);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, targetRect.centerX(), baseline, mPaint);
    }


}
