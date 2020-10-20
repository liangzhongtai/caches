//
//  Caches.h
//  HelloCordova
//
//  Created by 梁仲太 on 2018/9/17.
//

#import <Cordova/CDVPlugin.h>

// 保存
static NSInteger const SAVE = 0;
// 读写
static NSInteger const READ  = 1;
// 正常
static NSInteger const REMOVE = 2;
// 获取文件路径
static NSInteger const FILE_PATH = 3;
// 分享文件到微信/QQ
static NSInteger const FILE_SHARE = 5;
// 发送邮件
static NSInteger const SEND_MAIL = 7;
// 保存base64图片
static NSInteger const SAVE_BASE64_IMG = 8;
// 打开微信
static NSInteger const OPEN_WEIXIN = 9;
// 打开QQ
static NSInteger const OPEN_QQ = 10;
// 检查文件是否存在
static NSInteger const CHECK_FILE_EXITS = 13;

// 缓存完成
static NSInteger const CACHE_FINISH             = 0;
// 缓存的key为nil
static NSInteger const CACHE_KEY_NIL            = 1;
// 缓存的value为nil
static NSInteger const CACHE_VALUE_NIL          = 2;
// 文件路径返回
static NSInteger const FILE_PATH_SUC            = 3;
// 文件路径不存在
static NSInteger const FILE_PATH_FAI            = 7;
// 邮件发送失败
static NSInteger const MAIL_SEND_FAI            = 8;
// 文件不存在
static NSInteger const FILE_UNEXIST             = 9;
// 邮件发送成功
static NSInteger const MAIL_SEND_SUCCESS        = 10;
// 邮件地址/密码错误
static NSInteger const MAIL_ADDRESS_PASS_ERROR  = 11;

// 默认
static NSInteger const DEFAULT  = 0;

// JSONArray转CSV存储
static NSInteger const JSON2CSV = 1;

// CSV转JSONArray返回
static NSInteger const CSV2JSON = 2;

@interface Caches : CDVPlugin


-(void)successWithMessage:(NSArray *)messages;

-(void)faileWithMessage:(NSString *)message;

@end
