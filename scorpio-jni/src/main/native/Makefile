all:
	gcc -I/opt/java/openjdk-8u222-b10/include/ -I/opt/java/openjdk-8u222-b10/include/linux -fPIC -c com_machloop_iosp_sdk_Core.c -lsdk -lpthread
	gcc -fpic -shared -Wl,-soname,libsdk_jni.so.1 -o libsdk_jni.so com_machloop_iosp_sdk_Core.o -lsdk -lpthread
	cp libsdk_jni.so /lib64/libsdk_jni.so
#	mvn package -f /mnt/code/sdk-jni/ -Dmaven.test.skip=true
clean:
	rm -rf *.o libsdk_jni.so
