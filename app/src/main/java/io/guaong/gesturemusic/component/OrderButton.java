package io.guaong.gesturemusic.component;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;

/**
 * Created by 关桐 on 2017/8/10.
 *
 */

public class OrderButton extends CircleButton {

    // 顺序播放
    public static final int PLAY_ORDER = 1;
    // 随机播放
    public static final int PLAY_RANDOM = 2;
    // 单曲播放
    public static final int PLAY_SINGLE = 3;

    private int mOrderStatus;

    public static ChangeButtonStatusHandler changeButtonStatusHandler;

    public OrderButton(Context context) {
        super(context);
    }

    public OrderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mOrderStatus = PLAY_ORDER;
        changeButtonStatusHandler = new ChangeButtonStatusHandler(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOrderButton(canvas);
    }

    @Override
    public void changeStatus(int status) {
        setOrderStatus(status);
        postInvalidate();
    }

    @Override
    public int getNextStatus() {
        return getOrderStatus() % 3 + 1;
    }

    public void setOrderStatus(int status){
        mOrderStatus = status;
    }

    public int getOrderStatus(){
        return mOrderStatus;
    }

    /**
     * 画播放顺序按钮
     */
    private void drawOrderButton(Canvas canvas){
        drawCircle(canvas);
        switch (mOrderStatus){
            case PLAY_ORDER:drawText(canvas, "order");break;
            case PLAY_RANDOM:drawText(canvas, "random");break;
            case PLAY_SINGLE:drawText(canvas, "single");break;
        }
    }

    /**
     * 用于接收定时按钮，播放顺序按钮状态
     * 信息来自MusicActivity
     */
    public static class ChangeButtonStatusHandler extends Handler {

        private WeakReference<OrderButton> mWeakReference;
        private OrderButton mOrderButton;

        public ChangeButtonStatusHandler(OrderButton orderButton){
            mWeakReference = new WeakReference<>(orderButton);
            mOrderButton = mWeakReference.get();
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1){
                case PLAY_ORDER:mOrderButton.mOrderStatus = PLAY_ORDER;break;
                case PLAY_RANDOM:mOrderButton.mOrderStatus = PLAY_RANDOM;break;
                case PLAY_SINGLE:mOrderButton.mOrderStatus = PLAY_SINGLE;break;
            }
            mOrderButton.postInvalidate();
        }
    }
}
