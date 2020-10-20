//
//  Caches.m
//  HelloCordova
//
//  Created by 梁仲太 on 2018/9/17.
//

#import "Caches.h"
#import <objc/runtime.h>
#import "NSCSVUtil.h"
#import <MessageUI/MessageUI.h>
#import "NSData+Base64Additions.h"
#import "SKPSMTPMessage.h"

@interface Caches()<MFMailComposeViewControllerDelegate, SKPSMTPMessageDelegate>

@property(nonatomic,copy)NSString *callbackId;
@property(nonatomic,assign)NSInteger cahceType;
@property(nonatomic,strong)NSString *key;
@property(nonatomic,strong)NSObject *value;
@property(nonatomic,assign)NSInteger actionType;

@end

@implementation Caches

-(void)coolMethod:(CDVInvokedUrlCommand *)command{
    NSLog(@"--------*********执行Caches");
    self.callbackId = command.callbackId;
    
    self.cahceType  = [command.arguments[0] integerValue];
    self.key        =  command.arguments[1];
    self.actionType = DEFAULT;
    NSLog(@"--------*********cahceType=%ld", self.cahceType);
    if (self.cahceType == CHECK_FILE_EXITS) {
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        NSObject *obj = [defaults objectForKey:[self.key stringByReplacingOccurrencesOfString:@"/" withString:@""]];;
        [self successWithMessage:@[[NSNumber numberWithInteger:self.cahceType]
                                   ,[NSNumber numberWithInteger:CACHE_FINISH], self.key, [NSNumber numberWithBool: obj != nil ]]];
        return;
    } else if (self.cahceType == FILE_SHARE) {
        // 获取Documents文件夹目录
        NSLog(@"*************imageDocPath0=%@",self.key);
        NSArray *path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSLog(@"*************imageDocPath1=%@",path);
        NSString *documentPath = [path objectAtIndex:0];
        NSLog(@"*************imageDocPath2=%@",documentPath);
        // 指定新建文件夹路径
        NSString *imageDocPath = [documentPath stringByAppendingPathComponent:[NSString stringWithFormat:@"/%@", self.key]];
        NSLog(@"*************imageDocPath3=%@",imageDocPath);
        // [[UIApplication sharedApplication] openURL:[NSURL URLWithString:imageDocPath]];
        
        [self shareItems:@[[NSURL fileURLWithPath:imageDocPath]] target:self.viewController];
        return;
    } else if (self.cahceType == SEND_MAIL) {
        NSString *fileName = command.arguments[1];
        NSString *address  = command.arguments[2];
        NSString *pass     = command.arguments[3];
        NSString *ip       = command.arguments[4];
        NSInteger port     = [command.arguments[5] integerValue];
        NSString *receive  = command.arguments[6];
        NSString *title    = command.arguments[7];
        NSString *content  = command.arguments[8];
        NSString *carbon   = command.arguments[9];
        if ([address hasPrefix:@"139.com"]) {
            Class mailClass = (NSClassFromString(@"MFMailComposeViewController"));
            // 不支持APP内发生邮件
            if (!mailClass) {
                NSLog(@"不支持APP内发生邮件");
                
                NSLog(@"carbon=%@", carbon);
                NSMutableString *mailUrl = [[NSMutableString alloc]init];
                // 添加收件人
                NSArray *toRecipients;
                if ([receive hasPrefix:@","]) {
                    toRecipients = [receive componentsSeparatedByString:@","];
                } else {
                    toRecipients = [NSArray arrayWithObjects:receive, nil];
                }
                [mailUrl appendFormat:@"mailto:%@", [toRecipients componentsJoinedByString:@","]];
                // 添加抄送
                NSArray *ccRecipients;
                if (carbon == nil || [@"nil" isEqualToString:carbon] || [@"null" isEqualToString:carbon]) {
                    ccRecipients = [NSArray array];
                } else if ([carbon hasPrefix:@","]) {
                    ccRecipients = [carbon componentsSeparatedByString:@","];
                } else {
                    ccRecipients = [NSArray arrayWithObjects:carbon, nil];
                }
                [mailUrl appendFormat:@"?cc=%@", [ccRecipients componentsJoinedByString:@","]];
                // 添加密送
                // NSArray *bccRecipients = [NSArray arrayWithObjects:@"密送人邮件1", nil];
                // [mailUrl appendFormat:@"&bcc=%@", [bccRecipients componentsJoinedByString:@","]];
                // 添加主题
                [mailUrl appendString: [NSString stringWithFormat:@"&subject=%@", title]];
                // 添加邮件内容
                [mailUrl appendString: [NSString stringWithFormat:@"&body=%@", content]];
                NSString* email = [mailUrl stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
                [[UIApplication sharedApplication] openURL: [NSURL URLWithString:email]];
                return;
            }
            if (![mailClass canSendMail]) {
                NSLog(@"用户没有设置邮件账户");
                // [self toastWith:@"用户没有设置邮件账户"];
                [self faileWithMessage:@"iphone没有设置邮件账户"];
                return;
            }
            [self displayMFMailComposeVC:command];
        } else {
            //设置基本参数：
            SKPSMTPMessage *mail = [[SKPSMTPMessage alloc] init];
            [mail setSubject:title]; // 设置邮件主题
            [mail setToEmail:receive]; // 目标邮箱
            [mail setCcEmail:carbon];// 抄送邮箱
            [mail setFromEmail:address]; // 发送者邮箱
            [mail setRelayHost:ip]; // 发送邮件代理服务器
            [mail setRequiresAuth:YES];
            [mail setLogin:address]; // 发送者邮箱账号
            [mail setPass:pass]; // 发送者邮箱密码
            [mail setWantsSecure:YES]; // 需要加密
            [mail setDelegate:self];
            //设置邮件正文内容：
            
            NSString *contentEncoding = [NSString stringWithCString:[content UTF8String] encoding:NSUTF8StringEncoding];
            NSDictionary *plainPart = @{kSKPSMTPPartContentTypeKey : @"text/plain; charset=UTF-8", kSKPSMTPPartMessageKey : contentEncoding, kSKPSMTPPartContentTransferEncodingKey : @"8bit"};
            //添加附件（以下代码可在SKPSMTPMessage库的DMEO里找到）：
            NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
            NSString *filePath = [cachePath stringByAppendingPathComponent: fileName];
            // NSString *vcfPath = [[NSBundle mainBundle] pathForResource:@"EmptyPDF" ofType:@"pdf"];
            NSFileManager *fileManager = [NSFileManager defaultManager];
//            if (![fileManager fileExistsAtPath:filePath]) {
//               [self faileWithMessage:@"附件不存在"];
//               return;
//            }
//            NSData *vcfData = [NSData dataWithContentsOfURL:[NSURL fileURLWithPath:filePath]];
//            NSDictionary *vcfPart = [NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"text/directory;\r\n\tx-unix-mode=0644;\r\n\tname=\"%@\"", fileName],kSKPSMTPPartContentTypeKey,[NSString stringWithFormat:@"attachment;\r\n\tfilename=\"%@\"", fileName],kSKPSMTPPartContentDispositionKey,[vcfData encodeBase64ForData], kSKPSMTPPartMessageKey, @"base64", kSKPSMTPPartContentTransferEncodingKey,nil];
            //执行发送邮件代码
            // 邮件首部字段、邮件内容格式和传输编码
            //[mail setParts:@[plainPart, vcfPart]];
            [mail setParts:@[plainPart]];
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                [mail send];
            });
        }
        return;
    }
    
    if (self.cahceType == SAVE_BASE64_IMG) {
        self.value         =  command.arguments[2];
        NSData *base64Data = [[NSData alloc] initWithBase64Encoding:(NSString *)self.value];
        UIImage *image     = [UIImage imageWithData:base64Data];
        // 获取Documents文件夹目录
        NSArray *path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documentPath = [path objectAtIndex:0];
        // 指定新建文件夹路径
        NSString *imageDocPath = [documentPath stringByAppendingPathComponent:[NSString stringWithFormat:@"/%@", self.key]];
        // 把图片转成NSData类型的数据来保存文件
        NSData *data = nil;
        // 判断图片是不是png格式的文件
        if (UIImagePNGRepresentation(image)) {
            //返回为png图像。
            data = UIImagePNGRepresentation(image);
        } else {
            // 返回为JPEG图像。
            data = UIImageJPEGRepresentation(image, 1.0);
        }
        // 保存到本地
        [[NSFileManager defaultManager] createFileAtPath:imageDocPath contents:data attributes:nil];
        
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:imageDocPath];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        
        // 保存到相册
        UIImageWriteToSavedPhotosAlbum(image, self, @selector(image:didFinishSavingWithError:contextInfo:), (__bridge void *)self);
        return;
    }
    
    if (self.cahceType == OPEN_WEIXIN) {
        // 调起微信扫一扫
        NSLog(@"打开微信扫一扫");
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"weixin://scanqrcode"] options:[NSDictionary new] completionHandler:^(BOOL success) {
            if (success) {
           }
        }];
        return;
    }
    
    if (self.cahceType == OPEN_QQ) {
        // 调起QQ扫一扫
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"mqqapi://qrcode/scan_qrcode?version=1&src_type=app"] options:[NSDictionary new] completionHandler:^(BOOL success) {
            if (success) {
            }
        }];
        return;
    }
    
    //检查CSV文件是否存在
    if((self.actionType == JSON2CSV||self.actionType == CSV2JSON)&&(self.cahceType == READ||self.cahceType == FILE_PATH)){
        NSString* csvPathLocal = [[NSCSVUtil new] findLocalPath:self.key andInit:NO];
        BOOL isExistLocal = [[NSFileManager defaultManager] fileExistsAtPath:csvPathLocal];
        if(!isExistLocal){
            [self successWithMessage:@[[NSNumber numberWithInteger:self.cahceType]
                                       ,[NSNumber numberWithInteger:FILE_PATH_FAI],self.key,self.value]];
            return;
        }
    }
    
    if(self.cahceType == FILE_PATH){
        self.value = [[NSCSVUtil new] findLocalPath:self.key andInit:NO];
        [self successWithMessage:@[[NSNumber numberWithInteger:self.cahceType]
                                   ,[NSNumber numberWithInteger:FILE_PATH_SUC],self.key,self.value,[NSNumber numberWithInteger:self.actionType]]];
        return;
    }
    if(self.key==nil){
        [self successWithMessage:@[[NSNumber numberWithInteger:self.cahceType]
                                   ,[NSNumber numberWithInteger:CACHE_KEY_NIL],self.key,self.value,[NSNumber numberWithInteger:self.actionType]]];
        return;
    }
    
    self.value = nil;
    if(command.arguments.count>2){
        self.value      = command.arguments[2];
        if(self.value == nil){
            [self successWithMessage:@[[NSNumber numberWithInteger:self.cahceType]
                                       ,[NSNumber numberWithInteger:CACHE_VALUE_NIL],self.key,self.value,[NSNumber numberWithInteger:self.actionType]]];
            return;
        }
    }
    
    if(command.arguments.count>3){
        self.actionType = [command.arguments[3] integerValue];
    }
    NSLog(@"--------*********存储key=%@",self.key);
    NSLog(@"--------*********存储value=%@",command.arguments[2]);
    NSLog(@"--------*********存储actionType=%ld",self.actionType);
    //开启线程
    [self startWork:self.cahceType andKey:self.key andValue:self.value andType:self.actionType];
}

