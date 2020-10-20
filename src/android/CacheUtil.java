package com.chinamobile.cache;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import Decoder.BASE64Decoder;

/**
 * Created by liangzhongtai on 2020/3/18.
 */
public class CacheUtil {
    //加解密时以32K个字节为单位进行加解密计算
    private static final int BUFFER_LENGHT_CIPHER = 64;
    // 摘要长度
    private static final int LENGTH_SHA1 = 40;

    /**
     * 保存JSONArray
     * */
    public static void writeArray(String fileName, String dirName, boolean needDecodeAndEncode,
                                  boolean needDefenseTamper, JSONArray array) {
        String path = Environment.getExternalStorageDirectory() + (TextUtils.isEmpty(dirName)
                ? "/" : ("/" + dirName + "/")) + fileName;
        File dir = new File(Environment.getExternalStorageDirectory() +
                (TextUtils.isEmpty(dirName) ? "" : ("/" + dirName)));
        File file = new File(path);
        if(file.exists()){
            file.delete();
        } else if(!dir.exists()) {
            dir.mkdirs();
        }
        try {
            String string = array.toString();
            // 增加摘要
            if (needDefenseTamper) {
                String sha1 = checkSHA1(string);
                string = string + sha1;
            }
            FileUtils.writeStringToFile(file, string, "UTF-8");
            // 生成加密文件
            if (needDecodeAndEncode) {
                encodeFile(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读JSONArray
     * */
    public static JSONArray readArray(String fileName, String dirName, boolean needDecodeAndEncode,
                                      boolean needDefenseTamper) {
        String path = Environment.getExternalStorageDirectory() + (TextUtils.isEmpty(dirName)
                ? "/" : ("/" + dirName + "/")) + fileName;

        String copyPath = Environment.getExternalStorageDirectory() + (TextUtils.isEmpty(dirName)
                ? "/" : ("/" + dirName + "/")) + "copy_" + fileName;
        if (needDecodeAndEncode) {
            // 生成副本文件
            copyFile(path, copyPath);
            // 解密副本文件
            decodeFile(copyPath);
        }
        String readPath = needDecodeAndEncode ? copyPath : path;
        File file = new File(readPath);
        JSONArray array = new JSONArray();
        try {
            Log.d(Caches.TAG,"path=" + path);
            Log.d(Caches.TAG,"file=" + file.exists());
            String string = FileUtils.readFileToString(file);
            // 去除摘要
            if (needDefenseTamper && string.length() >= LENGTH_SHA1) {
                string = string.substring(0, string.length() - LENGTH_SHA1);
            }
            array = new JSONArray(string);
            // 删除副本文件
            if (needDecodeAndEncode) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }


    /**
     * 保存JSONObject
     * */
    public static void writeObject(String fileName, String dirName, boolean needDecodeAndEncode,
                                   boolean needDefenseTamper, JSONObject obj) {
        String path = Environment.getExternalStorageDirectory() + (TextUtils.isEmpty(dirName)
                ? "/" : ("/" + dirName + "/")) + fileName;
        File dir = new File(Environment.getExternalStorageDirectory() +
                (TextUtils.isEmpty(dirName) ? "" : ("/" + dirName)));
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        } else if(!dir.exists()) {
            dir.mkdirs();
        }
        try {
            String string = obj.toString();
            // 增加摘要
            if (needDefenseTamper) {
                String sha1 = checkSHA1(string);
                string = string + sha1;
            }
            FileUtils.writeStringToFile(file, string, "UTF-8");
            // 生成加密文件
            if (needDecodeAndEncode) {
                encodeFile(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读JSONObject
     * */
    public static JSONObject readObject(String fileName, String dirName, boolean needDecodeAndEncode,
                                        boolean needDefenseTamper) {
        String path = Environment.getExternalStorageDirectory() + (TextUtils.isEmpty(dirName)
                ? "/" : ("/" + dirName + "/")) + fileName;
        String copyPath = Environment.getExternalStorageDirectory() + (TextUtils.isEmpty(dirName)
                ? "/" : ("/" + dirName + "/")) + "copy_" + fileName;

        if (needDecodeAndEncode) {
            // 生成副本文件
            copyFile(path, copyPath);
            // 解密副本文件
            decodeFile(copyPath);
        }

        String readPath = needDecodeAndEncode ? copyPath : path;
        File file = new File( readPath);
        JSONObject obj = new JSONObject();
        try {
            String string = FileUtils.readFileToString(file);
            // 去除摘要
            if (needDefenseTamper && string.length() >= LENGTH_SHA1) {
                string = string.substring(0, string.length() - LENGTH_SHA1);
            }
            obj = new JSONObject(string);
            // 删除副本文件
            if (needDecodeAndEncode) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * 读JSONObject
     * */
    public static String[] readContentAndSha1s(String relatePath, boolean needDecodeAndEncode,
                                               boolean needDefenseTamper) {
        String path = Environment.getExternalStorageDirectory() + relatePath;
        // 解密加密文件
        if (needDecodeAndEncode) {
            decodeFile(path);
        }
        File file = new File(path);
        String[] strs = new String[]{"", ""};
        try {
            String string = FileUtils.readFileToString(file);
            // 切割数据和摘要
            if (needDefenseTamper && string.length() >= LENGTH_SHA1) {
                strs[0] = string.substring(0, string.length() - LENGTH_SHA1);
                strs[1] = string.substring(string.length() - LENGTH_SHA1, string.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strs;
    }

    /**
     * 保存JSONArray
     * */
    public static void writeString(String fileName, String dirName, String string) {
        String path = Environment.getExternalStorageDirectory() + (TextUtils.isEmpty(dirName)
                ? "/" : ("/" + dirName + "/")) + fileName;
        File dir = new File(Environment.getExternalStorageDirectory() +
                (TextUtils.isEmpty(dirName) ? "" : ("/" + dirName)));
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }else if(!dir.exists()){
            dir.mkdirs();
        }
        try {
            FileUtils.writeStringToFile(file, string, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 读JSONArray
     * */
    public static String readString(String fileName, String dirName) {
        String path = Environment.getExternalStorageDirectory() + (TextUtils.isEmpty(dirName)
                ? "/" : ("/" + dirName + "/")) + fileName;
        File file = new File(path);
        String string = "";
        try {
            string = FileUtils.readFileToString(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }


    public static String HOST = "smtp.139.com";
    public static String PORT = "25";
    public static String FROM_ADD = "18353366239@139.com";
    public static String FROM_PSW = "XXXX";
    public static String MAIL_TITLE = "";
    public static String MAIL_CONTENT = "";

    public static void sendSystemMail(Caches plugin, File file, String[] toEmails, String[] ccEmails) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        //邮件发送类型：带附件的邮件
        intent.setType("application/octet-stream");
        //设置邮件地址
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, toEmails);
        //设置抄送人邮件地址
        intent.putExtra(Intent.EXTRA_CC, ccEmails);
        //设置邮件标题
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, MAIL_TITLE);
        //设置发送的内容
        intent.putExtra(android.content.Intent.EXTRA_TEXT, MAIL_CONTENT);
        //附件
        if (Build.VERSION.SDK_INT >= 24) {
            Uri uri = FileProvider.getUriForFile(plugin.cordova.getContext(),
                    plugin.cordova.getContext().getPackageName() + ".provider", file);
            //7.0以后，系统要求授予临时uri读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        }
        //调用系统的邮件系统
        plugin.cordova.getActivity().startActivity(Intent.createChooser(intent, "请选择邮件发送应用"));

    }

    public static void mailToSystemMail(Caches plugin, String[] toEmails, String[] ccEmails) {
        Uri uri = Uri.parse("mailto:" + toEmails[0]);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        //设置邮件地址
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, toEmails);
        //设置抄送人邮件地址
        intent.putExtra(Intent.EXTRA_CC, ccEmails);
        //设置邮件标题
        intent.putExtra(Intent.EXTRA_SUBJECT, MAIL_TITLE);
        //设置发送的内容
        intent.putExtra(Intent.EXTRA_TEXT, MAIL_CONTENT);
        plugin.cordova.getActivity().startActivity(Intent.createChooser(intent,"请选择邮件发送应用"));
    }

    public static void setHtml(String[] add,String[] cc,String[] bcc) {
        final Mail mail = createMail(add, null, null);
        mail.setContent("<h1>这是标题</h1>");
        final MailSender sender = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sender.sendHtmlMail(mail);
            }
        }).start();
    }

    public static void sendMailWithFile(final File file, String[] toAdd, String[] cc, String[] bcc,
                                        Caches.MailListener listener) {
        final Mail mail = createMail(toAdd, cc, bcc);
        String content = mail.getContent();
        String s = content;
        mail.setContent(s);
        final MailSender sms = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = sms.sendFileMail(mail, file);
                if (listener != null) {
                    listener.finish(result);
                }
                Log.d(Caches.TAG, "附件发送------");
            }
        }).start();
    }


    public static void sendMail(String[] toAdd, String[] cc, String[] bcc) {
        final Mail mail = createMail(toAdd, cc, bcc);
        final MailSender sms = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sms.sendTextMail(mail);
            }
        }).start();
    }


    private static Mail createMail(String[] toAdds, String[] ccAdds, String bccAdds[]) {
        final Mail mail = new Mail();
        mail.setMailServerHost(HOST);
        mail.setMailServerPort(PORT);
        mail.setValidate(true);
        if (FROM_ADD.contains(".139")) {
            mail.setUserName(FROM_ADD); // 你的邮箱账号
        } else {
            String[] split = FROM_ADD.split("@");
            mail.setUserName(split[0]); // 你的邮箱账号
        }
        mail.setPassword(FROM_PSW);// 您的邮箱密码
        mail.setFromAddress(FROM_ADD); // 发送的邮箱
        mail.setToAddress(toAdds); // 发到哪个邮件去
        mail.setCcAddress(ccAdds);// 抄送邮件
        mail.setBccAddress(bccAdds);// 秘密抄送邮件
        mail.setSubject(MAIL_TITLE); // 邮件主题
        mail.setContent(MAIL_CONTENT); // 邮件文本
        return mail;
    }


    public static void main(String[] args) throws AddressException,MessagingException {
        // Log.d(Caches.TAG, "附件发送成功------0");
//        Properties properties = new Properties();
//        properties.put("mail.transport.protocol", "smtp");// 连接协议
//        properties.put("mail.smtp.host", "smtp.139.com");// 主机名
//        properties.put("mail.smtp.port", 465);// 端口号
//        properties.put("mail.smtp.auth", "true");
//        properties.put("mail.smtp.ssl.enable", "true");// 设置是否使用ssl安全连接 ---一般都使用
//        properties.put("mail.debug", "true");// 设置是否显示debug信息 true 会在控制台显示相关信息
//        // 得到回话对象
//        Session session = Session.getInstance(properties);
//        // 获取邮件对象
//        Message message = new MimeMessage(session);
//        // 设置发件人邮箱地址
//        message.setFrom(new InternetAddress("18520660170@139.com"));
//        // 设置收件人邮箱地址
//        message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{
//                new InternetAddress("15217828379@139.com")});
//        //message.setRecipient(Message.RecipientType.TO, new InternetAddress("xxx@qq.com"));//一个收件人
//        // 设置邮件标题
//        message.setSubject("群发邮件");
//        // 设置邮件内容
//        message.setText("邮件内容邮件内容邮件内容xmqtest");
//        // 得到邮差对象
//        Transport transport = session.getTransport();
//        // 连接自己的邮箱账户
//        transport.connect("18520660170@139.com", "Abc6229568");// 密码为QQ邮箱开通的stmp服务后得到的客户端授权码
//        // 发送邮件
//        transport.sendMessage(message, message.getAllRecipients());
//        transport.close();
        // Log.d(Caches.TAG, "附件发送成功------1");


        // 收件人电子邮箱
//         String to = "*********@163.com";
        String to = "279113482@qq.com";

        // 发件人电子邮箱
        String from = "18520660170@139.com";


        // 指定发送邮件的主机为 smtp.qq.com
        String host = "smtp.139.com";  //139 邮件服务器

        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", host);

        properties.put("mail.smtp.auth", "true");

        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties,new Authenticator(){
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, "Abc6229568"); //发件人邮件用户名、密码
            }
        });

        try{
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);
            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: 头部头字段
            message.setSubject("邮件头部!");
            // 设置消息体 message.setContent("测试", "text/html;charset=utf-8");
            // 设置内容  如果我们所使用的MimeMessage中信息内容是文本的话，我们便可以直接使用setText()方法来方便的设置文本内容
            message.setContent("<h2 style=\"color: red\">Hello World!</h2>\n", "text/html;charset=utf-8");
            // 发送消息
            Transport.send(message);
            System.out.println("发送 信息 成功....");
        }catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加密文件
     * */
    public static void encodeFile(String filePath) {
        try {
            long startTime = System.currentTimeMillis();
            File f = new File(filePath);
            if (!f.exists()) {
                return;
            }

            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            long totalLenght = raf.length();
            FileChannel channel = raf.getChannel();
            long multiples = totalLenght / BUFFER_LENGHT_CIPHER;
            long remainder = totalLenght % BUFFER_LENGHT_CIPHER;

            MappedByteBuffer buffer;
            byte tmp;
            byte rawByte;
            //先对整除部分加密
            for (int i = 0; i < multiples; i++) {
                buffer = channel.map(FileChannel.MapMode.READ_WRITE,
                        i * BUFFER_LENGHT_CIPHER,
                        (i + 1) * BUFFER_LENGHT_CIPHER);
                //此处的加密方法很简单，只是简单的异或计算
                for (int j = 0; j < BUFFER_LENGHT_CIPHER; ++j) {
                    rawByte = buffer.get(j);
                    tmp = (byte) (rawByte ^ j);
                    buffer.put(j, tmp);

                }
                buffer.force();
                buffer.clear();
            }
            //对余数部分加密
            buffer = channel.map(FileChannel.MapMode.READ_WRITE,
                    multiples * BUFFER_LENGHT_CIPHER,
                    multiples * BUFFER_LENGHT_CIPHER + remainder);
            for (int j = 0; j < remainder; ++j) {
                rawByte = buffer.get(j);
                tmp = (byte) (rawByte ^ j);
                buffer.put(j, tmp);
            }
            buffer.force();
            buffer.clear();

            channel.close();
            raf.close();
            //对加密后的文件重命名，增加.cipher后缀
            // f.renameTo(new File(f.getPath() + CIPHER_TEXT_SUFFIX));
            Log.d("加密用时：", (System.currentTimeMillis() - startTime) /1000 + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解密文件
     * */
    public static boolean decodeFile(String filePath) {
        try {
            long startTime = System.currentTimeMillis();
            File f = new File(filePath);
            if (!f.exists()) {
                return false;
            }
            // if(!f.getPath().toLowerCase().endsWith(CIPHER_TEXT_SUFFIX)){
            // //后缀不同，认为是不可解密的密文
            // return false;
            // }

            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            long totalLenght = raf.length();
            FileChannel channel = raf.getChannel();

            long multiples = totalLenght / BUFFER_LENGHT_CIPHER;
            long remainder = totalLenght % BUFFER_LENGHT_CIPHER;

            MappedByteBuffer buffer;
            byte tmp;
            byte rawByte;

            //先对整除部分解密
            for(int i = 0; i < multiples; i++) {
                buffer = channel.map(FileChannel.MapMode.READ_WRITE,
                        i * BUFFER_LENGHT_CIPHER,
                        (i + 1) * BUFFER_LENGHT_CIPHER);

                //此处的解密方法很简单，只是简单的异或计算
                for (int j = 0; j < BUFFER_LENGHT_CIPHER; ++j) {
                    rawByte = buffer.get(j);
                    tmp = (byte) (rawByte ^ j);
                    buffer.put(j, tmp);
                }
                buffer.force();
                buffer.clear();
            }

            //对余数部分解密
            buffer = channel.map(FileChannel.MapMode.READ_WRITE,
                    multiples * BUFFER_LENGHT_CIPHER,
                    multiples * BUFFER_LENGHT_CIPHER + remainder);

            for (int j = 0; j < remainder; ++j) {
                rawByte = buffer.get(j);
                tmp = (byte) (rawByte ^ j);
                buffer.put(j, tmp);
            }
            buffer.force();
            buffer.clear();
            channel.close();
            raf.close();
            //对加密后的文件重命名，增加.cipher后缀
            // f.renameTo(new File(f.getPath().substring(f.getPath().toLowerCase().indexOf(CIPHER_TEXT_SUFFIX))));
            Log.d("解密用时：", (System.currentTimeMillis() - startTime) / 1000 + "s");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 拷贝文件副本，用于解密
     * */
    private static void copyFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            Log.d(Caches.TAG, "copyFile:  oldFile not exist.");
            return;
        }

        if (!oldFile.isFile()) {
            Log.d(Caches.TAG, "copyFile:  oldFile not file.");
            return;
        }

        if (!oldFile.canRead()) {
            Log.d(Caches.TAG, "copyFile:  oldFile cannot read.");
            return;
        }

        File newFile = new File(newPath);
        if (newFile.exists()) {
            newFile.delete();
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(oldPath);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * base64编码字符串转换为图片
     * @param base64 base64编码字符串
     * @param fileName 图片文件名
     * @return
     */
    public static String base64Str2Image(Context context, String base64, String fileName) {
        Log.d(Caches.TAG,"base64=" + base64);
        Log.d(Caches.TAG,"fileName=" + fileName);
        if (base64 == null) {
            return null;
        }
        // 相册路径
        String path ;
        // 小米, VIVO手机
        String brand = Build.BRAND.toLowerCase();
        Log.d(Caches.TAG, "brand=" + brand);
        // 魅族，Oppo
        if(brand.contains("meizu") || brand.contains("oppo")) {
            path = Environment.getExternalStorageDirectory().getPath()+"/DCIM/"+fileName ;
        // 其它
        } else {
            path = Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/"+fileName ;
        }
        if (new File(path).exists()) {
            new File(path).delete();
        }
        // 自定义路径
        String pathCover = Environment.getExternalStorageDirectory() + "/COVER/" + fileName;
        if (new File(pathCover).exists()) {
            new File(pathCover).delete();
        }
        Log.d(Caches.TAG,"path：" + path);
        Log.d(Caches.TAG,"pathCover：" + pathCover);
        File dir = new File(Environment.getExternalStorageDirectory() + "/COVER");
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 解密
            byte[] bCover = decoder.decodeBuffer(base64);
            // 处理数据
            for (int i = 0; i < bCover.length; ++i) {
                if (bCover[i] < 0) {
                    bCover[i] += 256;
                }
            }
            // 保存到COVER目录
            OutputStream outCover = new FileOutputStream(pathCover);
            outCover.write(bCover);
            outCover.flush();
            outCover.close();

            // 解密
            byte[] b = decoder.decodeBuffer(base64);
            // 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            // 保存到相册
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();


            // 插入图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(), path, fileName, null);
            // 发送广播，通知刷新图库的显示
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));
            // 刷新相册
            MediaScannerConnection.scanFile(context, new String[]{ path }, null, null);
            return path;
        } catch (Exception e) {
            Log.d(Caches.TAG,"异常"+e.toString());
            return null;
        }
    }

    /**
     * 检验文件生成唯一的md5值 作用：检验文件是否已被修改
     * @param file 需要检验的文件
     * @return 该文件的md5值
     */
    private static String checkMd5(File file) {
        // 若输入的参数不是一个文件 则抛出异常
        if (!file.isFile()) {
            throw new NumberFormatException("参数错误！请输入校准文件。");
        }
        // 定义相关变量
        FileInputStream fis = null;
        byte[] rb = null;
        DigestInputStream digestInputStream;
        try {
            fis = new FileInputStream(file);
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digestInputStream = new DigestInputStream(fis,md5);
            byte[] buffer = new byte[4096];
            while (digestInputStream.read(buffer) > 0) {}
            md5 = digestInputStream.getMessageDigest();
            rb = md5.digest();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }finally{
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rb.length; i++) {
            String a = Integer.toHexString(0XFF & rb[i]);
            if (a.length() < 2) {
                a = '0' + a;
            }
            sb.append(a);
        }
        return sb.toString();
    }

    /**
     * 检查sha1值
     * */
    public static String checkSHA1(String str) {
        String sha1 = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(str.getBytes());
            byte[] shaBytes = messageDigest.digest();
            sha1 = byte2HexString(shaBytes);
            Log.d(Caches.TAG, "计算sha1=" + sha1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sha1;
    }

    private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static String byte2HexString(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_UPPER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_UPPER[0x0F & data[i]];
        }
        return new String(out);
    }
}
