package com.yoyo.smtpms.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;

/**
 * @author Administrator
 * @date 2018-10-18
 */
public class BaseUtil {
    /**
     * 全屏显示
     */
    public static void setFullSreen(Activity context) {
        WindowManager.LayoutParams params = context.getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        context.getWindow().setAttributes(params);
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        context.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); // Activity全屏显示，且状态栏被覆盖掉
    }
}
