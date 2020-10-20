package com.chinamobile.cache;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by liangzhongtai on 2018/9/17.
 */

public class CachesSP {
    public static void setSP(Context context, String key, int value) {
        SharedPreferences preferences=context.getSharedPreferences("caches", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void removeSP(Context context, String key) {
        SharedPreferences preferences=context.getSharedPreferences("caches", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public static int getSP(Context context,String key) {
        SharedPreferences preferences=context.getSharedPreferences("caches", Context.MODE_PRIVATE);
        return preferences.getInt(key,0);
    }

}
