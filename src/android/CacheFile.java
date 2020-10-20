package com.chinamobile.cache;

import android.text.TextUtils;

import java.io.File;
import java.io.Serializable;

/**
 * Created by liangzhongtai on 2019/7/19.
 */

public class CacheFile implements Serializable {
    // 文件名
    public String fileName;
    // 文件存储路径
    public String filePath;
    // 文件大小
    public long fileSize;

    public static CacheFile newInstance(String filePath) {
        CacheFile file = new CacheFile();
        file.filePath = filePath;
        if(!TextUtils.isEmpty(filePath)) {
            String[] array = filePath.split("/");
            file.fileName = array[array.length-1];
            file.fileSize = new File(filePath).length();
        }
        return file;
    }
    public String formatJson() {
        String json;
        json = "{\"fileName\":\"" + fileName + "\", \"filePath\":\"" + filePath + "\", \"fileSize\":" + fileSize + "}";
        return json;
    }
}
