#include <stdio.h>
#include <stdint.h>
#include <string.h>

#include "machlake_sdk.h"
#include "com_machloop_iosp_sdk_Core.h"

jclass g_class_searchCondition;
jclass g_class_metadata;
jclass g_class_fullObject;
jclass g_class_cursorResult;
jclass g_class_cursorMetadataResult;
jclass g_class_writeFullObject;
jclass g_class_subscribeObjectResult;
jclass g_class_subscribeMetadataResult;

jclass g_class_byteBuffer;

jclass g_exception_IOException;
jclass g_exception_IllegalArgumentException;

jmethodID g_method_construction_metadata;
jmethodID g_method_construction_fullObject;
jmethodID g_method_construction_cursorResult;
jmethodID g_method_construction_cursorMetadataResult;
jmethodID g_method_construction_subscribeObjectResult;
jmethodID g_method_construction_subscribeMetadataResult;

jmethodID g_method_byteBuffer_warp;

jfieldID g_field_condition_start_time;
jfieldID g_field_condition_end_time;
jfieldID g_field_condition_time_out;
jfieldID g_field_condition_zone;
jfieldID g_field_condition_site;
jfieldID g_field_condition_label;

jfieldID g_field_metadata_create_time;
jfieldID g_field_metadata_ingest_time;
jfieldID g_field_metadata_object_size;
jfieldID g_field_metadata_object_id;
jfieldID g_field_metadata_object_name;
jfieldID g_field_metadata_zone;
jfieldID g_field_metadata_site;
jfieldID g_field_metadata_label;

jfieldID g_field_fullObject_create_time;
jfieldID g_field_fullObject_ingest_time;
jfieldID g_field_fullObject_object_size;
jfieldID g_field_fullObject_object_id;
jfieldID g_field_fullObject_object_name;
jfieldID g_field_fullObject_zone;
jfieldID g_field_fullObject_site;
jfieldID g_field_fullObject_label;
jfieldID g_field_fullObject_content;

jfieldID g_field_cursorResult_objects;
jfieldID g_field_cursorResult_msg;
jfieldID g_field_cursorResult_cursor;

jfieldID g_field_cursorMetadataResult_metadatas;
jfieldID g_field_cursorMetadataResult_msg;
jfieldID g_field_cursorMetadataResult_cursor;

jfieldID g_field_writeFullObject_object_name;
jfieldID g_field_writeFullObject_zone;
jfieldID g_field_writeFullObject_create_time;
jfieldID g_field_writeFullObject_site;
jfieldID g_field_writeFullObject_label;
jfieldID g_field_writeFullObject_content;

jfieldID g_field_subscribeObjectResult_objects;
jfieldID g_field_subscribeObjectResult_hasNext;

jfieldID g_field_subscribeMetadataResult_metadatas;
jfieldID g_field_subscribeMetadataResult_hasNext;

void init_java_class(JNIEnv *env){
  jclass local_class_searchCondition = (*env)->FindClass(env, "com/machloop/iosp/sdk/SearchCondition");
  jclass local_class_metadata = (*env)->FindClass(env, "com/machloop/iosp/sdk/Metadata");
  jclass local_class_fullObject = (*env)->FindClass(env, "com/machloop/iosp/sdk/FullObject");
  jclass local_class_cursorResult = (*env)->FindClass(env, "com/machloop/iosp/sdk/CursorFullObjectResult");
  jclass local_class_cursorMetadataResult = (*env)->FindClass(env, "com/machloop/iosp/sdk/CursorMetadataResult");
  jclass local_class_writefullObject = (*env)->FindClass(env, "com/machloop/iosp/sdk/WriteFullObject");
  jclass local_class_subscribeObjectResult = (*env)->FindClass(env, "com/machloop/iosp/sdk/subscribe/SubscribeObjectResult");
  jclass local_class_subscribeMetadataResult = (*env)->FindClass(env, "com/machloop/iosp/sdk/subscribe/SubscribeMetadataResult");

  jclass local_class_byteBuffer = (*env)->FindClass(env, "java/nio/ByteBuffer");
  
  g_class_searchCondition = (jclass)(*env)->NewGlobalRef(env, local_class_searchCondition);
  g_class_metadata = (jclass)(*env)->NewGlobalRef(env, local_class_metadata);
  g_class_fullObject = (jclass)(*env)->NewGlobalRef(env, local_class_fullObject);
  g_class_cursorResult = (jclass)(*env)->NewGlobalRef(env, local_class_cursorResult);
  g_class_cursorMetadataResult = (jclass)(*env)->NewGlobalRef(env, local_class_cursorMetadataResult);
  g_class_writeFullObject = (jclass)(*env)->NewGlobalRef(env, local_class_writefullObject);
  g_class_subscribeObjectResult = (jclass)(*env)->NewGlobalRef(env, local_class_subscribeObjectResult);
  g_class_subscribeMetadataResult = (jclass)(*env)->NewGlobalRef(env, local_class_subscribeMetadataResult);

  g_class_byteBuffer = (jclass)(*env)->NewGlobalRef(env, local_class_byteBuffer);

  (*env)->DeleteLocalRef(env, local_class_searchCondition);
  (*env)->DeleteLocalRef(env, local_class_metadata);
  (*env)->DeleteLocalRef(env, local_class_fullObject);
  (*env)->DeleteLocalRef(env, local_class_cursorResult);
  (*env)->DeleteLocalRef(env, local_class_cursorMetadataResult);
  (*env)->DeleteLocalRef(env, local_class_writefullObject);
  (*env)->DeleteLocalRef(env, local_class_subscribeObjectResult);
  (*env)->DeleteLocalRef(env, local_class_subscribeMetadataResult);
  (*env)->DeleteLocalRef(env, local_class_byteBuffer);
}

