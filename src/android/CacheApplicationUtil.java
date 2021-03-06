package com.chinamobile.cache;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by liangzhongtai on 2020/3/12.
 */

public class CacheApplicationUtil {
    private static final String TAG="OnFrontUtil";
    boolean isResumed = false;
    boolean isPaused = false;
    boolean isFront = false;
    int count=0;

    //因为需要统计Activity，所以用单例实现
    private volatile static CacheApplicationUtil instance;
    private CacheApplicationUtil(Application app, OnFrontCallback callback){
        registerOnFront(app,callback);
    }

    //只需要在初始化时被调用一次，所以修改了单例写法，不再返回实例，只完成注册监听
    public static void listenOnFront(Application app,OnFrontCallback callback){
        if(instance==null){
            synchronized (CacheApplicationUtil.class){
                if(instance==null){
                    instance=new CacheApplicationUtil(app,callback);
                }
            }
        }
    }

    //向调用者反馈回到前台事件
    public interface OnFrontCallback{
        void onFront();
    }

    //通过监听和统计Activity生命周期，判断App是否来到前台显示
    private void registerOnFront(Application app,final OnFrontCallback callback){
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                isResumed = false;
            }

            @Override
            public void onActivityStarted(Activity activity) {
                count++;

                Log.d(TAG," started "+count);
                if(count==1){
                    Log.d(TAG," is front");
                    callback.onFront();
                    isFront = true;

                }

            }

            @Override
            public void onActivityResumed(Activity activity) {
                //1.有执行过onPause
                //2.不是重复事件
                //3.不是第一次进入onResume
                if(isPaused&&!isFront&&isResumed){
                    if(count==1){
                        Log.d(TAG," is front");
                        callback.onFront();
                    }
                }
                isPaused = false;
                isResumed = true;
                isFront = false;
            }


            @Override
            public void onActivityPaused(Activity activity) {
                isPaused = true;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                count--;

                Log.d(TAG," stopped "+count);
                if(count==0){
                    Log.d(TAG," is background");

                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}
