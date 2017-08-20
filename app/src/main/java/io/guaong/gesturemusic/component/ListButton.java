package io.guaong.gesturemusic.component;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Created by 关桐 on 2017/8/10.
 * 用于展示列表的按钮
 */

public class ListButton extends CircleButton {

    public ListButton(Context context) {
        super(context);
    }

    public ListButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawListButton(canvas);
    }

    @Override
    public void changeStatus(int status) {

    }

    @Override
    public int getNextStatus() {
        return 0;
    }

    /**
     * 画歌曲列表按钮
     */
    private void drawListButton(Canvas canvas){
        drawCircle(canvas);
        drawText(canvas, "list");
    }
}