-(void)startWork:(NSInteger)cacheType andKey:(NSString *)key andValue:(NSObject *)value andType:(NSInteger) actionType{
    __weak Caches *weakSelf = self;
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
            NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
            NSObject *obj = nil;
            if(cacheType == SAVE){
                if (actionType == JSON2CSV) {
                    [[NSCSVUtil new] exportCsv:key andArray:(NSArray *)value];
                } else if(actionType == DEFAULT) {
                    [defaults setObject:value forKey:key];
                    [defaults synchronize];
                }
                NSLog(@"存储成功");
            } else if (cacheType == READ) {
                if(actionType == CSV2JSON){
                    obj = [[NSCSVUtil new] inputFile:key];
                }else if(actionType == DEFAULT){
                    obj = [defaults objectForKey:key];
                }
                NSLog(@"读取成功");
            } else if (cacheType == REMOVE) {
                if (actionType == JSON2CSV) {
                    NSString* csvPathLocal = [[NSCSVUtil new] findLocalPath:key andInit:NO];
                    BOOL isExistLocal = [[NSFileManager defaultManager] fileExistsAtPath:csvPathLocal];
                    if (isExistLocal){
                        NSFileManager* fileSystem = [NSFileManager defaultManager];
                        [fileSystem removeItemAtPath:csvPathLocal error:nil];
                    }
                } else if (actionType == DEFAULT){
                    [defaults removeObjectForKey:key];
                    [defaults synchronize];
                }
                NSLog(@"删除成功");
            }
        
        [weakSelf successWithMessage:@[[NSNumber numberWithInteger:cacheType],[NSNumber numberWithInteger:CACHE_FINISH],key,obj==nil?value:obj,[NSNumber numberWithInteger:actionType]]];
    });
}

