package io.guaong.gesturemusic.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

import io.guaong.gesturemusic.config.ColorConfig;

/**
 * Created by 关桐 on 2017/8/5.
 * 一个简单的缩放切换播放按钮
 */

public class PlayButton extends View {

    // 播放状态
    public static final int PLAY = 1;
    // 暂停状态
    public static final int PAUSE = 2;
    // 播放到暂停状态第一步，缩小三角
    public static final int NARROW_TRIANGLE = 3;
    // 暂停到播放状态第一步，缩小两条线
    public static final int NARROW_LINE = 4;
    // 播放到暂停状态第二步，扩大两条线
    public static final int EXPAND_TRIANGLE = 5;
    // 暂停到播放状态第二步，扩大三角
    public static final int EXPAND_LINE = 6;
    // 由于加粗Paint会导致绘制超出边界，因此向内收缩
    private final int PAINT_WIDTH = 10;
    private final int BORDER_DISTANCE = 80;

    private final int PAINT_COLOR = ColorConfig.BUTTON_CUPCAKE_COLOR;

    public static AnimationHandler animationHandler;
    /* 坐标 */
    private float mLeftTopX;
    private float mLeftTopY;
    private float mLeftBottomX;
    private float mLeftBottomY;
    private float mRightTopX;
    private float mRightTopY;
    private float mRightBottomX;
    private float mRightBottomY;
    private float mRightCenterX;
    private float mRightCenterY;
    // 状态
    private int status;
    // 动画时每次三角改变（缩放）值
    private int mTriangleChangeValue = 0;
    // 动画时每次线改变（缩放）值
    private int mLineChangeValue = 0;
    // 组件宽高
    private int mWidth, mHeight;

    private Paint mPaint;

    private DrawFilter mDrawFilter;

    public PlayButton(Context context) {
        super(context);
    }

    public PlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始绘制波纹的画笔
        mPaint = new Paint();
        // 去除画笔锯齿
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(PAINT_COLOR);
        // 设置画笔粗细
        if (mWidth <= 480){
            mPaint.setStrokeWidth(3);
        }else {
            mPaint.setStrokeWidth(4.5f);
        }

        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        animationHandler = new AnimationHandler(this);
        status = PLAY;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /* 获取一些坐标以及长宽值 */
        mWidth = w;
        mHeight = h;
        /* 兼容手机分辨率 */
        if (w <= 480){
            mLeftTopX = (float)(mWidth / 2 -
                    (mWidth / 2 - BORDER_DISTANCE * 2) * Math.cos(Math.PI / 3));
            mLeftTopY = (float)(mHeight / 2 -
                    (mHeight / 2 - BORDER_DISTANCE * 2) * Math.sin(Math.PI / 3));
        }else {
            mLeftTopX = (float)(mWidth / 2 -
                    (mWidth / 2 - BORDER_DISTANCE * 2) * Math.cos(Math.PI / 3) * 2 / 3);
            mLeftTopY = (float)(mHeight / 2 -
                    (mHeight / 2 - BORDER_DISTANCE * 2) * Math.sin(Math.PI / 3) * 2 / 3);
        }
        mLeftBottomX = mLeftTopX;
        mLeftBottomY = mHeight - mLeftTopY;
        mRightTopX = mWidth - mLeftTopX;
        mRightTopY = mLeftTopY;
        mRightBottomX = mRightTopX;
        mRightBottomY = mLeftBottomY;
        mRightCenterX = (float)((mLeftBottomY - mLeftTopY) * Math.sin(Math.PI / 3) + mLeftTopX);
        mRightCenterY = (float)(mLeftBottomY - (Math.cos(Math.PI / 3) * (mLeftBottomY - mLeftTopY)));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /* 精确和填充模式时的宽高值 */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int flag = Math.min(widthSize * 2 / 3, heightSize * 2 / 3);

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
        // 绘制外部边框的圆形
        drawOutsideCircle(canvas);
        /* 暂停到播放，三角收缩，接着竖线放大 */
        /* 播放到暂停，竖线收缩，接着三角放大 */
        switch (status){
            case PLAY:
                drawInsideTriangle(canvas, 0);
                mTriangleChangeValue = 0;
                break;
            case NARROW_TRIANGLE:
                mTriangleChangeValue += 2;
                drawInsideTriangle(canvas, mTriangleChangeValue);
                if (mTriangleChangeValue >= 20){
                    status = EXPAND_LINE;
                }
                invalidate();
                break;
            case EXPAND_LINE:
                mTriangleChangeValue -= 5;
                drawInsideDoubleLine(canvas, mTriangleChangeValue);
                if (mTriangleChangeValue <= 0){
                    status = PAUSE;
                }
                invalidate();
                break;
            case NARROW_LINE:
                mLineChangeValue += 2;
                drawInsideDoubleLine(canvas, mLineChangeValue);
                if (mLineChangeValue >= 20){
                    status = EXPAND_TRIANGLE;
                }
                invalidate();
                break;
            case EXPAND_TRIANGLE:
                mLineChangeValue -= 5;
                drawInsideTriangle(canvas, mLineChangeValue);
                if (mLineChangeValue <= 0){
                    status = PLAY;
                }
                invalidate();
                break;
            case PAUSE:
                drawInsideDoubleLine(canvas, 0);
                mLineChangeValue = 0;
                break;
        }
    }

    /**
     * 绘制外圈的圆形
     * @param canvas 画布
     */
    private void drawOutsideCircle(Canvas canvas){
        // 圆的半径
        int r = mWidth / 2 - PAINT_WIDTH;
        // 画圆
        canvas.drawCircle(mWidth / 2, mHeight / 2, r, mPaint);
    }

    /**
     * 绘制内圈的三角形
     * @param canvas 画布
     */
    private void drawInsideTriangle(Canvas canvas, int move){
        //使用双缓冲，先将所画内容存放至缓冲区
        final Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas();
        c.setBitmap(bitmap);
        final Path p = new Path();
        p.moveTo((float)(mLeftTopX + move * Math.sin(Math.PI * 4))
                , (float)(mLeftTopY + move * Math.cos(Math.PI * 4)));
        p.lineTo((float)(mLeftBottomX + move * Math.sin(Math.PI * 4))
                , (float)(mLeftBottomY - move * Math.cos(Math.PI * 4)));
        p.lineTo(mRightCenterX - move, mRightCenterY);
        p.close();
        c.drawPath(p, mPaint);
        canvas.drawBitmap(bitmap, 0, 0, mPaint);
    }

    /**
     * 绘制两条线
     * @param canvas 画布
     */
    private void drawInsideDoubleLine(Canvas canvas, float move){
        canvas.drawLine(mLeftTopX, mLeftTopY + move, mLeftBottomX, mLeftBottomY - move, mPaint);
        canvas.drawLine(mRightTopX, mRightTopY + move, mRightBottomX, mRightBottomY - move, mPaint);
    }

    /**
     * 接受来自activity的消息
     * 通过MusicActivity发送消息告知播放按钮改变样式
     * 位置原因，不能像其他按钮使用postInvalidate在外部使用，只好使用该方法
     */
    public static class AnimationHandler extends Handler{

        private WeakReference<PlayButton> mWeakReference;
        private PlayButton mBtn;

        AnimationHandler(PlayButton playButton){
            mWeakReference = new WeakReference<>(playButton);
            mBtn = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1){
                case PLAY:
                    mBtn.status = NARROW_TRIANGLE;
                    mBtn.postInvalidate();
                    break;
                case PAUSE:
                    mBtn.status = NARROW_LINE;
                    mBtn.postInvalidate();
                    break;
            }
        }
    }
}
