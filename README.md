## Caches插件使用说明
* 版本:1.3.4

## 环境配置
* npm 4.4.1 +
* node 9.8.0 +


## 使用流程
#### 注意:
###### ios平台,Mac系统下如果以下的控制台命令遇到权限问题，可以在命令前加sudo
###### 插件的读取动作时，请根据result中返回的key来判断返回的是哪个key对应的value。

###### 1.进入项目的根目录，添加Ping插件:com.chinamobile.cache.caches
* 为项目添加Pings插件，执行:`cordova plugin add com.chinamobile.cache.caches`
* 如果要删除插件,执行:`cordova plugin add com.chinamobile.cache.caches`
* 为项目添加对应的platform平台,已添加过，此步忽略，执行:
* 安卓平台: `cordova platform add android`
* ios 平台̨:`cordova platform add ios`
* 将插件添加到对应平台,执行: `cordova build`

###### 2.在js文件中,通过以下js方法调用插件，执行缓存操作
*
```javascript
    caches: function(){
        //缓存操作-----------------------------------------------------------------------------------------
        //向native发出缓存操作请求
        //success:成功的回调函数
        //error:失败的回调函数
        //Pings:插件名,固定值
        //coolMethod:插件方法，固定值
        //[0,"163.177.151.110",4]:插件方法参数，具体对应以下：
        //*元素1：插件动作cacheType 0:存储，1:读取，2:删除,3:获取文件的本地路径;4:打开文件；5:分享文件到微信/QQ 6:分享多个文件到微信和QQ
        //*元素2：key，缓存文件名
        //元素3: value，缓存数据：支持String,int,float,double,bool,long,JSONObject,JSONArray
       
        var person = {
            id:11,
            name:"小黑"
        }
        cordova.exec(success,error,"Caches","coolMethod",[0,"test_c",person]);
        
       
         //CSV操作-JSONArray转CSV文件存储------------------------------------------------------------------ 
         //[0,"person.csv",personArray,1]:插件方法参数，具体对应以下：
         //*元素1：插件动作cacheType 0:存储，1:读取，2:删除，3:获取文件的本地路径
         //*元素2：key，csv文件名
         //元素3: value，JSONArray数据
         //*元素4: 操作类型actionType 0:缓存操作 1:CSV操作-JSONArray转CSV文件存储 2:CSV操作-CSV文件转成JSONArray返回
         var personArray = [{id:11,name:"张三"},{id:12,name:"李四"}];
         cordova.exec(success,error,"Caches","coolMethod",[0,"person.csv",personArray,1]);
         
         
         //CSV操作-JSONArray转CSV文件存储------------------------------------------------------------------
         cordova.exec(success,error,"Caches","coolMethod",[1,"person.csv",null,2]);
         
         
         //获取CSV文件路径---------------------------------------------------------------------------------
         cordova.exec(success,error,"Caches","coolMethod",[3,"person.csv",null,0]);
    }
    
    success: function(var result){
        //缓存操作:array数组:[1,0,"test_c",{id:"11",name:"小黑"},0]
        //CSV操作-写:array数组:[0,0,"person.csv",{id:"11",name:"小黑"},1]
        //CSV操作-读:array数组:[1,0,"person.csv",{id:"11",name:"小黑"},2]
        //获取CSV文件路径:array数组:[3,0,"person.csv","/xx/xx/CSV/person.csv",0]
        
        //插件动作cacheType:0=存储,1=读取,2=删除，3=获取文件的本地路径
        var cacheType = result[0];
        
        //status:0=完成，1=key为null，2=value为null，3=存储的数据格式不支持，4=缓存中出错;
        var status = result[1];
        
        //存储的文件名
        var key        = result[2];
        
        //存储的String,int,float,double,bool,long,JSONObject,JSONArray数据,只有cacheType = 1时，才有值.
        var value      = result[3];
        
        //0=缓存操作，1=CSV操作-JSONArray转CSV文件存储， 2=CSV操作-CSV文件转成JSONArray返回
        var actionType = result[4];
    }

    error: function(var result){
        //测试异常提示
        alert(result);
    }
```

## 问题反馈
  在使用中有任何问题，可以用以下方式联系.
  * 作者:梁仲太
  * 邮件:18520660170@139.com
  * 时间:2018-8-29 18:49:00