void init_exception_class(JNIEnv *env){
  jclass IOException = (*env)->FindClass(env, "java/io/IOException");
  jclass IllegalArgumentException = (*env)->FindClass(env, "java/lang/IllegalArgumentException");

  g_exception_IOException = (jclass)(*env)->NewGlobalRef(env, IOException);
  g_exception_IllegalArgumentException  = (jclass)(*env)->NewGlobalRef(env, IllegalArgumentException);

  (*env)->DeleteLocalRef(env, IOException);
  (*env)->DeleteLocalRef(env, IllegalArgumentException);
}

void init_java_method(JNIEnv *env){
  g_method_construction_metadata = (*env)->GetMethodID(env, g_class_metadata, "<init>", "()V");
  g_method_construction_fullObject = (*env)->GetMethodID(env, g_class_fullObject, "<init>", "()V");
  g_method_construction_cursorResult = (*env)->GetMethodID(env, g_class_cursorResult, "<init>", "()V");
  g_method_construction_cursorMetadataResult = (*env)->GetMethodID(env, g_class_cursorMetadataResult, "<init>", "()V");
  g_method_construction_subscribeObjectResult = (*env)->GetMethodID(env, g_class_subscribeObjectResult, "<init>", "()V");
  g_method_construction_subscribeMetadataResult = (*env)->GetMethodID(env, g_class_subscribeMetadataResult, "<init>", "()V");

  g_method_byteBuffer_warp = (*env)->GetStaticMethodID(env, g_class_byteBuffer, "wrap", "([B)Ljava/nio/ByteBuffer;");
}

