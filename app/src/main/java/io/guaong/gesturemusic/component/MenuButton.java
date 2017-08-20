package io.guaong.gesturemusic.component;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Created by 关桐 on 2017/8/10.
 *
 */

public class MenuButton extends CircleButton {

    private int mMenuStatus;
    // 菜单按钮
    public static final int MENU_BUTTON = 1;
    // 关闭菜单按钮
    public static final int ON_CLICK_MENU_BUTTON = 2;

    public MenuButton(Context context) {
        super(context);
    }

    public MenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMenuStatus = MENU_BUTTON;
    }

    public void setMenuStatus(int status){
        mMenuStatus = status;
    }

    public int getMenuStatus(){
        return mMenuStatus;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mMenuStatus){
            case MENU_BUTTON:drawMenuButton(canvas);break;
            case ON_CLICK_MENU_BUTTON:drawClickedMenuBtn(canvas);break;
        }
    }

    @Override
    public void changeStatus(int status) {
        setMenuStatus(status);
        postInvalidate();
    }

    @Override
    public int getNextStatus() {
        return getMenuStatus() % 2 + 1;
    }

    /**
     * 画菜单按钮
     */
    private void drawMenuButton(Canvas canvas){
        drawCircle(canvas);
        drawText(canvas, "menu");
    }

}
