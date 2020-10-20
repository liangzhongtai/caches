//
//  NSCSVUtil.h
//  网优助手
//
//  Created by 梁仲太 on 2018/11/20.
//

#import <Foundation/Foundation.h>

@interface NSCSVUtil : NSObject

//CSV读成JSONArray返回
-(NSMutableArray *)inputFile:(NSString *)fileName;

//JSONArray写入CSV
-(void)exportCsv:(NSString*)fileName andArray:(NSArray *)array;

//创建指定文件
-(void)createTempFile:(NSString*)path;

//在cache目录下生成一个盛放CSV的文件夹
-(NSString *)findLocalPath:(NSString *)fileName andInit:(BOOL) init;

//创建盛放CSV文件的文件夹
-(NSString *)creatFile:(NSString *)name andInit:(BOOL) init;

//md5加密
-(NSString *)md5:(NSString *)str;

@end
