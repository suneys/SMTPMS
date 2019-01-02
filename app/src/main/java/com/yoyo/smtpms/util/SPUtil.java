package com.yoyo.smtpms.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences工具类
 * Created by Administrator on 2017/4/21 0021.
 */

public class SPUtil {
    public static void saveString(Context context, String key, String value){
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = config.edit();
        edit.putString(key,value);
        edit.commit();
    }


    public static String getString(Context context, String key, String defaultVlaue){
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        return config.getString(key,defaultVlaue);
    }

    public static void saveInt(Context context, String key, int value){
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = config.edit();
        edit.putInt(key,value);
        edit.commit();
    }
    public static int getInt(Context context,String key, int defaultVlaue){
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        return config.getInt(key,defaultVlaue);
    }
}