// 保存图片到相册成功
- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo{
    NSLog(@"error=%@", error);
}

//调出邮件发送窗口
- (void) displayMFMailComposeVC:(CDVInvokedUrlCommand *)command{
    NSString *fileName = command.arguments[1];
    NSString *address  = command.arguments[2];
    NSString *pass     = command.arguments[3];
    NSString *ip       = command.arguments[4];
    NSInteger port     = [command.arguments[5] integerValue];
    NSString *receive  = command.arguments[6];
    NSString *title    = command.arguments[7];
    NSString *content  = command.arguments[8];
    NSString *carbon   = command.arguments[9];
    MFMailComposeViewController *mailCompose = [[MFMailComposeViewController alloc] init];
    
    mailCompose.mailComposeDelegate = self;
    
    // 设置主题
    [mailCompose setSubject: title];
    
    // 添加收件人
    NSArray *toRecipients;
    if ([receive hasPrefix:@","]) {
        toRecipients = [receive componentsSeparatedByString:@","];
    } else {
        toRecipients = [NSArray arrayWithObjects:receive, nil];
    }
    [mailCompose setToRecipients: toRecipients];
    
    // 添加抄送
    NSArray *ccRecipients;
    if (carbon == nil || [@"nil" isEqualToString:carbon] || [@"null" isEqualToString:carbon]) {
        ccRecipients = [NSArray array];
    } else if ([carbon hasPrefix:@","]) {
        ccRecipients = [carbon componentsSeparatedByString:@","];
    } else {
        ccRecipients = [NSArray arrayWithObjects:carbon, nil];
    }
    [mailCompose setCcRecipients:ccRecipients];
    
    // 添加密送
    // NSArray *bccRecipients = [NSArray arrayWithObjects:@"密送人邮件1", nil];
    // [mailCompose setBccRecipients:bccRecipients];
    
    // 添加一张图片
    // UIImage *addPic = [UIImage imageNamed: @"图片"];
    // NSData *imageData = UIImagePNGRepresentation(addPic);// png
    // 关于mimeType：http://www.iana.org/assignments/media-types/
    // [mailCompose addAttachmentData:imageData mimeType:@"image" fileName:@"Icon.png"];
    //    application
    //    audio
    //    font
    //    example
    //    image
    //    message
    //    model
    //    multipart
    //    text
    //    video
    // 添加一个附件
    //NSData *pdf = [NSData dataWithContentsOfURL:[NSURL URLWithString:fileName]];
    // 关于mimeType：http://www.iana.org/assignments/media-types/
    //[mailCompose addAttachmentData:pdf mimeType:@"application" fileName:fileName];
    NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
    NSString *filePath = [cachePath stringByAppendingPathComponent: fileName];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if (![fileManager fileExistsAtPath:filePath]) {
        [self faileWithMessage:@"附件不存在"];
        return;
    }
    NSData *data = [NSData dataWithContentsOfURL:[NSURL fileURLWithPath:filePath]];
    [mailCompose addAttachmentData:data mimeType:@"application" fileName:fileName];
    //富文本为 isHTML：YES  字符串isHTML：NO
    NSString *emailBody = content;
    [mailCompose setMessageBody:emailBody isHTML:NO];
    [self.viewController presentViewController:mailCompose animated:NO completion:^{
        
    }];
}

