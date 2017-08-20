package io.guaong.gesturemusic.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import io.guaong.gesturemusic.config.ColorConfig;

/**
 * Created by 关桐 on 2017/8/8.
 * 一变三按钮组
 */
public class OneToThreeButtonGroup extends ViewGroup {

    // 控件宽高
    private int mWidth, mHeight;
    // 每行每列坐标（控件为两行三列）
    private int firstX, secondX, thirdX, fourthX, fifthX, sixthX;
    private int firstY, secondY, thirdY, fourthY;
    // 中间子控件位置
    private float mTop, mBottom, mLeft, mRight;
    // 动作（分为一变三，三变一，静止）
    private int action;
    // 一变三或三变一过程中直线位移变化量
    private int variation = 0;
    // 一变三或三变一过程中直线变圆时的变化量
    private int mOneToThreeFlag = 0;
    // 每个child的半径
    private float mChildR;

    private Paint mPaint;

    private DrawFilter mDrawFilter;

    private MenuButton mMenuBtn;

    private ListButton mListBtn;

    private OrderButton mOrderBtn;

    private TimingButton mTimingBtn;

    private final int PAINT_COLOR = ColorConfig.BUTTON_CUPCAKE_COLOR;

    // action三种状态
    public static final int ONE_TO_THREE = 1;
    public static final int THREE_TO_ONE = 2;
    public static final int STATIC = 3;
    // 组件间距
    public static final int BORDER_SPACE = 60;

    public static ButtonTypeHandler mButtonTypeHandler;

    public OneToThreeButtonGroup(Context context) {
        super(context);
    }