void init_java_field(JNIEnv *env){
  g_field_condition_start_time = (*env)->GetFieldID(env, g_class_searchCondition, "startTime", "I");
  g_field_condition_end_time = (*env)->GetFieldID(env, g_class_searchCondition, "endTime", "I");
  g_field_condition_time_out = (*env)->GetFieldID(env, g_class_searchCondition, "timeout", "I");
  g_field_condition_zone = (*env)->GetFieldID(env, g_class_searchCondition, "zone", "Ljava/lang/String;");
  g_field_condition_site = (*env)->GetFieldID(env, g_class_searchCondition, "site", "Ljava/lang/String;");
  g_field_condition_label = (*env)->GetFieldID(env, g_class_searchCondition, "label", "Ljava/lang/String;");

  g_field_metadata_create_time = (*env)->GetFieldID(env, g_class_metadata, "createTime", "I");
  g_field_metadata_ingest_time = (*env)->GetFieldID(env, g_class_metadata, "ingestTime", "I");
  g_field_metadata_object_size = (*env)->GetFieldID(env, g_class_metadata, "objectSize", "I");
  g_field_metadata_object_id = (*env)->GetFieldID(env, g_class_metadata, "objectId", "Ljava/lang/String;");
  g_field_metadata_object_name = (*env)->GetFieldID(env, g_class_metadata, "objectName", "Ljava/lang/String;");
  g_field_metadata_zone = (*env)->GetFieldID(env, g_class_metadata, "zone", "Ljava/lang/String;");
  g_field_metadata_site = (*env)->GetFieldID(env, g_class_metadata, "site", "Ljava/lang/String;");
  g_field_metadata_label = (*env)->GetFieldID(env, g_class_metadata, "label", "Ljava/lang/String;");

  g_field_fullObject_create_time = (*env)->GetFieldID(env, g_class_fullObject, "createTime", "I");
  g_field_fullObject_ingest_time = (*env)->GetFieldID(env, g_class_fullObject, "ingestTime", "I");
  g_field_fullObject_object_size = (*env)->GetFieldID(env, g_class_fullObject, "objectSize", "I");
  g_field_fullObject_object_id = (*env)->GetFieldID(env, g_class_fullObject, "objectId", "Ljava/lang/String;");
  g_field_fullObject_object_name = (*env)->GetFieldID(env, g_class_fullObject, "objectName", "Ljava/lang/String;");
  g_field_fullObject_zone = (*env)->GetFieldID(env, g_class_fullObject, "zone", "Ljava/lang/String;");
  g_field_fullObject_site = (*env)->GetFieldID(env, g_class_fullObject, "site", "Ljava/lang/String;");
  g_field_fullObject_label = (*env)->GetFieldID(env, g_class_fullObject, "label", "Ljava/lang/String;");
  g_field_fullObject_content = (*env)->GetFieldID(env, g_class_fullObject, "content", "Ljava/nio/ByteBuffer;");

  g_field_cursorResult_objects = (*env)->GetFieldID(env, g_class_cursorResult, "objects", "[Lcom/machloop/iosp/sdk/FullObject;");
  g_field_cursorResult_msg = (*env)->GetFieldID(env, g_class_cursorResult, "datanodeMsg", "Ljava/lang/String;");
  g_field_cursorResult_cursor = (*env)->GetFieldID(env, g_class_cursorResult, "cursor", "I");

  g_field_cursorMetadataResult_metadatas = (*env)->GetFieldID(env, g_class_cursorMetadataResult, "metadatas", "[Lcom/machloop/iosp/sdk/Metadata;");
  g_field_cursorMetadataResult_msg = (*env)->GetFieldID(env, g_class_cursorMetadataResult, "datanodeMsg", "Ljava/lang/String;");
  g_field_cursorMetadataResult_cursor = (*env)->GetFieldID(env, g_class_cursorMetadataResult, "cursor", "I");

  g_field_writeFullObject_object_name = (*env)->GetFieldID(env, g_class_writeFullObject, "objectName", "Ljava/lang/String;");
  g_field_writeFullObject_zone = (*env)->GetFieldID(env, g_class_writeFullObject, "zone", "Ljava/lang/String;");
  g_field_writeFullObject_create_time = (*env)->GetFieldID(env, g_class_writeFullObject, "createTime", "I");
  g_field_writeFullObject_site = (*env)->GetFieldID(env, g_class_writeFullObject, "site", "Ljava/lang/String;");
  g_field_writeFullObject_label = (*env)->GetFieldID(env, g_class_writeFullObject, "label", "Ljava/lang/String;");
  g_field_writeFullObject_content = (*env)->GetFieldID(env, g_class_writeFullObject, "content", "Ljava/nio/ByteBuffer;");

  g_field_subscribeObjectResult_objects = (*env)->GetFieldID(env, g_class_subscribeObjectResult, "objects", "[Lcom/machloop/iosp/sdk/FullObject;");
  g_field_subscribeObjectResult_hasNext = (*env)->GetFieldID(env, g_class_subscribeObjectResult, "hasNext", "I");

  g_field_subscribeMetadataResult_metadatas = (*env)->GetFieldID(env, g_class_subscribeMetadataResult, "metadatas", "[Lcom/machloop/iosp/sdk/Metadata;");
  g_field_subscribeMetadataResult_hasNext = (*env)->GetFieldID(env, g_class_subscribeMetadataResult, "hasNext", "I");
}

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    globalInit
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_machloop_iosp_sdk_Core_globalInit
  (JNIEnv *env, jclass clazz, jstring log_path){
    const char *str = log_path==NULL? NULL:(*env)->GetStringUTFChars(env, log_path, 0);
    int ret = iosp_global_init((char*) str);
    // free
    (*env)->ReleaseStringUTFChars(env, log_path, str);
  
    init_java_class(env);
    init_exception_class(env);
    init_java_method(env);
    init_java_field(env);

    return ret;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    syncInit
 * Signature: (CLjava/lang/String;SLjava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_machloop_iosp_sdk_Core_syncInit
  (JNIEnv *env, jclass clazz, jchar mode, jstring serverIp, jshort serverPort, jstring clientId, jstring clientToken){
    const char *pcServerIp = (*env)->GetStringUTFChars(env, serverIp, 0);
    const char *pcClientId = (*env)->GetStringUTFChars(env, clientId, 0);
    const char *pcToken = (*env)->GetStringUTFChars(env, clientToken, 0);
    long pLake = (long) iosp_sync_init(mode, (char*)pcServerIp, serverPort, (char*)pcClientId, (char*)pcToken);
    
    // free
    (*env)->ReleaseStringUTFChars(env, serverIp, pcServerIp);
    (*env)->ReleaseStringUTFChars(env, clientId, pcClientId);
    (*env)->ReleaseStringUTFChars(env, clientToken, pcToken);

    if(pLake == 0){
      int errorCode = iosp_get_error_code();
      const char* msg = iosp_get_error(errorCode);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return 0;
    }
    return pLake;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    search
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;CII)Lcom/machloop/iosp/sdk/CursorResult;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_search
  (JNIEnv *env, jclass clazz, jlong pLake, jobject searchCondition, jchar sortType, jint searchNum, jint cursor, jboolean useDirectByteBuffer){
    void* p = (void*) pLake;

    struct metadata_search_tag search_tag;

    int start_time = (*env)->GetIntField(env, searchCondition, g_field_condition_start_time);
    int end_time = (*env)->GetIntField(env, searchCondition, g_field_condition_end_time);
    int time_out = (*env)->GetIntField(env, searchCondition, g_field_condition_time_out);

    search_tag.start_time = start_time;
    search_tag.end_time = end_time;
    search_tag.time_out = time_out;

    jstring zoneStr = (jstring)(*env)->GetObjectField(env, searchCondition, g_field_condition_zone);
    jstring siteStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_site);
    jstring labelStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_label);

    const char *zone = (*env)->GetStringUTFChars(env, zoneStr, 0);
    strcpy(search_tag.zone, zone);
    (*env)->ReleaseStringUTFChars(env, zoneStr, zone);
    
    if(siteStr!=NULL){
      const char *site = (*env)->GetStringUTFChars(env, siteStr, 0);
      strcpy(search_tag.site, site);
      (*env)->ReleaseStringUTFChars(env, siteStr, site);
    }else{
      char* site = "\0";
      strcpy(search_tag.site, site);
    }

    if(labelStr!=NULL){
      const char *label = (*env)->GetStringUTFChars(env, labelStr, 0);
      strcpy(search_tag.label, label);
      (*env)->ReleaseStringUTFChars(env, labelStr, label);
    }else{
      char* label = "\0";
      strcpy(search_tag.label, label);
    }

    metadata **ppstMetadataArray;
    char *pcDnMsg;

    int ret = iosp_search_object(p, &search_tag, sortType, &searchNum, &cursor, &ppstMetadataArray, &pcDnMsg);
    if(ret != 0){
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return NULL;
    }

    // 结果
    jobjectArray resultArray = (*env)->NewObjectArray(env, searchNum, g_class_fullObject, 0);
    if(useDirectByteBuffer){
      for(int i=0;i<searchNum;i++){
        jobject fullObject =  (*env)->NewObject(env, g_class_fullObject, g_method_construction_cursorResult);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_create_time, ppstMetadataArray[i]->create_time);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_ingest_time, ppstMetadataArray[i]->ingest_time);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_object_size, ppstMetadataArray[i]->object_size);
        // object id
        jstring objectId = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_id);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_id, objectId);
        // object name
        jstring objectName = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_name);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_name, objectName);
        // zone
        jstring zone = (*env)->NewStringUTF(env, ppstMetadataArray[i]->zone);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_zone, zone);
        // site
        jstring site = (*env)->NewStringUTF(env, ppstMetadataArray[i]->site);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_site, site);      
        // label
        jstring label = (*env)->NewStringUTF(env, ppstMetadataArray[i]->label);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_label, label);      
        // object
        if(ppstMetadataArray[i]->object_size!=0xFFFFFFFF){ // 大文件
          jobject contentByteBuffer = (*env)->NewDirectByteBuffer(env, ppstMetadataArray[i]->object, ppstMetadataArray[i]->object_size);
          (*env)->SetObjectField(env, fullObject, g_field_fullObject_content, contentByteBuffer);
        }

        (*env)->SetObjectArrayElement(env, resultArray, i, fullObject);
      }
    }else{
      for(int i=0;i<searchNum;i++){
        jobject fullObject =  (*env)->NewObject(env, g_class_fullObject, g_method_construction_cursorResult);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_create_time, ppstMetadataArray[i]->create_time);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_ingest_time, ppstMetadataArray[i]->ingest_time);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_object_size, ppstMetadataArray[i]->object_size);
        // object id
        jstring objectId = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_id);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_id, objectId);
        // object name
        jstring objectName = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_name);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_name, objectName);
        // zone
        jstring zone = (*env)->NewStringUTF(env, ppstMetadataArray[i]->zone);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_zone, zone);
        // site
        jstring site = (*env)->NewStringUTF(env, ppstMetadataArray[i]->site);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_site, site);      
        // label
        jstring label = (*env)->NewStringUTF(env, ppstMetadataArray[i]->label);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_label, label);      
        // object
        if(ppstMetadataArray[i]->object_size!=0xFFFFFFFF){ // 大文件
          jbyteArray array = (*env)->NewByteArray(env, ppstMetadataArray[i]->object_size);
          (*env)->SetByteArrayRegion(env, array, 0, ppstMetadataArray[i]->object_size, ppstMetadataArray[i]->object);
          jobject contentByteBuffer = (*env)->CallStaticObjectMethod(env, g_class_byteBuffer, g_method_byteBuffer_warp, array);
          (*env)->SetObjectField(env, fullObject, g_field_fullObject_content, contentByteBuffer);
        }

        (*env)->SetObjectArrayElement(env, resultArray, i, fullObject);
      }
    }

    jobject resultObject =  (*env)->NewObject(env, g_class_cursorResult, g_method_construction_cursorResult);
    (*env)->SetIntField(env, resultObject, g_field_cursorResult_cursor, cursor);
    (*env)->SetObjectField(env, resultObject, g_field_cursorResult_objects, resultArray);
    jstring datanodeMsg = (*env)->NewStringUTF(env, pcDnMsg);
    (*env)->SetObjectField(env, resultObject, g_field_cursorResult_msg, datanodeMsg);    

    return resultObject;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    searchMetadata
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;CII)Lcom/machloop/iosp/sdk/CursorMetadataResult;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_searchMetadata
  (JNIEnv *env, jclass clazz, jlong pLake, jobject searchCondition, jchar sortType, jint searchNum, jint cursor){
    void* p = (void*) pLake;

    struct metadata_search_tag search_tag;

    int start_time = (*env)->GetIntField(env, searchCondition, g_field_condition_start_time);
    int end_time = (*env)->GetIntField(env, searchCondition, g_field_condition_end_time);
    int time_out = (*env)->GetIntField(env, searchCondition, g_field_condition_time_out);

    search_tag.start_time = start_time;
    search_tag.end_time = end_time;
    search_tag.time_out = time_out;

    jstring zoneStr = (jstring)(*env)->GetObjectField(env, searchCondition, g_field_condition_zone);
    jstring siteStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_site);
    jstring labelStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_label);

    const char *zone = (*env)->GetStringUTFChars(env, zoneStr, 0);
    strcpy(search_tag.zone, zone);
    (*env)->ReleaseStringUTFChars(env, zoneStr, zone);
    
    if(siteStr!=NULL){
      const char *site = (*env)->GetStringUTFChars(env, siteStr, 0);
      strcpy(search_tag.site, site);
      (*env)->ReleaseStringUTFChars(env, siteStr, site);
    }else{
      char* site = "\0";
      strcpy(search_tag.site, site);
    }

    if(labelStr!=NULL){
      const char *label = (*env)->GetStringUTFChars(env, labelStr, 0);
      strcpy(search_tag.label, label);
      (*env)->ReleaseStringUTFChars(env, labelStr, label);
    }else{
      char* label = "\0";
      strcpy(search_tag.label, label);
    }

    metadata **ppstMetadataArray;
    char *pcDnMsg;

    int ret = iosp_search_metadata(p, &search_tag, sortType, &searchNum, &cursor, &ppstMetadataArray, &pcDnMsg);
    if(ret != 0){
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return NULL;
    }

    // 结果
    jobjectArray resultArray = (*env)->NewObjectArray(env, searchNum, g_class_metadata, 0);

    for(int i=0;i<searchNum;i++){
      jobject o_metadata =  (*env)->NewObject(env, g_class_metadata, g_method_construction_metadata);
      (*env)->SetIntField(env, o_metadata, g_field_metadata_create_time, ppstMetadataArray[i]->create_time);
      (*env)->SetIntField(env, o_metadata, g_field_metadata_ingest_time, ppstMetadataArray[i]->ingest_time);
      (*env)->SetIntField(env, o_metadata, g_field_metadata_object_size, ppstMetadataArray[i]->object_size);
      // object id
      jstring objectIdResult = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_id);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_object_id, objectIdResult);
      // object name
      jstring objectName = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_name);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_object_name, objectName);
      // zone
      jstring zone = (*env)->NewStringUTF(env, ppstMetadataArray[i]->zone);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_zone, zone);
      // site
      jstring site = (*env)->NewStringUTF(env, ppstMetadataArray[i]->site);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_site, site);      
      // label
      jstring label = (*env)->NewStringUTF(env, ppstMetadataArray[i]->label);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_label, label);      

      (*env)->SetObjectArrayElement(env, resultArray, i, o_metadata);
    }

    jobject resultObject =  (*env)->NewObject(env, g_class_cursorMetadataResult, g_method_construction_cursorMetadataResult);
    (*env)->SetIntField(env, resultObject, g_field_cursorMetadataResult_cursor, cursor);
    (*env)->SetObjectField(env, resultObject, g_field_cursorMetadataResult_metadatas, resultArray);
    jstring datanodeMsg = (*env)->NewStringUTF(env, pcDnMsg);
    (*env)->SetObjectField(env, resultObject, g_field_cursorMetadataResult_msg, datanodeMsg);    

    return resultObject;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    searchAndWriteDisk
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_searchAndWriteDisk
  (JNIEnv *env, jclass clazz, jlong pLake, jobject searchCondition, jstring savePath, jint dirFileNum){
    if(savePath==NULL){
      (*env)->ThrowNew(env, g_exception_IllegalArgumentException, "savePath could not be null.");
      return;
    }

    void* p = (void*) pLake;

    struct metadata_search_tag search_tag;

    int start_time = (*env)->GetIntField(env, searchCondition, g_field_condition_start_time);
    int end_time = (*env)->GetIntField(env, searchCondition, g_field_condition_end_time);
    int time_out = (*env)->GetIntField(env, searchCondition, g_field_condition_time_out);

    search_tag.start_time = start_time;
    search_tag.end_time = end_time;
    search_tag.time_out = time_out;

    jstring zoneStr = (jstring)(*env)->GetObjectField(env, searchCondition, g_field_condition_zone);
    jstring siteStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_site);
    jstring labelStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_label);

    const char *zone = (*env)->GetStringUTFChars(env, zoneStr, 0);
    strcpy(search_tag.zone, zone);
    (*env)->ReleaseStringUTFChars(env, zoneStr, zone);
    
    if(siteStr!=NULL){
      const char *site = (*env)->GetStringUTFChars(env, siteStr, 0);
      strcpy(search_tag.site, site);
      (*env)->ReleaseStringUTFChars(env, siteStr, site);
    }else{
      char* site = "\0";
      strcpy(search_tag.site, site);
    }

    if(labelStr!=NULL){
      const char *label = (*env)->GetStringUTFChars(env, labelStr, 0);
      strcpy(search_tag.label, label);
      (*env)->ReleaseStringUTFChars(env, labelStr, label);
    }else{
      char* label = "\0";
      strcpy(search_tag.label, label);
    }

    const char *pcSavePath = (*env)->GetStringUTFChars(env, savePath, 0);

    int ret = iosp_search_object_write_disk(p, &search_tag, (char*)pcSavePath, dirFileNum);

    if(ret != 0){
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
    }
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    searchAnalys
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_machloop_iosp_sdk_Core_searchAnalys
  (JNIEnv *env, jclass clazz, jlong pLake, jobject searchCondition){
    void* p = (void*) pLake;

    struct metadata_search_tag search_tag;

    int start_time = (*env)->GetIntField(env, searchCondition, g_field_condition_start_time);
    int end_time = (*env)->GetIntField(env, searchCondition, g_field_condition_end_time);
    int time_out = (*env)->GetIntField(env, searchCondition, g_field_condition_time_out);

    search_tag.start_time = start_time;
    search_tag.end_time = end_time;
    search_tag.time_out = time_out;

    jstring zoneStr = (jstring)(*env)->GetObjectField(env, searchCondition, g_field_condition_zone);
    jstring siteStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_site);
    jstring labelStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_label);

    const char *zone = (*env)->GetStringUTFChars(env, zoneStr, 0);
    strcpy(search_tag.zone, zone);
    (*env)->ReleaseStringUTFChars(env, zoneStr, zone);
    
    if(siteStr!=NULL){
      const char *site = (*env)->GetStringUTFChars(env, siteStr, 0);
      strcpy(search_tag.site, site);
      (*env)->ReleaseStringUTFChars(env, siteStr, site);
    }else{
      char* site = "\0";
      strcpy(search_tag.site, site);
    }

    if(labelStr!=NULL){
      const char *label = (*env)->GetStringUTFChars(env, labelStr, 0);
      strcpy(search_tag.label, label);
      (*env)->ReleaseStringUTFChars(env, labelStr, label);
    }else{
      char* label = "\0";
      strcpy(search_tag.label, label);
    }

    char *pcAnalysResult;
    char *pcDnMsg;

    int ret = iosp_search_analys(p, &search_tag, &pcAnalysResult, &pcDnMsg);
    if(ret != 0){
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return NULL;
    }

    jclass string_class = (*env)->FindClass(env, "java/lang/String");
    jobjectArray resultArray = (*env)->NewObjectArray(env, 2, string_class, 0);

    jstring analysMsg = (*env)->NewStringUTF(env, pcAnalysResult);
    jstring datanodeMsg = (*env)->NewStringUTF(env, pcDnMsg);

    (*env)->SetObjectArrayElement(env, resultArray, 0, analysMsg);
    (*env)->SetObjectArrayElement(env, resultArray, 1, datanodeMsg);

    return resultArray;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    read
 * Signature: (JLjava/lang/String;)Lcom/machloop/iosp/sdk/FullObject;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_read
  (JNIEnv *env, jclass clazz, jlong pLake, jstring objectId, jboolean useDirectByteBuffer){
    if(objectId==NULL){
      (*env)->ThrowNew(env, g_exception_IllegalArgumentException, "object id could not be null.");
      return NULL;
    }

    void* p = (void*) pLake;
    const char *objectIdStr = (*env)->GetStringUTFChars(env, objectId, 0);

    void *buffer;
    metadata *pstMetadata;
    
    int ret = iosp_read_object(p, (char*)objectIdStr, &buffer);

    // free
    (*env)->ReleaseStringUTFChars(env, objectId, objectIdStr);

    if(ret < 0) {
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return NULL;
    }
      
    pstMetadata = (metadata *)buffer;

    jobject fullObject =  (*env)->NewObject(env, g_class_fullObject, g_method_construction_fullObject);
    (*env)->SetIntField(env, fullObject, g_field_fullObject_create_time, pstMetadata->create_time);
    (*env)->SetIntField(env, fullObject, g_field_fullObject_ingest_time, pstMetadata->ingest_time);
    (*env)->SetIntField(env, fullObject, g_field_fullObject_object_size, pstMetadata->object_size);
    // object id
    jstring objectIdResult = (*env)->NewStringUTF(env, pstMetadata->object_id);
    (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_id, objectIdResult);
    // object name
    jstring objectName = (*env)->NewStringUTF(env, pstMetadata->object_name);
    (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_name, objectName);
    // zone
    jstring zone = (*env)->NewStringUTF(env, pstMetadata->zone);
    (*env)->SetObjectField(env, fullObject, g_field_fullObject_zone, zone);
    // site
    jstring site = (*env)->NewStringUTF(env, pstMetadata->site);
    (*env)->SetObjectField(env, fullObject, g_field_fullObject_site, site);      
    // label
    jstring label = (*env)->NewStringUTF(env, pstMetadata->label);
    (*env)->SetObjectField(env, fullObject, g_field_fullObject_label, label);      
    // object
    jobject contentByteBuffer; 

    if(pstMetadata->object_size!=0xFFFFFFFF){ // 大文件
      if(useDirectByteBuffer){
        contentByteBuffer = (*env)->NewDirectByteBuffer(env, pstMetadata->object, pstMetadata->object_size);
      }else{
        jbyteArray array = (*env)->NewByteArray(env, pstMetadata->object_size);
        (*env)->SetByteArrayRegion(env, array, 0, pstMetadata->object_size, pstMetadata->object);
        contentByteBuffer = (*env)->CallStaticObjectMethod(env, g_class_byteBuffer, g_method_byteBuffer_warp, array);
      }
      (*env)->SetObjectField(env, fullObject, g_field_fullObject_content, contentByteBuffer);
    }

    return fullObject;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    readMetadata
 * Signature: (JLjava/lang/String;)Lcom/machloop/iosp/sdk/Metadata;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_readMetadata
  (JNIEnv *env, jclass clazz, jlong pLake, jstring objectId){
    if(objectId==NULL){
      (*env)->ThrowNew(env, g_exception_IllegalArgumentException, "object id could not be null.");
      return NULL;
    }

    void* p = (void*) pLake;
    const char *objectIdStr = (*env)->GetStringUTFChars(env, objectId, 0);

    void *buffer;
    metadata *pstMetadata;
    
    int ret = iosp_read_metadata(p, (char*)objectIdStr, &buffer);

    // free
    (*env)->ReleaseStringUTFChars(env, objectId, objectIdStr);

    if(ret < 0) {
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return NULL;
    }
      
    pstMetadata = (metadata *)buffer;

    jobject o_metadata =  (*env)->NewObject(env, g_class_metadata, g_method_construction_metadata);
    (*env)->SetIntField(env, o_metadata, g_field_metadata_create_time, pstMetadata->create_time);
    (*env)->SetIntField(env, o_metadata, g_field_metadata_ingest_time, pstMetadata->ingest_time);
    (*env)->SetIntField(env, o_metadata, g_field_metadata_object_size, pstMetadata->object_size);
    // object id
    jstring objectIdResult = (*env)->NewStringUTF(env, pstMetadata->object_id);
    (*env)->SetObjectField(env, o_metadata, g_field_fullObject_object_id, objectIdResult);
    // object name
    jstring objectName = (*env)->NewStringUTF(env, pstMetadata->object_name);
    (*env)->SetObjectField(env, o_metadata, g_field_fullObject_object_name, objectName);
    // zone
    jstring zone = (*env)->NewStringUTF(env, pstMetadata->zone);
    (*env)->SetObjectField(env, o_metadata, g_field_fullObject_zone, zone);
    // site
    jstring site = (*env)->NewStringUTF(env, pstMetadata->site);
    (*env)->SetObjectField(env, o_metadata, g_field_fullObject_site, site);      
    // label
    jstring label = (*env)->NewStringUTF(env, pstMetadata->label);
    (*env)->SetObjectField(env, o_metadata, g_field_fullObject_label, label);      

    return o_metadata;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    write
 * Signature: (JLcom/machloop/iosp/sdk/WriteFullObject;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_machloop_iosp_sdk_Core_write
  (JNIEnv *env, jclass clazz, jlong pLake, jobject writeFullObject){
    void* p = (void*) pLake;

    struct metadata_tag pstMetadata;

    int createTime = (*env)->GetIntField(env, writeFullObject, g_field_writeFullObject_create_time);
    pstMetadata.create_time = createTime;

    jstring objectNameStr = (jstring)(*env)->GetObjectField(env, writeFullObject, g_field_writeFullObject_object_name);
    const char *objectName = (*env)->GetStringUTFChars(env, objectNameStr, 0);
    strcpy(pstMetadata.object_name, objectName);
    (*env)->ReleaseStringUTFChars(env, objectNameStr, objectName);
    
    jstring zoneStr = (jstring)(*env)->GetObjectField(env, writeFullObject, g_field_writeFullObject_zone);
    const char *zone = (*env)->GetStringUTFChars(env, zoneStr, 0);
    strcpy(pstMetadata.zone, zone);
    (*env)->ReleaseStringUTFChars(env, zoneStr, zone);

    jstring siteStr = (jstring)(*env)->GetObjectField(env, writeFullObject, g_field_writeFullObject_site);
    if(siteStr!=NULL){
      const char *site = (*env)->GetStringUTFChars(env, siteStr, 0);
      strcpy(pstMetadata.site, site);
      (*env)->ReleaseStringUTFChars(env, siteStr, site);
    }else{
      char* site = "\0";
      strcpy(pstMetadata.site, site);
    }
    
    jstring labelStr = (jstring)(*env)->GetObjectField(env, writeFullObject, g_field_writeFullObject_label);
    if(labelStr!=NULL){
      const char *label = (*env)->GetStringUTFChars(env, labelStr, 0);
      strcpy(pstMetadata.label, label);
      (*env)->ReleaseStringUTFChars(env, labelStr, label);
    }else{
      char* label = "\0";
      strcpy(pstMetadata.label, label);
    }

    jobject contentByteBuffer = (*env)->GetObjectField(env, writeFullObject, g_field_writeFullObject_content);
    pstMetadata.object = (char*)(*env)->GetDirectBufferAddress(env, contentByteBuffer);
    pstMetadata.object_size = (*env)->GetDirectBufferCapacity(env, contentByteBuffer);

    int objectIdLen = 128;
    char objectId[objectIdLen];

    int ret = iosp_write_object(p, &pstMetadata, objectId, objectIdLen);
    if(ret < 0) {
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return NULL;
    }

    return (*env)->NewStringUTF(env, objectId);
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    modifyLabel
 * Signature: (JLjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_modifyLabel
  (JNIEnv *env, jclass clazz, jlong pLake, jstring objectId, jstring label){
    if(objectId==NULL || label==NULL){
      (*env)->ThrowNew(env, g_exception_IllegalArgumentException, "object id or label could not be null.");
      return;
    }
    void* p = (void*) pLake;
    const char *objectIdStr = (*env)->GetStringUTFChars(env, objectId, 0);
    const char *labelStr = (*env)->GetStringUTFChars(env, label, 0);

    int ret = iosp_modify_label(p, (char*)objectIdStr, (char*)labelStr);
    // free
    (*env)->ReleaseStringUTFChars(env, objectId, objectIdStr);
    (*env)->ReleaseStringUTFChars(env, label, labelStr);

    if(ret < 0) {
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return;
    }
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    createSubscribeTask
 * Signature: (JLcom/machloop/iosp/sdk/SearchCondition;CJ)J
 */
JNIEXPORT jlong JNICALL Java_com_machloop_iosp_sdk_Core_createSubscribeTask
  (JNIEnv *env, jclass clazz, jlong pLake, jobject searchCondition, jchar type, jlong taskId){
    void* p = (void*) pLake;

    struct metadata_search_tag search_tag;

    int start_time = (*env)->GetIntField(env, searchCondition, g_field_condition_start_time);
    int end_time = (*env)->GetIntField(env, searchCondition, g_field_condition_end_time);
    int time_out = (*env)->GetIntField(env, searchCondition, g_field_condition_time_out);

    search_tag.start_time = start_time;
    search_tag.end_time = end_time;
    search_tag.time_out = time_out;

    jstring zoneStr = (jstring)(*env)->GetObjectField(env, searchCondition, g_field_condition_zone);
    jstring siteStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_site);
    jstring labelStr = (jstring) (*env)->GetObjectField(env, searchCondition, g_field_condition_label);

    const char *zone = (*env)->GetStringUTFChars(env, zoneStr, 0);
    strcpy(search_tag.zone, zone);
    (*env)->ReleaseStringUTFChars(env, zoneStr, zone);
    
    if(siteStr!=NULL){
      const char *site = (*env)->GetStringUTFChars(env, siteStr, 0);
      strcpy(search_tag.site, site);
      (*env)->ReleaseStringUTFChars(env, siteStr, site);
    }else{
      char* site = "\0";
      strcpy(search_tag.site, site);
    }

    if(labelStr!=NULL){
      const char *label = (*env)->GetStringUTFChars(env, labelStr, 0);
      strcpy(search_tag.label, label);
      (*env)->ReleaseStringUTFChars(env, labelStr, label);
    }else{
      char* label = "\0";
      strcpy(search_tag.label, label);
    }

    // unsigned long long
    uint64_t ret = iosp_subscribe_task_create(p, &search_tag, type, (uint64_t)taskId);
    if(ret == 0){
      const char* msg = "Create subscribe task failed. maybe this a task already exists for the conn,please destroy task.";
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return 0;
    }

    return (long) ret;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    consumeSubscribeObject
 * Signature: (JJZ)Lcom/machloop/iosp/sdk/subscribe/SubscribeObjectResult;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_consumeSubscribeObject
  (JNIEnv *env, jclass clazz, jlong pLake, jlong taskId, jboolean useDirectByteBuffer){
    void* p = (void*) pLake;
    metadata **ppstMetadataArray;
    int searchNum;

    int ret = iosp_subscribe_consume(p, &searchNum, (uint64_t)taskId, &ppstMetadataArray);
    if(ret < 0){
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return NULL;
    }

    // 结果
    jobjectArray resultArray = (*env)->NewObjectArray(env, searchNum, g_class_fullObject, 0);
    if(useDirectByteBuffer){
      for(int i=0;i<searchNum;i++){
        jobject fullObject =  (*env)->NewObject(env, g_class_fullObject, g_method_construction_cursorResult);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_create_time, ppstMetadataArray[i]->create_time);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_ingest_time, ppstMetadataArray[i]->ingest_time);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_object_size, ppstMetadataArray[i]->object_size);
        // object id
        jstring objectId = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_id);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_id, objectId);
        // object name
        jstring objectName = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_name);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_name, objectName);
        // zone
        jstring zone = (*env)->NewStringUTF(env, ppstMetadataArray[i]->zone);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_zone, zone);
        // site
        jstring site = (*env)->NewStringUTF(env, ppstMetadataArray[i]->site);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_site, site);      
        // label
        jstring label = (*env)->NewStringUTF(env, ppstMetadataArray[i]->label);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_label, label);      
        // object
        if(ppstMetadataArray[i]->object_size!=0xFFFFFFFF && ppstMetadataArray[i]->object!=NULL){ // 大文件
          jobject contentByteBuffer = (*env)->NewDirectByteBuffer(env, ppstMetadataArray[i]->object, ppstMetadataArray[i]->object_size);
          (*env)->SetObjectField(env, fullObject, g_field_fullObject_content, contentByteBuffer);
        }

        (*env)->SetObjectArrayElement(env, resultArray, i, fullObject);
      }
    }else{
      for(int i=0;i<searchNum;i++){
        jobject fullObject =  (*env)->NewObject(env, g_class_fullObject, g_method_construction_cursorResult);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_create_time, ppstMetadataArray[i]->create_time);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_ingest_time, ppstMetadataArray[i]->ingest_time);
        (*env)->SetIntField(env, fullObject, g_field_fullObject_object_size, ppstMetadataArray[i]->object_size);
        // object id
        jstring objectId = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_id);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_id, objectId);
        // object name
        jstring objectName = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_name);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_object_name, objectName);
        // zone
        jstring zone = (*env)->NewStringUTF(env, ppstMetadataArray[i]->zone);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_zone, zone);
        // site
        jstring site = (*env)->NewStringUTF(env, ppstMetadataArray[i]->site);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_site, site);      
        // label
        jstring label = (*env)->NewStringUTF(env, ppstMetadataArray[i]->label);
        (*env)->SetObjectField(env, fullObject, g_field_fullObject_label, label);      
        // object
        if(ppstMetadataArray[i]->object_size!=0xFFFFFFFF && ppstMetadataArray[i]->object!=NULL){ // 大文件
          jbyteArray array = (*env)->NewByteArray(env, ppstMetadataArray[i]->object_size);
          (*env)->SetByteArrayRegion(env, array, 0, ppstMetadataArray[i]->object_size, ppstMetadataArray[i]->object);
          jobject contentByteBuffer = (*env)->CallStaticObjectMethod(env, g_class_byteBuffer, g_method_byteBuffer_warp, array);
          (*env)->SetObjectField(env, fullObject, g_field_fullObject_content, contentByteBuffer);
        }

        (*env)->SetObjectArrayElement(env, resultArray, i, fullObject);
      }
    }

    jobject resultObject =  (*env)->NewObject(env, g_class_subscribeObjectResult, g_method_construction_subscribeObjectResult);
    // ret 0:hasNext;1:over
    (*env)->SetIntField(env, resultObject, g_field_subscribeObjectResult_hasNext, ret);
    (*env)->SetObjectField(env, resultObject, g_field_subscribeObjectResult_objects, resultArray);  

    return resultObject;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    consumeSubscribeMetadata
 * Signature: (JJ)Lcom/machloop/iosp/sdk/subscribe/SubscribeMetadataResult;
 */
