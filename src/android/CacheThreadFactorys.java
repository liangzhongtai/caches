package com.chinamobile.cache;

/**
 * Created by liangzhongtai on 2018/8/29.
 */

public class CacheThreadFactorys {
    static CacheThreadProxy mNormalThreadPoolProxy;
    /** * 得到普通线程池代理对象mNormalThreadPoolProxy */
    public static CacheThreadProxy getNormalThreadPoolProxy() {
        if (mNormalThreadPoolProxy == null) {
            synchronized (CacheThreadProxy.class) {
                if (mNormalThreadPoolProxy == null) {
                    mNormalThreadPoolProxy = new CacheThreadProxy(5, 5);
                }
            }
        } return mNormalThreadPoolProxy;
    }

    /**
     * 清空线程池
     * */
    public static void clearProxy() {
        if (mNormalThreadPoolProxy == null) {
            synchronized (CacheThreadProxy.class) {
                if (mNormalThreadPoolProxy != null) {
                    mNormalThreadPoolProxy.clear();
                }
            }
        }
    }
}
