package com.chinamobile.cache;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;


import com.chinamobile.cst.CSTestUtil;
import com.chinamobile.gdwy.MainActivity;
import com.chinamobile.phone.PhonesUtil;
import com.chinamobile.upload.FileProvider;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.crosswalk.engine.XWalkCordovaUiClient;
import org.crosswalk.engine.XWalkCordovaView;
import org.crosswalk.engine.XWalkWebViewEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkView;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;


/**
 * Created by liangzhongtai on 2018/5/21.
 */

public class Caches extends CordovaPlugin {
    public final static String TAG = "Caches_Plugin";
    public final static int RESULTCODE_PERMISSION = 100;
    public final static int RESULTCODE_PHONE_PROVIDER = 200;
    public final static int RESULTCODE_FILE = 300;

    // 存储
    public final static int SAVE               = 0;
    // 读取
    public final static int READ               = 1;
    // 移除
    public final static int REMOVE             = 2;
    // 读取文件路径
    public final static int FILE_PATH          = 3;
    // 打开文件夹
    public final static int FILE_OPEN          = 4;
    // 分享文件到微信/QQ
    public final static int FILE_SHARE         = 5;
    // 分享多个文件到微信/QQ
    public final static int FILES_SHARE        = 6;
    // 发送邮件
    public final static int SEND_MAIL          = 7;
    // 保存base64图片
    public final static int SAVE_BASE64_IMG    = 8;
    // 打开微信
    public final static int OPEN_WEIXIN        = 9;
    // 打开QQ
    public final static int OPEN_QQ            = 10;
    // 检查权限
    public final static int CHECK_PERMISSION   = 12;
    // 检查文件是否存在
    public final static int CHECK_FILE_EXITS   = 13;
    // 检查篡改
    public final static int CHECK_TAMPER       = 14;


    public final static int NO_DATA_TYPE= -1;
    public final static int NULL        = 0;
    public final static int STRING      = 1;
    public final static int NUMBER      = 2;
    public final static int BOOLEAN     = 3;
    public final static int ARRAY       = 4;
    public final static int OBJECT      = 5;

    // 缓存完成
    public final static int CACHE_FINISH               = 0;
    // 缓存的key为null
    public final static int CACHE_KEY_NULL             = 1;
    // 缓存的value为null
    public final static int CACHE_VALUE_NULL           = 2;
    // 缓存的数据格式不支持
    public final static int CACHE_VALUE_FORMAT_ERROR   = 3;
    // 缓存出错
    public final static int CACHE_ERROR                = 4;
    // 缓存读取解析异常
    public final static int CACHE_VALUE_ANALYSIS_ERROR = 5;
    // 文件路径存在
    public final static int FILE_PATH_SUC              = 6;
    // 文件路径不存在
    public final static int FILE_PATH_FAI              = 7;
    // 邮件发送失败
    public final static int MAIL_SEND_FAILE            = 8;
    // 邮件附件不存在
    public final static int FILE_UNEXIST               = 9;
    // 邮件发送成功
    public final static int MAIL_SEND_SUCCESS          = 10;
    // 邮件地址/密码错误
    public final static int MAIL_ADDRESS_OR_PASS_ERROR = 11;
    // 无权限
    public final static int NO_PERMISSION              = 12;
    // 有权限
    public final static int HAS_PERMISSION             = 13;


    // 默认操作
    public final static int DEFAULT        = 0;
    // JSON转CSV存储
    public final static int JSON2CSV       = 1;
    // CSV转JSON返回
    public final static int CSV2JSON       = 2;
    // 微信
    public final static int WEIXIN         = 1;
    // QQ
    public final static int QQ             = 2;

