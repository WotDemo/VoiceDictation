# VoiceDictation
讯飞语音的一个小Demo

---
#### 自定义UI使用步骤

1. 去[讯飞开放平台](http://www.xfyun.cn/)下载好SDK

2. 将开发工具包中libs目录下的 Msc.jar,Sunflower.jar,armeab 拷贝到Android工程的jniLibs目录

3. 在build.gradle(Module:app)增加以下配置
```java
 buildTypes {
        sourceSets {
            main {
                jniLibs.srcDirs = ['libs']
            }
        }
    }

+implementation files('libs/Sunflower.jar')
+implementation files('libs/Msc.jar')
````

4. 配置权限
```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
```

5.添加[XfHelper](https://github.com/yangsanning/VoiceDictation/blob/master/app/src/main/java/ysn/com/voicedictation/helper/XfHelper.java)辅助类以及[JsonParser](https://github.com/yangsanning/VoiceDictation/blob/master/app/src/main/java/ysn/com/voicedictation/util/JsonParser.java)数据解析类

6. 将XfHelper类中的APP_ID替换成你在讯飞平台注册的id
```java
private static final String APP_ID = "5b1e2ec3";
```

7. 调用一下方法即可使用
```java
 XfHelper.get(this).setOnXfListener(new XfHelper.OnXfListener() {
 
            @Override
            public void onVolumeChanged(int volume) {
                
            }

            @Override
            public void onResult(String result) {

            }

            @Override
            public void onError(int code, String msg) {

            }

            @Override
            public void onEndOfSpeech() {

            }
        }).start();
```

8. 资源释放
```java
XfHelper.get(this).destroy();
```
