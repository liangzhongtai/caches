//
//  NSCSVUtil.m
//  网优助手
//
//  Created by 梁仲太 on 2018/11/20.
//

#import "NSCSVUtil.h"
#import <CommonCrypto/CommonDigest.h>

@implementation NSCSVUtil

/**
 读文件
 
 @param path 文件路径
 */
-(NSMutableArray *)inputFile:(NSString *)fileName{
    NSError *error = nil;
    //unsigned long encode = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    NSString *path = [self findLocalPath:fileName andInit:NO];
    NSString *fileContents = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&error];
    NSLog(@"fileContents=%@",fileContents);
    NSMutableArray *array = [NSMutableArray array];
    NSArray *lines = [fileContents componentsSeparatedByString:@"\n"];
    NSArray *keys  = [lines[0] componentsSeparatedByString:@","];
    int lineCount = (int)lines.count-1;
    int keyCount  = (int)keys.count;
    for (int i=1; i<lineCount; i++) {
        NSMutableDictionary *dic = [NSMutableDictionary new];
        NSArray *lineValues = [lines[i] componentsSeparatedByString:@","];
        for (int j=0; j<keyCount; j++) {
            [dic setValue:lineValues[j] forKey:keys[j]];
        }
        [array addObject:dic];
    }
    return array;
}

//
/**
 将数据取出 写入文件
 @param filename 文件路径
 */
-(void)exportCsv:(NSString*)fileName andArray:(NSArray *)array{
    NSLog(@"fileName=%@",fileName);
    NSString* path = [self findLocalPath:fileName andInit:YES];
    NSLog(@"path= %@", path);
    
    [self createTempFile: path];
    NSOutputStream* output = [[NSOutputStream alloc] initToFileAtPath: path append: YES];
    [output open];
    if (![output hasSpaceAvailable]){
        NSLog(@"No space available in %@", path);
    }else{
        NSLog(@"array[0].keys= %@", [array[0] allKeys]);
        //NSInteger result = 0;
        NSString *header = @"";
        NSMutableArray *lines = [NSMutableArray array];
        NSArray *keys = [array[0] allKeys];
        int keyCount = (int)keys.count;
        int arrayCount = (int)array.count;
        for (int i=0; i<keyCount; i++) {
            header = [header stringByAppendingString:[NSString stringWithFormat:i==keyCount-1?@"%@\n":@"%@,",keys[i]]];
        }
        NSLog(@"header= %@", header);
        //uint8_t buffer[2048];
        //memcpy(buffer, [header UTF8String], [header length]+1);
        //result = [output write:buffer maxLength: [header length]];
        for (int i = 0; i < arrayCount; i++){
            NSString* line = @"";
            for (int j=0; j<keyCount; j++) {
                NSDictionary *obj = array[i];
                NSString *key = keys[j];
                //NSString *str=[[obj valueForKey:key] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];
                line = [line stringByAppendingString:[NSString stringWithFormat:j==keyCount-1?@"%@\n":@"%@,",[obj valueForKey:key]]];
                
            }
            //uint8_t buffer[2048];
            //memcpy(buffer, [line UTF8String], [line length]+1);
            //result = [output write:buffer maxLength: [line length]];
            [lines addObject:line];
            NSLog(@"line= %@", line);
        }
        
        NSFileHandle* fileHandle = [NSFileHandle fileHandleForUpdatingAtPath:path];
        //将节点调到文件末尾
        [fileHandle seekToEndOfFile];
        [fileHandle writeData:[header dataUsingEncoding:NSUTF8StringEncoding]];
        for(int i=0;i<lines.count;i++){
            //追加写入数据
            [fileHandle writeData:[lines[i] dataUsingEncoding:NSUTF8StringEncoding]];
        }
        
        [fileHandle closeFile];
    }
    [output close];
    NSLog(@"exportCsv存在=%d",[[NSFileManager defaultManager] fileExistsAtPath:path]);
    NSString *str = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:nil];
    //str = [str stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    NSLog(@"str存在=%@",str);
}



//创建指定文件
- (void)createTempFile:(NSString*)path{
    NSFileManager* fileSystem = [NSFileManager defaultManager];
    [fileSystem removeItemAtPath: path error: nil];
    
    NSMutableDictionary* attributes = [[NSMutableDictionary alloc] init];
    NSNumber* permission = [NSNumber numberWithLong: 0640];
    [attributes setObject: permission forKey: NSFilePosixPermissions];
    if(![fileSystem createFileAtPath: path contents: nil attributes: attributes]){
        NSLog(@"Unable to create temp file for exporting CSV.");
    }
}

//在cache目录下生成一个盛放CSV的文件夹
- (NSString *)findLocalPath:(NSString *)fileName andInit:(BOOL)init{
    NSString* documentsDir = [self creatFile:@"CSV" andInit:init];
    //NSString *name = [NSString stringWithFormat:@"%@",fileName];
    //NSString *md5  = [self md5:name];//md5加密保证文件的唯一性
    NSString* csvPath = [NSString stringWithFormat:@"%@/%@",documentsDir,fileName];//文件路径
    //NSLog(@"csvPath=%@",csvPath);
    return csvPath;
}

//创建盛放CSV文件的文件夹
- (NSString *)creatFile:(NSString *)name andInit:(BOOL)init{
    NSString *pathString = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES)[0];
    pathString = [NSString stringWithFormat:@"%@/%@",pathString,name];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSLog(@"creatFile存在=%d",[[NSFileManager defaultManager] fileExistsAtPath:pathString]);
    if (init&&![fileManager fileExistsAtPath:pathString]){
        [fileManager createDirectoryAtPath:pathString withIntermediateDirectories:YES attributes:nil error:nil];
    }
    //NSLog(@"pathString=%@",pathString);
    return pathString;
}

//md5加密
- (NSString *)md5:(NSString *)str{
    const char *cStr = [str UTF8String];
    unsigned char digest[CC_MD5_DIGEST_LENGTH];
    CC_MD5(cStr,(CC_LONG)strlen(cStr), digest);
    NSMutableString *output = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for(int i = 0; i < CC_MD5_DIGEST_LENGTH; i++)
        [output appendFormat:@"%02x", digest[i]];
    return  output;
}


@end