- (void)mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error{
    //关闭邮件发送窗口
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
    NSString *msg;
    switch (result) {
        case MFMailComposeResultCancelled:
            //用户取消编辑邮件
            msg = @"取消发送邮件";
            break;
        case MFMailComposeResultSaved:
            //用户成功保存邮件
            msg = @"保存邮件成功";
            break;
        case MFMailComposeResultSent:
            //用户点击发送，将邮件放到队列中，还没发送
            msg = @"邮件发送中";
            break;
        case MFMailComposeResultFailed:
            //用户试图保存或者发送邮件失败
            msg = @"保存或者发送邮件失败";
            break;
        default:
            msg = @"";
            break;
    }
    if (msg.length) {
        NSLog(msg);
    }
}

- (void)messageSent:(SKPSMTPMessage *)message{
    [self successWithMessage:@[[NSNumber numberWithInteger:self.cahceType]
                               ,[NSNumber numberWithInteger:MAIL_SEND_SUCCESS],self.key,self.value,[NSNumber numberWithInteger:self.actionType]]];
}

- (void)messageFailed:(SKPSMTPMessage *)message error:(NSError *)error{
    [self faileWithMessage:@"邮件发送失败"];
}

/**
 * 系统分享
 */
- (void)shareItems:(NSArray *)items target:(id)target {
    if (items.count == 0 || target == nil) {
        return;
    }
    UIActivityViewController *activityVC = [[UIActivityViewController alloc] initWithActivityItems:items applicationActivities:nil];
    // UIActivityTypeMarkupAsPDF是在iOS 11.0 之后才有的
    if (@available(iOS 11.0, *)) {
        activityVC.excludedActivityTypes = @[UIActivityTypeMessage,UIActivityTypeMail,UIActivityTypeOpenInIBooks,UIActivityTypeMarkupAsPDF];
    // UIActivityTypeOpenInIBooks是在iOS 9.0 之后才有的
    } else if (@available(iOS 9.0, *)) {
        activityVC.excludedActivityTypes = @[UIActivityTypeMessage,UIActivityTypeMail,UIActivityTypeOpenInIBooks];
    }else {
        activityVC.excludedActivityTypes = @[UIActivityTypeMessage,UIActivityTypeMail];
    }
    activityVC.completionWithItemsHandler = ^(UIActivityType  _Nullable activityType, BOOL completed, NSArray * _Nullable returnedItems, NSError * _Nullable activityError) {
        
    };
    // 这儿一定要做iPhone与iPad的判断，因为这儿只有iPhone可以present，iPad需pop，所以这儿actVC.popoverPresentationController.sourceView = self.view;在iPad下必须有，不然iPad会crash，self.view你可以换成任何view，你可以理解为弹出的窗需要找个依托。
    UIViewController *vc = target;
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        activityVC.popoverPresentationController.sourceView = vc.view;
        [vc presentViewController:activityVC animated:YES completion:nil];
    } else {
        [vc presentViewController:activityVC animated:YES completion:nil];
    }
}

-(void)successWithMessage:(NSArray *)messages{
    if(self.callbackId==nil)return;
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:messages];
    [result setKeepCallbackAsBool:YES];
    NSLog(@"message=%@",messages);
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

-(void)faileWithMessage:(NSString *)message{
    if(self.callbackId==nil)return;
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

@end
