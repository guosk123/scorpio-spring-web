使用JNI方式调用libsdk.so

根据class文件生成JNI头文件：
```
javah -classpath . -jni -encoding UTF-8 com.machloop.iosp.sdk.Core
```

需编译src/main/native下的.c文件，将生成的libsdk_jni.so文件放到/lib64下，编译方法如下：  
```
gcc -I/opt/java/openjdk-8u222-b10/include/ -I/opt/java/openjdk-8u222-b10/include/linux -fPIC -c com_machloop_iosp_sdk_Core.c -lsdk -lpthread -std=c99
gcc -fpic -shared -Wl,-soname,libsdk_jni.so.1 -o libsdk_jni.so com_machloop_iosp_sdk_Core.o -lsdk -lpthread
```
