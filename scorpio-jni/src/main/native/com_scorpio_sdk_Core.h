/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_machloop_iosp_sdk_Core */

#ifndef _Included_com_machloop_iosp_sdk_Core
#define _Included_com_machloop_iosp_sdk_Core
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    globalInit
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_machloop_iosp_sdk_Core_globalInit
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    syncInit
 * Signature: (CLjava/lang/String;SLjava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_machloop_iosp_sdk_Core_syncInit
  (JNIEnv *, jclass, jchar, jstring, jshort, jstring, jstring);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    search
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;CIIZ)Lcom/machloop/iosp/sdk/CursorFullObjectResult;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_search
  (JNIEnv *, jclass, jlong, jobject, jchar, jint, jint, jboolean);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    searchMetadata
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;CII)Lcom/machloop/iosp/sdk/CursorMetadataResult;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_searchMetadata
  (JNIEnv *, jclass, jlong, jobject, jchar, jint, jint);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    searchAndWriteDisk
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_searchAndWriteDisk
  (JNIEnv *, jclass, jlong, jobject, jstring, jint);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    searchAnalys
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_machloop_iosp_sdk_Core_searchAnalys
  (JNIEnv *, jclass, jlong, jobject);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    read
 * Signature: (JLjava/lang/String;Z)Lcom/machloop/iosp/sdk/FullObject;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_read
  (JNIEnv *, jclass, jlong, jstring, jboolean);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    readMetadata
 * Signature: (JLjava/lang/String;)Lcom/machloop/iosp/sdk/Metadata;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_readMetadata
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    write
 * Signature: (JLcom/machloop/iosp/sdk/WriteFullObject;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_machloop_iosp_sdk_Core_write
  (JNIEnv *, jclass, jlong, jobject);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    modifyLabel
 * Signature: (JLjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_modifyLabel
  (JNIEnv *, jclass, jlong, jstring, jstring);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    createSubscribeTask
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;CJ)J
 */
JNIEXPORT jlong JNICALL Java_com_machloop_iosp_sdk_Core_createSubscribeTask
  (JNIEnv *, jclass, jlong, jobject, jchar, jlong);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    consumeSubscribeObject
 * Signature: (JJZ)Lcom/machloop/iosp/sdk/subscribe/SubscribeObjectResult;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_consumeSubscribeObject
  (JNIEnv *, jclass, jlong, jlong, jboolean);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    consumeSubscribeMetadata
 * Signature: (JJ)Lcom/machloop/iosp/sdk/subscribe/SubscribeMetadataResult;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_consumeSubscribeMetadata
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    destroySubscribe
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_destroySubscribe
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    resultRelease
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_resultRelease
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    destory
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_destory
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