JNIEXPORT jobject JNICALL Java_com_machloop_iosp_sdk_Core_consumeSubscribeMetadata
  (JNIEnv *env, jclass clazz, jlong pLake, jlong taskId){
    void* p = (void*) pLake;
    metadata **ppstMetadataArray;
    int searchNum;

    int ret = iosp_subscribe_consume(p, &searchNum, (uint64_t)taskId, &ppstMetadataArray);
    if(ret < 0){
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return NULL;
    }

    // 结果
    jobjectArray resultArray = (*env)->NewObjectArray(env, searchNum, g_class_metadata, 0);

    for(int i=0;i<searchNum;i++){
      jobject o_metadata =  (*env)->NewObject(env, g_class_metadata, g_method_construction_metadata);
      (*env)->SetIntField(env, o_metadata, g_field_metadata_create_time, ppstMetadataArray[i]->create_time);
      (*env)->SetIntField(env, o_metadata, g_field_metadata_ingest_time, ppstMetadataArray[i]->ingest_time);
      (*env)->SetIntField(env, o_metadata, g_field_metadata_object_size, ppstMetadataArray[i]->object_size);
      // object id
      jstring objectIdResult = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_id);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_object_id, objectIdResult);
      // object name
      jstring objectName = (*env)->NewStringUTF(env, ppstMetadataArray[i]->object_name);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_object_name, objectName);
      // zone
      jstring zone = (*env)->NewStringUTF(env, ppstMetadataArray[i]->zone);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_zone, zone);
      // site
      jstring site = (*env)->NewStringUTF(env, ppstMetadataArray[i]->site);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_site, site);      
      // label
      jstring label = (*env)->NewStringUTF(env, ppstMetadataArray[i]->label);
      (*env)->SetObjectField(env, o_metadata, g_field_fullObject_label, label);      

      (*env)->SetObjectArrayElement(env, resultArray, i, o_metadata);
    }

    jobject resultObject =  (*env)->NewObject(env, g_class_subscribeMetadataResult, g_method_construction_subscribeMetadataResult);
    // ret 0:hasNext;1:over
    (*env)->SetIntField(env, resultObject, g_field_subscribeMetadataResult_hasNext, ret);
    (*env)->SetObjectField(env, resultObject, g_field_subscribeMetadataResult_metadatas, resultArray);  

    return resultObject;
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    destroySubscribe
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_destroySubscribe
  (JNIEnv *env, jclass clazz, jlong pLake){
    void* p = (void*) pLake;
    int ret = iosp_subscribe_destroy(p);
    if(ret != 0){
      const char* msg = iosp_get_error(ret);
      (*env)->ThrowNew(env, g_exception_IOException, msg);
      return;
    }
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    resultRelease
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_resultRelease
  (JNIEnv *env, jclass clazz, jlong pLake){
    void* p = (void*) pLake;
    iosp_sync_result_release(p);
  }

/*
 * Class:     com_machloop_iosp_sdk_Core
 * Method:    destory
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_machloop_iosp_sdk_Core_destory
  (JNIEnv *env, jclass clazz, jlong pLake){
    void* p = (void*) pLake;
    iosp_sync_destory(&p);
  }