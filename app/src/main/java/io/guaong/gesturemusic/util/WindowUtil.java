package io.guaong.gesturemusic.util;

import android.content.Context;

/**
 * Created by 关桐 on 2017/8/15.
 *
 */

public class WindowUtil {

    /**
     * dp转px
     * @param context Context
     * @param dpValue dp值
     * @return px值
     */
    public static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static float pxToSp(Context context, float pxValue){
        return (pxValue / context.getResources().getDisplayMetrics().scaledDensity);
    }

}
