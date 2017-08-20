package io.guaong.gesturemusic.component;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Created by 关桐 on 2017/8/10.
 *
 */

public class TimingButton extends CircleButton {

    // 0
    public static final int ZERO = 1;
    // 半小时
    public static final int HALF_HOUR = 2;
    // 一小时
    public static final int AN_HOUR = 3;
    // 一个半小时
    public static final int ONE_AND_HALF_AN_HOUR = 4;
    // 两小时
    public static final int TWO_HOURS = 5;

    private int mTimingStatus;

    public TimingButton(Context context) {
        super(context);
    }

    public TimingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTimingStatus = ZERO;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTimingButton(canvas);
    }

    @Override
    public void changeStatus(int status) {
        setTimingStatus(status);
        postInvalidate();
    }

    @Override
    public int getNextStatus() {
        return getTimingStatus() % 5 + 1;
    }

    public void setTimingStatus(int status){
        mTimingStatus = status;
    }

    public int getTimingStatus(){
        return mTimingStatus;
    }


    /**
     * 画定时按钮
     */
    private void drawTimingButton(Canvas canvas){
        drawCircle(canvas);
        switch (mTimingStatus){
            case ZERO:drawText(canvas, "timing");break;
            case HALF_HOUR:drawText(canvas, "0.5h");break;
            case AN_HOUR:drawText(canvas, "1h");break;
            case ONE_AND_HALF_AN_HOUR:drawText(canvas, "1.5h");break;
            case TWO_HOURS:drawText(canvas, "2h");break;
        }
    }
}
