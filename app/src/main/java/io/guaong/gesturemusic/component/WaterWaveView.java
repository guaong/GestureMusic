package io.guaong.gesturemusic.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

import io.guaong.gesturemusic.config.ColorConfig;
import io.guaong.gesturemusic.util.WindowUtil;

/**
 * Created by 关桐 on 2017/8/3.
 * 通过不断重绘，绘制N条竖线（N为控件的宽度，即填充）
 * 不断改变线的长度，实现波浪效果
 * 绘制两条，第二条注释掉了
 */

public class WaterWaveView extends View {

    // 重置动画
    public static final int RESET_ANIMATION = 1;
    // 改变动画（动画动或暂停）
    public static final int CHANGE_ANIMATION = 2;
    // 更新动画（动画高度变化）
    public static final int UPDATE_ANIMATION = 3;

    // 画笔颜色
    private final int WAVE_PAINT_COLOR = ColorConfig.WATER_CUPCAKE_COLOR;

    // 第一条水波移动速度
    private static final float TRANSLATE_X_SPEED_ONE = 7f;
    // 第二条水波移动速度
    // private static final float TRANSLATE_X_SPEED_TWO = 5f;

    // 当前控件高度
    private int mHeight;
    // 当前控件宽度
    private int mWidth;
    // 第一条的偏移量
    private int mFirstOffsetX;
    private float mFirstOffsetY = 0;
    // 第二条的偏移量
    // private int mSecondOffsetX;
    // private float mSecondOffsetY = 0;
    // 第一条偏移的大小
    private int mFirstOffsetSpeedX;
    // 第二条偏移的大小
    // private int mSecondOffsetSpeedX;

    // Y坐标组
    private float[] mPositionsY;
    // 改变的第一组Y坐标
    private float[] mResetFirstPositionsY;
    // 改变的第二组Y坐标
    // private float[] mResetSecondPositionsY;
    // 第一组线（四个为一条线）
    private float[] firstLines;
    // 第二组线
    // private float[] secondLines;

    // 是否改变坐标数据，即重绘内容是否改变
    private boolean isStop = true;
    // 音乐时长
    private long mMusicDuration;

    private Paint mPaint;
    private DrawFilter mDrawFilter;

    public static AnimationHandler animationHandler;

    public WaterWaveView(Context context) {
        super(context);
    }

    public WaterWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        animationHandler = new AnimationHandler(this);
        // 将dp转化为px，用于控制不同分辨率上移动速度基本一致
        mFirstOffsetSpeedX = WindowUtil.dipToPx(context, TRANSLATE_X_SPEED_ONE);
        // mSecondOffsetSpeedX = dipToPx(context, TRANSLATE_X_SPEED_TWO);
        // 初始绘制波纹的画笔
        mPaint = new Paint();
        // 去除画笔锯齿
        mPaint.setAntiAlias(true);
        // 设置风格为实线
        mPaint.setStyle(Paint.Style.FILL);
        // 设置画笔颜色
        mPaint.setColor(WAVE_PAINT_COLOR);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);
        // 重置所有Y坐标，第一次为初始化
        resetPositionsY();
        int j = 0;
        /* 将组成线的四个坐标存入数组 */
        for (int i = 0; i < mWidth; i++){
            // secondLines[j] = i;
            firstLines[j++] = i;
            // secondLines[j] = mHeight - mResetSecondPositionsY[i] - mSecondOffsetY;
            firstLines[j++] = mHeight - mResetFirstPositionsY[i] - mFirstOffsetY;
            // secondLines[j] = i;
            firstLines[j++] = i;
            // secondLines[j] = mHeight;
            firstLines[j++] = mHeight;
        }
        canvas.drawLines(firstLines, mPaint);
        //canvas.drawLines(secondLines, mPaint);
        /* cut点，将从该点将正弦函数分割成两部分 */
        mFirstOffsetX += mFirstOffsetSpeedX;
        // mSecondOffsetX += mSecondOffsetSpeedX;
        if (mFirstOffsetX >= mWidth) {
            mFirstOffsetX = 0;
        }
        // if (mSecondOffsetX >= mWidth) {
        //     mSecondOffsetX = 0;
        // }

        // 重绘
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mPositionsY = new float[mWidth];
        mResetFirstPositionsY = new float[mWidth];
        // mResetSecondPositionsY = new float[mWidth];
        firstLines = new float[mWidth * 4];
        // secondLines = new float[mWidth * 4];

        // 将周期定为view总宽度
        final float mCycle = (float) (2 * Math.PI / mWidth);
        final float OFFSET_Y = 0;
        final float STRETCH_FACTOR_A = 20f;
        for (int i = 0; i < mWidth; i++){
            // Asin（ωx+φ）+b
            mPositionsY[i] = (float)(STRETCH_FACTOR_A * Math.sin(mCycle * i) + OFFSET_Y);
        }
    }

    /**
     * 重置Y坐标
     * 将正弦函数分割为两部分，分别得到原始正弦数据的相应部分
     * 由于是一个完整的2π周期，因此是连续不断地
     */
    private void resetPositionsY(){
        if (!isStop){
            int firstIntervalY = mPositionsY.length - mFirstOffsetX;
            System.arraycopy(mPositionsY, mFirstOffsetX, mResetFirstPositionsY, 0, firstIntervalY);
            System.arraycopy(mPositionsY, 0, mResetFirstPositionsY, firstIntervalY, mFirstOffsetX);
            // int secondIntervalY = mPositionsY.length - mSecondOffsetX;
            // System.arraycopy(mPositionsY, mSecondOffsetX, mResetSecondPositionsY, 0, secondIntervalY);
            // System.arraycopy(mPositionsY, 0, mResetSecondPositionsY, secondIntervalY, mSecondOffsetX);
        }
    }

    /**
     * 用于接收动画状态
     * 信息来自MusicPlayService
     * 用于根据当前播放时间改变水波高度
     */
    public static class AnimationHandler extends Handler{

        private WeakReference<WaterWaveView> mWeakReference;
        private WaterWaveView mWaterWaveView;

        AnimationHandler(WaterWaveView waterWaveView){
            mWeakReference = new WeakReference<>(waterWaveView);
            mWaterWaveView = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
           switch (msg.arg1){
               case RESET_ANIMATION:
                   resetAnimation(msg);
                   break;
               case CHANGE_ANIMATION:
                   changeAnimation(msg);
                   break;
               case UPDATE_ANIMATION:
                   updateAnimation(msg);
                   break;
           }
        }

        private void resetAnimation(Message msg){
            mWaterWaveView.mMusicDuration = (long)msg.obj;

        }

        private void changeAnimation(Message msg){
            mWaterWaveView.isStop = (boolean)msg.obj;
        }

        private void updateAnimation(Message msg){
            int current = (int)msg.obj;
            mWaterWaveView.mFirstOffsetY =
                    (float) mWaterWaveView.mHeight / mWaterWaveView.mMusicDuration * current;
            // mWaterWaveView.mSecondOffsetY =
            //        (float) mWaterWaveView.mHeight / mWaterWaveView.mMusicDuration *current;
        }
    }

}