    public OneToThreeButtonGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        action = STATIC;
        mButtonTypeHandler = new ButtonTypeHandler(this);
        mPaint = new Paint();
        // 去除画笔锯齿
        mPaint.setAntiAlias(true);
        // 设置风格为实线
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(PAINT_COLOR);
        // 设置画笔粗细
        if (mWidth <= 800){
            mPaint.setStrokeWidth(3);
        }else {
            mPaint.setStrokeWidth(4.5f);
        }
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    // 当有手势滑动时，会重新调用该方法
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        /* 至多添加4个有效child，安排位置，形成一个倒T形 */
        for (int i = 0; i < count; i++){
            if (i == 0){
                getChildAt(i).layout(thirdX, thirdY, fourthX, fourthY);
                mMenuBtn = (MenuButton) getChildAt(i);
            }
            if (i == 1){
                getChildAt(i).layout(thirdX, firstY, fourthX, secondY);
                mListBtn = (ListButton) getChildAt(i);
            }
            if (i == 2){
                getChildAt(i).layout(firstX, thirdY, secondX, fourthY);
                mOrderBtn = (OrderButton) getChildAt(i);
            }
            if (i == 3){
                getChildAt(i).layout(fifthX, thirdY, sixthX, fourthY);
                mTimingBtn = (TimingButton) getChildAt(i);
                break;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        /* 子控件边界坐标 */
        firstX = BORDER_SPACE;
        secondX = mWidth / 3 - BORDER_SPACE;
        thirdX = mWidth / 3 + BORDER_SPACE;
        fourthX = mWidth / 3 * 2 - BORDER_SPACE;
        fifthX = mWidth / 3 * 2 + BORDER_SPACE;
        sixthX = mWidth - BORDER_SPACE;
        // 为了保证各Child控件中心到相邻Child控件中心距离一致，
        // 修改firstY，secondY，使第一层到第二层距离等于第一列到第二列
        firstY = mHeight / 4 * 3 - mWidth / 2 + BORDER_SPACE;
        secondY = mHeight / 4 * 3 - mWidth / 2 + BORDER_SPACE + (secondX - firstX);
        thirdY = mHeight / 2 + BORDER_SPACE;
        fourthY = mHeight - BORDER_SPACE;

        /* 中间child控件真实位置 */
        float rX = mWidth / 2;
        float rY = mHeight / 4 * 3;

        /* 兼容手机分辨率 */
        if (w <= 800){
            mLeft = rX - (Math.min(fourthY - thirdY, fourthX - thirdX) / 2 - 5);
            mRight = rX + Math.min(fourthY - thirdY, fourthX - thirdX) / 2 - 5;
            mTop = rY - (Math.min(fourthY - thirdY, fourthX - thirdX) / 2 - 5);
            mBottom = rY + Math.min(fourthY - thirdY, fourthX - thirdX) / 2 - 5;
            mChildR = (mBottom - mTop) / 2;
        } else {
            mLeft = rX - (Math.min(fourthY - thirdY, fourthX - thirdX) / 2 - 5) * 2 / 3;
            mRight = rX + (Math.min(fourthY - thirdY, fourthX - thirdX) / 2 - 5) * 2 / 3;
            mTop = rY - (Math.min(fourthY - thirdY, fourthX - thirdX) / 2 - 5) * 2 / 3;
            mBottom = rY + (Math.min(fourthY - thirdY, fourthX - thirdX) / 2 - 5) * 2 / 3;
            mChildR = (mBottom - mTop) / 2;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED){
            heightSize = mWidth / 3 * 2;
        }
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED){
            widthSize = mWidth;
        }
        setMeasuredDimension(widthSize, heightSize);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);
        switch (action){
            case STATIC:break;
            case ONE_TO_THREE:
                mMenuBtn.setVisibility(INVISIBLE);
                drawOneToThree(canvas);
                break;
            case THREE_TO_ONE:
                mMenuBtn.setVisibility(INVISIBLE);
                drawThreeToOne(canvas);
                break;
        }
    }

    /**
     * 一变三
     */
    private void drawOneToThree(Canvas canvas){
        if (variation < mWidth / 3 - mChildR){ // 第一个过程，三条线向左右上三个方向移动
            // 每次移动距离
            if (mWidth <= 800){
                variation += (mWidth / 3 - mChildR) / 5;
            }else {
                variation += (mWidth / 3 - mChildR) / 4;
            }
            drawStepOne(canvas, mLeft - variation, mTop, mRight - variation, mTop);
            drawStepOne(canvas, mLeft + variation, mBottom, mRight + variation, mBottom);
            drawStepOne(canvas, mRight, mTop - variation, mRight, mBottom - variation);
            postInvalidate();
        }else { // 第二过程，当线到达其他child位置，直线转化成圆弧过程
            if (mOneToThreeFlag <= 2 * mChildR){
                drawHorizontalStepTwo(canvas, mLeft - variation, mTop,
                        mRight - variation - mOneToThreeFlag, mTop, -90);
                drawHorizontalStepTwo(canvas, mLeft + variation + mOneToThreeFlag, mBottom,
                        mRight + variation, mBottom, 90);
                drawVerticalStepTwo(canvas, mRight, mTop - variation,
                        mRight, mBottom - variation - mOneToThreeFlag, 0);
                if (mWidth <= 720){
                    mOneToThreeFlag += (2 * mChildR) / 10;
                }else {
                    mOneToThreeFlag += (2 * mChildR) / 19;
                }

                postInvalidate();
            }else { // 形成圆，显示其他child控件
                mMenuBtn.changeStatus(MenuButton.ON_CLICK_MENU_BUTTON);
                mMenuBtn.setVisibility(VISIBLE);
                mListBtn.setVisibility(VISIBLE);
                mOrderBtn.setVisibility(VISIBLE);
                mTimingBtn.setVisibility(VISIBLE);
                action = STATIC;
            }
        }
    }

    /**
     * 三变一
     */
    private void drawThreeToOne(Canvas canvas){
        if (mOneToThreeFlag == 2 * mChildR){ // 隐藏控件
            mMenuBtn.changeStatus(MenuButton.MENU_BUTTON);
            mMenuBtn.setVisibility(INVISIBLE);
            mListBtn.setVisibility(INVISIBLE);
            mOrderBtn.setVisibility(INVISIBLE);
            mTimingBtn.setVisibility(INVISIBLE);
        }
        if (mOneToThreeFlag >= 0){ // 圆弧变直线
            drawHorizontalStepTwo(canvas, mLeft - variation, mTop,
                    mRight - variation - mOneToThreeFlag, mTop, -90);
            drawHorizontalStepTwo(canvas, mLeft + variation + mOneToThreeFlag, mBottom,
                    mRight + variation, mBottom, 90);
            drawVerticalStepTwo(canvas, mRight, mTop - variation,
                    mRight, mBottom - variation - mOneToThreeFlag, 0);
            if (mWidth <= 800){
                mOneToThreeFlag -= (2 * mChildR) / 10;
            }else {
                mOneToThreeFlag -= (2 * mChildR) / 19;
            }

            postInvalidate();
        }else { // 直线向右左下移动
            if (variation > mChildR){
                if (mWidth <= 800){
                    variation -= (mWidth / 3 - mChildR) / 5;
                }else {
                    variation -= (mWidth / 3 - mChildR) / 4;
                }

                drawStepOne(canvas, mLeft - variation, mTop, mRight - variation, mTop);
                drawStepOne(canvas, mLeft + variation, mBottom, mRight + variation, mBottom);
                drawStepOne(canvas, mRight, mTop - variation, mRight, mBottom - variation);
                postInvalidate();
            }else { // 到达中间child控件
                variation = 0;
                mMenuBtn.setVisibility(VISIBLE);
                action = STATIC;
            }
        }
    }

    /**
     * 线的平移
     * 改变需要移动的值
     */
    private void drawStepOne(Canvas canvas, float leftX, float leftY, float rightX, float rightY){
        canvas.drawLine(leftX, leftY, rightX, rightY, mPaint);
    }

    /**
     * 线变圆（横向）
     * 按照线的变化量（现有 / 原有）乘以360得到相应弧
     * 绘制弧
     */
    private void drawHorizontalStepTwo(Canvas canvas, float leftX, float leftY,
                                       float rightX, float rightY, int start){
        final RectF rectF;
        if (start >= 90 && start < 180){ // 向右
            rectF = new RectF(rightX - mChildR, mTop, rightX + mChildR, mBottom);
        }else { // 向左
            rectF = new RectF(leftX - mChildR, mTop, leftX + mChildR, mBottom);
        }
        canvas.drawArc(rectF, start, ((rightX - leftX) / (2 * mChildR) - 1) * 360, false, mPaint);
        canvas.drawLine(rightX, rightY, leftX, leftY, mPaint);
    }

    /**
     * 线变圆（纵向）
     * 按照线的变化量（现有 / 原有）乘以360得到相应弧
     * 绘制弧
     */
    private void drawVerticalStepTwo(Canvas canvas, float topX, float topY,
                                     float bottomX, float bottomY, int start){
        final RectF rectF;
        if (start >= 0 && start < 90){ // 向上
            rectF = new RectF(mLeft, topY - mChildR, mRight, topY + mChildR);
        }else { // 向下
            rectF = new RectF(mLeft, bottomY + mChildR, mRight, bottomY - mChildR);
        }
        canvas.drawArc(rectF, start, ((bottomY - topY) / (2 * mChildR) - 1) * 360, false, mPaint);
        canvas.drawLine(topX, topY, bottomX, bottomY, mPaint);
    }


    /**
     * 用于接收来自MusicActivity的消息
     * 在activity点击按钮后发送消息，告知是三变一还是一变三
     */
    public static class ButtonTypeHandler extends Handler {

        WeakReference<OneToThreeButtonGroup> mWeakReference;
        OneToThreeButtonGroup mBtnGroup;

        ButtonTypeHandler(OneToThreeButtonGroup btnGroup){
            mWeakReference = new WeakReference<>(btnGroup);
            mBtnGroup = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1){
                case OneToThreeButtonGroup.ONE_TO_THREE:
                    mBtnGroup.action = ONE_TO_THREE;
                    mBtnGroup.postInvalidate();
                    break;
                case OneToThreeButtonGroup.THREE_TO_ONE:
                    mBtnGroup.action = THREE_TO_ONE;
                    mBtnGroup.postInvalidate();
                    break;
            }
        }
    }
}
