package com.yoyo.smtpms;

import android.app.Application;
import android.content.Intent;
import com.yoyo.smtpms.util.SPUtil;
import org.xutils.x;

/**
 * @author Administrator
 * @date 2018-11-25
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
    }
}