    public CordovaInterface cordova;
    private CallbackContext callbackContext;
    // 插件动作：0，存储；1，读取；2，移除
    public int permissionCacheType;
    // 缓存操作的key
    public String permissionKey;
    // 缓存的value,支持String,int,float,double,bool,long,JSONObject,JSONArray
    public Object permissionValue;
    // 操作类型
    public int permissionActionType;
    // 文件夹名
    public String permissionDir;
    // 是否使用默认路径
    public boolean permissionUseDefaultPath;
    // 数据类型
    public int permissionDataType;
    // 接收的参数
    public JSONArray permissionArgs;
    // 存在不再询问的权限
    public boolean hasDeniedAndNeverAskAgain;
    // 是否需要加密和解密
    public boolean permissionNeedEncodeAndDecode;
    // 防止篡改
    public boolean permissionNeedDefenseTamper;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
        this.webView = webView;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG,"执行方法Caches");
        Log.d(TAG,"Caches传递的args="+args);
        this.callbackContext = callbackContext;
        if ("coolMethod".equals(action)) {
            int cacheType = args.getInt(0);
            String key = args.getString(1);
            Object value = null;
            int actionType = DEFAULT;
            String dir = null;
            boolean useDefaultPath = true;
            int dataType = -1;
            boolean needEncodeAndDecode = false;
            boolean needDefenseTamper = false;
            Log.d(TAG, cacheType == READ ? "读取" : "保存");
            if (cacheType == CHECK_FILE_EXITS) {
                File file = new File(Environment.getExternalStorageDirectory() + key);
                sendCachesMessage(callbackContext,
                        formatJSONArray(cacheType, CACHE_FINISH, key, file.exists(), actionType),false);
                return true;
            }
            if (cacheType != SEND_MAIL) {
                if (args.length() > 2) {
                    value = args.get(2);
                    Log.d(TAG, "存储的value=" + value);
                }

                if (args.length() > 3) {
                    actionType = args.getInt(3);
                }

                if (args.length() > 4) {
                    dir = args.getString(4);
                }

                if (args.length() > 5) {
                    useDefaultPath = args.getBoolean(5);
                }

                if (args.length() > 6) {
                    dataType = args.getInt(6);
                }
                if (args.length() > 7) {
                    needEncodeAndDecode = args.getBoolean(7);
                }
                if (args.length() > 8) {
                    needDefenseTamper = args.getBoolean(8);
                }
            }
            //权限
            try {
                if (!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    !PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "缺少权限------");
                    permissionCacheType = cacheType;
                    permissionKey = key;
                    permissionValue = value;
                    permissionActionType = actionType;
                    permissionDir = dir;
                    permissionUseDefaultPath = useDefaultPath;
                    permissionDataType = dataType;
                    permissionNeedEncodeAndDecode = needEncodeAndDecode;
                    permissionNeedDefenseTamper = needDefenseTamper;
                    permissionArgs = args;
                    if (cacheType == CHECK_PERMISSION) {
                        sendCachesMessage(callbackContext,
                                formatJSONArray(cacheType, NO_PERMISSION, key, null, actionType), false);
                    }
                    PermissionHelper.requestPermissions(this, RESULTCODE_PERMISSION, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    });

                } else if (cacheType == CHECK_PERMISSION) {
                    sendCachesMessage(callbackContext,
                            formatJSONArray(cacheType, HAS_PERMISSION, key, null, actionType), false);
                } else {
                    Log.d(TAG, "已授予权限------");
                    startWork(callbackContext, cacheType, key, value, actionType, dir,
                            useDefaultPath, dataType, needEncodeAndDecode, needDefenseTamper, args);
                }
            } catch (Exception e) {
                //权限异常
                callbackContext.error("缓存功能异常");
                return true;
            }

            return true;
        }
        return super.execute(action, args, callbackContext);
    }

    /**
     * 关闭弹窗提示
     * */
    public static void closeAndroidPDialog() {
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步cookie
     * */
    public static void syncCookies(CordovaWebView appView, String launchUrl) {
        XWalkCordovaView wv = (XWalkCordovaView) appView.getView();
        wv.setUIClient(new XWalkCordovaUiClient((XWalkWebViewEngine) appView.getEngine()) {
            @Override
            public void onPageLoadStarted(XWalkView view, String url) {
                super.onPageLoadStarted(view, url);
                Log.d(TAG, "onPageLoadStarted:" + url);
                syncXWalkViewCookies(launchUrl);
            }

            @Override
            public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
                super.onPageLoadStopped(view, url, status);
                Log.d(TAG,  "onPageLoadStopped:" + url);
            }
        });
    }

    public static void syncXWalkViewCookies(String launchUrl){
        XWalkCookieManager cookieManager = new XWalkCookieManager();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptFileSchemeCookies(true);
        String cookies = cookieManager.getCookie(launchUrl);
        // 如果没有特殊需求，这里只需要将session id以"key=value"形式作为cookie即可
        // cookieManager.setCookie(SysParam.shoppingMall, cookie);
        Log.d(TAG,  "cookies:" + cookies);
        cookieManager.flushCookieStore();
    }

    @Override
    public Bundle onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        Log.d(TAG, "返回权限------");
        int count = 0;
        hasDeniedAndNeverAskAgain = false;
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.shouldShowRequestPermissionRationale(cordova.getActivity(), permissions[count])) {
                hasDeniedAndNeverAskAgain = true;
            }
            if (r == PackageManager.PERMISSION_DENIED && permissionCacheType != CHECK_PERMISSION) {
                callbackContext.error("缺少读写外部存储的权限,无法缓存数据");
                Log.d(TAG, "缺少读写外部存储的权限,无法缓存数据");
                return;
            }
            count++;
        }
        if (permissionCacheType == CHECK_PERMISSION) {
            return;
        }
        switch (requestCode) {
            case RESULTCODE_PERMISSION:
                Log.d(TAG,"权限设置页面");
                startWork(callbackContext, permissionCacheType, permissionKey, permissionValue,
                       permissionActionType, permissionDir, permissionUseDefaultPath,
                       permissionDataType, permissionNeedEncodeAndDecode, permissionNeedDefenseTamper,
                       permissionArgs);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RESULTCODE_PHONE_PROVIDER) {
            startWork(callbackContext, permissionCacheType, permissionKey, permissionValue,
                    permissionActionType, permissionDir, permissionUseDefaultPath,
                    permissionDataType, permissionNeedEncodeAndDecode, permissionNeedDefenseTamper,
                    permissionArgs);
        }
        Log.d(TAG+"---","文件路径requestCode="+requestCode);
        Log.d(TAG+"---","文件路径resultCode="+resultCode);
        // 选择了文件
        if (requestCode == RESULTCODE_FILE && resultCode == Activity.RESULT_OK) {
            String path;
            Uri uri = intent.getData();
            // 使用第三方应用打开
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                path = uri.getPath();
                // 4.4以后
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                path = CSTestUtil.getPath(cordova.getActivity().getBaseContext(), uri);
                // 4.4以下下系统调用方法
            } else {
                path = CSTestUtil.getRealPathFromURI(cordova.getActivity(),uri);

            }
            Log.d(TAG+"---","文件路径="+path);
            if (path!=null) {
                CacheFile file = CacheFile.newInstance(path);
                sendCachesMessage(callbackContext,
                    formatJSONArray(permissionCacheType, FILE_PATH_SUC, permissionKey,
                            file.formatJson(), permissionActionType),
                    false);
            } else {
                sendCachesMessage(callbackContext,
                    formatJSONArray(permissionCacheType, FILE_PATH_FAI, permissionKey,
                            "" , permissionActionType), true);
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        CacheThreadFactorys.clearProxy();
    }

    private void startWork(CallbackContext callback, int cacheType, String key, Object value,
                           int actionType, String dir, boolean useDefaultPath, int dataType,
                           boolean needEncodeAndDecode, boolean needDefenseTamper, JSONArray args) {
        // 读写操作
        if ((cacheType == READ && actionType == CSV2JSON) ||
            cacheType == FILE_PATH) {
            String path = Environment.getExternalStorageDirectory() +
                    (TextUtils.isEmpty(dir) ? "/" : "/"+dir+"/")+key;
            File file = new File(path);
            if (file.exists() && cacheType == FILE_PATH) {
                sendCachesMessage(callback,
                        formatJSONArray(cacheType, FILE_PATH_SUC, key, path, actionType),false);
                return;
            } else if (!file.exists()) {
                sendCachesMessage(callback,
                        formatJSONArray(cacheType, FILE_PATH_FAI, key, path, actionType),true);
                return;
            }
        }

        // 分享文件
        if (cacheType == FILE_SHARE && key != null) {
            String fileName = key;
            String dirReally = Environment.getExternalStorageDirectory()
                    + (TextUtils.isEmpty(dir) ? "" : ("/" + dir));
            File file = new File(dirReally, fileName);
            if (!file.exists()) {
                sendCachesMessage(callback,
                        formatJSONArray(cacheType, FILE_PATH_FAI, key, file.getAbsolutePath(), actionType),true);
                return;
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            // 判断版本大于等于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
            }
            // 微信
            if (actionType == WEIXIN) {
                intent.setPackage("com.tencent.mm");
            // QQ
            } else if (actionType == QQ) {
                intent.setPackage("com.tencent.mobileqq");

            }
            Uri uri = FileProvider.getUriForFile(cordova.getContext(),
                    cordova.getContext().getApplicationContext().getPackageName() + ".provider", file);
            String type = CsvUtil.getMIMETypeString(file);
            intent.setType(type);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            cordova.getActivity().startActivity(Intent.createChooser(intent,
                    actionType == WEIXIN ? "发送到微信" : (actionType == QQ ? "发送到QQ" : "发送")));
            return;
        }

        // 保存64字符串为图片
        if (cacheType == SAVE_BASE64_IMG) {
            String path =
                    CacheUtil.base64Str2Image(cordova.getActivity(), (String)value, key);
            callback.success(path);
            return;
        }

        // 打开微信
        if (cacheType == OPEN_WEIXIN) {
            Intent intent = cordova.getContext().getPackageManager().
                    getLaunchIntentForPackage("com.tencent.mm");
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            cordova.getActivity().startActivity(intent);
            return;
        }

        // 打开QQ
        if (cacheType == OPEN_QQ) {
            Intent intent = cordova.getContext().getPackageManager().
                    getLaunchIntentForPackage("com.tencent.mobileqq");
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            cordova.getActivity().startActivity(intent);
            return;
        }

        // 检查是否被篡改
        if (cacheType == CHECK_TAMPER) {
            // 读取文件的摘要
            String[] strs = CacheUtil.readContentAndSha1s(key, needEncodeAndDecode, true);
            String sha1Old = strs[1];
            // 生成文件的摘要
            String sha1File = CacheUtil.checkSHA1(strs[0]);
            Log.d(TAG, "sha1Old" + sha1Old);
            Log.d(TAG, "sha1File=" + sha1File);
            boolean hasTampered = !sha1Old.equals(sha1File);
            sendCachesMessage(callbackContext,
                    formatJSONArray(cacheType, CACHE_FINISH, key, hasTampered, actionType),false);
            return;
        }

        // 分享多个文件
        if (cacheType == FILES_SHARE && key != null) {
            String[] fileNames = key.split(",");
            String[] dirs = TextUtils.isEmpty(dir) ? null : (dir.contains(",") ? new String[]{ dir } : dir.split(","));
            ArrayList<Uri> uris = new ArrayList();
            for (int i = 0; i < fileNames.length; i++) {
                dir = dirs == null ? "" : (dirs.length == 1 ? dirs[0] : dir);
                String dirReally = Environment.getExternalStorageDirectory()
                        + (TextUtils.isEmpty(dir) ? "" : ("/" + dir));
                File file = new File(dirReally, fileNames[i]);
                if (!file.exists()) {
                    sendCachesMessage(callback, formatJSONArray(cacheType, FILE_PATH_FAI, key,
                            file.getAbsolutePath(), actionType),true);
                    return;
                }
                Uri uri = FileProvider.getUriForFile(cordova.getContext(),
                        cordova.getContext().getApplicationContext().getPackageName() + ".provider", file);
                uris.add(uri);
            }
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            // 判断版本大于等于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
            }
            // 微信
            if (actionType == WEIXIN) {
                intent.setPackage("com.tencent.mm");
                // QQ
            } else if (actionType == QQ) {
                intent.setPackage("com.tencent.mobileqq");

            }
            //String type = CsvUtil.getMIMETypeString(file);
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            cordova.getActivity().startActivity(Intent.createChooser(intent,
                    actionType == WEIXIN ? "发送到微信" : (actionType == QQ ? "发送到QQ" : "发送")));
            return;
        }

        if (cacheType == SEND_MAIL) {
            try {
                String fileName = args.getString(1);
                String address  = args.getString(2);
                String pass     = args.getString(3);
                String ip       = args.getString(4);
                int port        = args.getInt(5);
                String receive  = args.getString(6);
                String title    = args.getString(7);
                String content  = args.getString(8);
                String carbon   = args.getString(9);
                CacheUtil.HOST = ip;
                CacheUtil.PORT = port + "";
                CacheUtil.FROM_ADD = address;
                CacheUtil.FROM_PSW = pass;
                CacheUtil.MAIL_TITLE = title;
                CacheUtil.MAIL_CONTENT = content;
                String[] receives = new String[]{ receive };
                if (receive.contains(",")) {
                    receives = receive.split(",");
                }
                String[] carbons = new String[]{ carbon };
                // Log.d(TAG, "carbon=" + carbon);
                if (TextUtils.isEmpty(carbon) || carbon.equals("null")) {
                    carbons = new String[]{};
                } else if(carbon.contains(",")) {
                    carbons = carbon.split(",");
                }
                File file = new File(Environment.getExternalStorageDirectory() + "", fileName);
                if (!file.exists()) {
                    sendCachesMessage(callback,
                            formatJSONArray(cacheType, FILE_UNEXIST, key, value, actionType),true);
                    return;
                }


                if (address.contains("139.com")) {
                    CacheUtil.sendSystemMail(this, file, receives, carbons);
                    sendCachesMessage(callback,
                            formatJSONArray(cacheType, MAIL_SEND_SUCCESS, key, value, actionType),false);
                } else {
                    CacheUtil.sendMailWithFile(file,
                        receives, carbons, new String[]{},
                        new MailListener() {
                            @Override
                            public void finish(int result) {
                                Message message = new Message();
                                message.what = result == 0 ? HANDLER_MAIL_SUCCESS :
                                        (result == 1 ? HANDLER_MAIL_ADDRESS_PASS_ERROR : HANDLER_MAIL_FAILE);
                                message.obj = new Object[]{cacheType, fileName, "", actionType, callback};
                                mHandler.sendMessage(message);
                            }
                        });
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendCachesMessage(callback,
                        formatJSONArray(cacheType, MAIL_SEND_FAILE, key, value, actionType),true);
            }
            return;

        }

        if (key == null) {
            sendCachesMessage(callback,
                    formatJSONArray(cacheType, CACHE_KEY_NULL, key, value, actionType),true);
            return;
        }
        if (cacheType == SAVE && value == null) {
            Log.d(TAG,"保存value=" + value);
            sendCachesMessage(callback,
                    formatJSONArray(cacheType, CACHE_VALUE_NULL, key, value, actionType),true);
            return;
        }
        if (cacheType == FILE_OPEN) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            // 设置类型，这里是任意类型，任意后缀的可以这样写。
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            cordova.setActivityResultCallback(this);
            cordova.getActivity().startActivityForResult(intent, RESULTCODE_FILE);
            return;
        }
        String finalDir = dir;
        CacheThreadFactorys.getNormalThreadPoolProxy().execute(() -> {
            int threadCacheType = cacheType;
            Message msg   = new Message();
            msg.obj       = new Object[]{threadCacheType, key, value, actionType, callback};
           Context context = Caches.this.cordova.getContext();
            try {
                //存储
                if (threadCacheType == Caches.SAVE) {
                    msg.what = HANDLER_CACHE_SAVE;
                    //JSON转CSV存储
                    if (actionType == JSON2CSV) {
                        try {
                            String path = CsvUtil.writeCsv(key, finalDir, (JSONArray) value);
                            msg.obj = new Object[]{threadCacheType, key, value, actionType, callback, path};
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                            msg.what = HANDLER_CACHE_FORMAT_ERROR;
                            mHandler.sendMessage(msg);
                            return;
                        }
                        //普通操作
                    } else if(actionType == DEFAULT) {
                        Log.d(TAG, "----------------------useDefaultPath=" + useDefaultPath);
                        if (useDefaultPath) {
                            if (value instanceof String) {
                                ACache.get(context).put(key, (String) value);
                                CachesSP.setSP(context, key, 1);
                            } else if (value instanceof Integer) {
                                ACache.get(context).put(key, value + "");
                                CachesSP.setSP(context, key, 1);
                            } else if (value instanceof Boolean) {
                                ACache.get(context).put(key, value + "");
                                CachesSP.setSP(context, key, 2);
                            } else if (value instanceof Float) {
                                ACache.get(context).put(key, value + "");
                                CachesSP.setSP(context, key, 3);
                            } else if (value instanceof Double) {
                                ACache.get(context).put(key, value + "");
                                CachesSP.setSP(context, key, 4);
                            } else if (value instanceof Long) {
                                ACache.get(context).put(key, value + "");
                                CachesSP.setSP(context, key, 5);
                            } else if (value instanceof JSONArray) {
                                ACache.get(context).put(key, (JSONArray) value);
                                CachesSP.setSP(context, key, 6);
                            } else if (value instanceof JSONObject) {
                                ACache.get(context).put(key, (JSONObject) value);
                                CachesSP.setSP(context, key, 7);
                            } else {
                                msg.what = HANDLER_CACHE_FORMAT_ERROR;
                            }
                        } else {
                            if (value instanceof String) {
                                CacheUtil.writeString(key, null, (String) value);
                                CachesSP.setSP(context, key, 1);
                            } else if (value instanceof Integer) {
                                CacheUtil.writeString(key, null, value + "");
                                CachesSP.setSP(context, key, 1);
                            } else if (value instanceof Boolean) {
                                CacheUtil.writeString(key, null, value + "");
                                CachesSP.setSP(context, key, 2);
                            } else if (value instanceof Float) {
                                CacheUtil.writeString(key, null, value + "");
                                CachesSP.setSP(context, key, 3);
                            } else if (value instanceof Double) {
                                CacheUtil.writeString(key, null, value + "");
                                CachesSP.setSP(context, key, 4);
                            } else if (value instanceof Long) {
                                CacheUtil.writeString(key, null, value + "");
                                CachesSP.setSP(context, key, 5);
                            } else if (value instanceof JSONArray) {
                                CacheUtil.writeArray(key, null, needEncodeAndDecode,
                                        needDefenseTamper, (JSONArray) value);
                                CachesSP.setSP(context, key, 6);
                            } else if (value instanceof JSONObject) {
                                CacheUtil.writeObject(key, null, needEncodeAndDecode,
                                        needDefenseTamper, (JSONObject) value);
                                CachesSP.setSP(context, key, 7);
                            } else {
                                msg.what = HANDLER_CACHE_FORMAT_ERROR;
                            }
                        }
                    }
                // 读取
                } else if (threadCacheType == Caches.READ) {
                    msg.what = HANDLER_CACHE_READ;
                    //CSV文件转JSON返回
                    if (actionType == CSV2JSON) {
                        try {
                            JSONArray array = CsvUtil.readCsv(key);
                            msg.obj = new Object[]{threadCacheType, key, array, actionType, callback};
                        } catch (Throwable throwable) {
                            //throwable.printStackTrace();
                            msg.what = HANDLER_CACHE_ANALYSIS_ERROR;
                            mHandler.sendMessage(msg);
                            return;
                        }
                        //默认操作
                    } else if (actionType == DEFAULT) {
                        try {
                            int type = CachesSP.getSP(context, key);
                            Object obj = null;
                            if (dataType != NO_DATA_TYPE) {
                                if (useDefaultPath) {
                                    if (dataType == ARRAY) {
                                        obj = ACache.get(context).getAsJSONArray(key);
                                    } else if (dataType == OBJECT) {
                                        obj = ACache.get(context).getAsJSONObject(key);
                                    } else {
                                        obj = ACache.get(context).getAsString(key);
                                    }
                                } else {
                                    if (dataType == ARRAY) {
                                        obj = CacheUtil.readArray(key, null,
                                                needEncodeAndDecode, needDefenseTamper);
                                    } else if (dataType == OBJECT) {
                                        obj = CacheUtil.readObject(key, null,
                                                needEncodeAndDecode, needDefenseTamper);
                                    } else {
                                        obj = CacheUtil.readString(key, null);
                                    }
                                }
                            } else {
                                if (useDefaultPath) {
                                    if (type == 0) {
                                        obj = ACache.get(context).getAsJSONObject(key);
                                    } else if (type < 6) {
                                        obj = ACache.get(context).getAsString(key);
                                    } else if (type == 6) {
                                        obj = ACache.get(context).getAsJSONArray(key);
                                    } else if (type == 7) {
                                        obj = ACache.get(context).getAsJSONObject(key);
                                    }
                                } else {
                                    if (type == 0) {
                                        obj = CacheUtil.readObject(key, null,
                                                needEncodeAndDecode, needDefenseTamper);
                                    } else if (type < 6) {
                                        obj = CacheUtil.readString(key, null);
                                    } else if (type == 6) {
                                        obj = CacheUtil.readArray(key, null,
                                                needEncodeAndDecode, needDefenseTamper);
                                    } else if (type == 7) {
                                        obj = CacheUtil.readObject(key, null,
                                                needEncodeAndDecode, needDefenseTamper);
                                    }
                                }
                            }
                            msg.obj = new Object[]{ threadCacheType, key, obj, actionType, callback};
                            Log.d(TAG,"读取msg.obj=" + msg.obj);
                        } catch (Exception e1) {
                            Log.d(TAG,"读取jsonobject异常");
                            msg.what = HANDLER_CACHE_ANALYSIS_ERROR;
                            mHandler.sendMessage(msg);
                            return;
                        }
                    }
                } else if (threadCacheType == Caches.REMOVE) {
                    msg.what = HANDLER_CACHE_REMOVE;
                    ACache.get(context).remove(key);
                    CachesSP.removeSP(context, key);
                    //ACache.get(Caches.this.cordova.getContext()).put(key,"");
                }
                mHandler.sendMessage(msg);
            } catch (Exception e) {
                Log.d(TAG, "存储错误e=" + e.toString());
                msg.what = HANDLER_CACHE_ERROR;
                mHandler.sendMessage(msg);
            }
       });
    }

    public void sendCachesMessage(CallbackContext callback, JSONArray array, boolean error) {
        Log.d(TAG,"返回的Caches="+array);
        PluginResult pluginResult;
        if (error) {
            pluginResult = new PluginResult(PluginResult.Status.ERROR,array);
        } else {
            pluginResult = new PluginResult(PluginResult.Status.OK,array);
        }
        pluginResult.setKeepCallback(false);
        callback.sendPluginResult(pluginResult);
    }

    public JSONArray formatJSONArray(int cacheType, int status, String key, Object value,
                                     int actionType, String path){
        JSONArray array = new JSONArray();
        //array数组:[1,0,"test_c","save_text"]
        //0.cacheType:0=存储,1=读取,2=删除
        //1.status:0=完成，1=key为null，2=value为null，3=存储的数据格式不支持，4=缓存中出错;
        //2.存储的文件名
        //3.存储的String/JSONArray数据
        try {
            array.put(0,cacheType);
            array.put(1,status);
            array.put(2,key);
            array.put(3,value);
            array.put(4,actionType);
            array.put(5, path);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }


    public JSONArray formatJSONArray(int cacheType, int status, String key, Object value,
                                     int actionType) {
        return formatJSONArray(cacheType, status, key, value, actionType, "");
    }

    public final static int HANDLER_CACHE_ERROR             = 0;
    public final static int HANDLER_CACHE_FORMAT_ERROR      = 1;
    public final static int HANDLER_CACHE_SAVE              = 2;
    public final static int HANDLER_CACHE_READ              = 3;
    public final static int HANDLER_CACHE_REMOVE            = 4;
    public final static int HANDLER_CACHE_ANALYSIS_ERROR    = 5;
    public final static int HANDLER_MAIL_SUCCESS            = 6;
    public final static int HANDLER_MAIL_FAILE              = 7;
    public final static int HANDLER_MAIL_ADDRESS_PASS_ERROR = 8;

    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Object[] objs = (Object[]) msg.obj;
            int cacheType = (int) objs[0];
            String key    = (String) objs[1];
            Object value  = objs[2];
            int actionType= (int) objs[3];
            CallbackContext callback = (CallbackContext) objs[4];
            String path = "";
            if (objs.length > 5) {
                path = (String) objs[5];
            }
            if (msg.what == HANDLER_CACHE_FORMAT_ERROR) {
                sendCachesMessage(callback, formatJSONArray(cacheType, CACHE_VALUE_FORMAT_ERROR, key, "",actionType),true);
            } else if (msg.what == HANDLER_CACHE_ANALYSIS_ERROR) {
                sendCachesMessage(callback, formatJSONArray(cacheType, CACHE_VALUE_ANALYSIS_ERROR, key, "",actionType),true);
            } else if (msg.what == HANDLER_MAIL_SUCCESS) {
                sendCachesMessage(callback,
                        formatJSONArray(cacheType, MAIL_SEND_SUCCESS, key, value, actionType),false);
            } else if (msg.what == HANDLER_MAIL_FAILE) {
                sendCachesMessage(callback,
                        formatJSONArray(cacheType, MAIL_SEND_FAILE, key, value, actionType),true);
            } else if (msg.what == HANDLER_MAIL_ADDRESS_PASS_ERROR) {
                sendCachesMessage(callback,
                        formatJSONArray(cacheType, MAIL_ADDRESS_OR_PASS_ERROR, key, value, actionType),true);
            } else {
                sendCachesMessage(callback, formatJSONArray(cacheType
                        , msg.what == HANDLER_CACHE_ERROR ? CACHE_ERROR : CACHE_FINISH, key
                        , msg.what == HANDLER_CACHE_READ ? value : "",actionType, path)
                        , msg.what == HANDLER_CACHE_ERROR);
            }
        }
    };

    public interface MailListener {
       void finish(int result);
    }
}
