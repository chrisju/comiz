/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <stdio.h>
#include <string.h>
#include <jni.h>

#define NAME3(CLASS3, FUNC3) Java_##CLASS3##_##FUNC3
#define NAME2(CLASS2, FUNC2) NAME3(CLASS2, FUNC2)
#define JNI_FUNC(FUNC) NAME2(PACKAGE_NAME, FUNC)

#include <android/log.h>
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "zz", __VA_ARGS__)


/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/HelloJni/HelloJni.java
JNIEXPORT jstring JNICALL stringFromJNI( JNIEnv* env,
        jobject thiz )
*/
jstring
JNI_FUNC(MainActivity_loadluaconfig)( JNIEnv* env,
                                                  jobject thiz, jstring path )
{
    int w,h;
    char s[256];
    const char *pathl = (*env)->GetStringUTFChars(env, path, 0);
    load (pathl, &w, &h);
    sprintf(s,"w=%d\nh=%d\n",w,h);
    return (*env)->NewStringUTF(env, s);
}

jstring
JNI_FUNC(MainActivity_file2stringarray)( JNIEnv* env,
                                                  jobject thiz, jstring luapath, jstring datapath )
{
    const char *path1 = (*env)->GetStringUTFChars(env, luapath, 0);
    const char *path2 = (*env)->GetStringUTFChars(env, datapath, 0);

    char **comics;
    int i=0,len;
    file2tab (path1, path2, &comics, &len);
    (*env)->ReleaseStringUTFChars(env, luapath, path1);
    (*env)->ReleaseStringUTFChars(env, datapath, path2);

    jclass objClass = (*env)->FindClass(env , "java/lang/String");
    jobjectArray texts = (*env)->NewObjectArray(env, (jsize)len, objClass, 0);
    jstring jstr;

    for(i=0;i<len;i++)
    {
        jstr = (*env)->NewStringUTF(env , comics[i]);
        (*env)->SetObjectArrayElement(env, texts, i, jstr);//必须放入jstring
        free(comics[i]);
    }
    free(comics);

    return texts;
}

jstring
JNI_FUNC(MainActivity_lua2stringarray)( JNIEnv* env,
                                                  jobject thiz, jstring luastring, jstring datapath )
{
    const char *luas = (*env)->GetStringUTFChars(env, luastring, 0);
    const char *path2 = (*env)->GetStringUTFChars(env, datapath, 0);

    char **comics;
    int i=0,len;
    lua2tab (luas, path2, &comics, &len);
    (*env)->ReleaseStringUTFChars(env, luastring, luas);
    (*env)->ReleaseStringUTFChars(env, datapath, path2);

    jclass objClass = (*env)->FindClass(env , "java/lang/String");
    jobjectArray texts = (*env)->NewObjectArray(env, (jsize)len, objClass, 0);
    jstring jstr;

    for(i=0;i<len;i++)
    {
        jstr = (*env)->NewStringUTF(env , comics[i]);
        (*env)->SetObjectArrayElement(env, texts, i, jstr);//必须放入jstring
        free(comics[i]);
    }
    free(comics);

    return texts;
}

jstring
JNI_FUNC(MainActivity_luafunc2stringarray)( JNIEnv* env,
                                                  jobject thiz, jstring luastring, jstring luafunc )
{
    const char *luas = (*env)->GetStringUTFChars(env, luastring, 0);
    const char *func = (*env)->GetStringUTFChars(env, luafunc, 0);

    char **comics;
    int i=0,len;
    luafunc2tab (luas,func, &comics, &len);
    (*env)->ReleaseStringUTFChars(env, luastring, luas);
    (*env)->ReleaseStringUTFChars(env, luafunc, func);

    jclass objClass = (*env)->FindClass(env , "java/lang/String");
    jobjectArray texts = (*env)->NewObjectArray(env, (jsize)len, objClass, 0);
    jstring jstr;

    for(i=0;i<len;i++)
    {
        jstr = (*env)->NewStringUTF(env , comics[i]);
        (*env)->SetObjectArrayElement(env, texts, i, jstr);//必须放入jstring
        free(comics[i]);
    }
    free(comics);

    return texts;
}

jstring
JNI_FUNC(MainActivity_luafunc2stringarray1)( JNIEnv* env,
                                                  jobject thiz, jstring luastring, jstring luafunc, jstring strparam1 )
{
    const char *luas = (*env)->GetStringUTFChars(env, luastring, 0);
    const char *func = (*env)->GetStringUTFChars(env, luafunc, 0);
    const char *param = (*env)->GetStringUTFChars(env, strparam1, 0);

    char **comics;
    int i=0,len;
    luafunc2tab1 (luas,func, param, &comics, &len);
    (*env)->ReleaseStringUTFChars(env, luastring, luas);
    (*env)->ReleaseStringUTFChars(env, luafunc, func);
    (*env)->ReleaseStringUTFChars(env, strparam1, param);

    jclass objClass = (*env)->FindClass(env , "java/lang/String");
    jobjectArray texts = (*env)->NewObjectArray(env, (jsize)len, objClass, 0);
    jstring jstr;

    for(i=0;i<len;i++)
    {
        jstr = (*env)->NewStringUTF(env , comics[i]);
        (*env)->SetObjectArrayElement(env, texts, i, jstr);//必须放入jstring
        (*env)->DeleteLocalRef(env, jstr);
        free(comics[i]);
    }
    free(comics);

    return texts;
}

jstring
JNI_FUNC(MainActivity_luafunc2stringarray1b)( JNIEnv* env,
                                                  jobject thiz, jstring luastring, jstring luafunc, jbyteArray b)
{
    const char *luas = (*env)->GetStringUTFChars(env, luastring, 0);
    const char *func = (*env)->GetStringUTFChars(env, luafunc, 0);

    //只处理不含'\0'的字节数组
    //获取数组长度
    jsize length = (*env)->GetArrayLength(env,b);
    char *bytes = calloc(length+1,1);
    //获取接收到的数据
    int j;
    jbyte* p = (*env)->GetByteArrayElements(env,b,NULL);
    //打印
    for(j=0;j<length;j++)
    {
        bytes[j]=p[j];
    }

    char **comics;
    int i,len;
    luafunc2tab1 (luas,func, bytes, &comics, &len);
    (*env)->ReleaseStringUTFChars(env, luastring, luas);
    (*env)->ReleaseStringUTFChars(env, luafunc, func);

    jclass objClass = (*env)->FindClass(env , "java/lang/String");
    jobjectArray texts = (*env)->NewObjectArray(env, (jsize)len, objClass, 0);
    jstring jstr;

    for(i=0;i<len;i++)
    {
        jstr = (*env)->NewStringUTF(env , comics[i]);
        (*env)->SetObjectArrayElement(env, texts, i, jstr);//必须放入jstring
        (*env)->DeleteLocalRef(env, jstr);
        free(comics[i]);
    }
    free(comics);

    free(bytes);
    return texts;
}

jstring
JNI_FUNC(MainActivity_jnidecode)( JNIEnv* env,
                                                  jobject thiz, jbyteArray b)
{
    jsize length = (*env)->GetArrayLength(env,b);
    char* bytes = (*env)->GetByteArrayElements(env,b,NULL);
    char pwd[] = "java/lang/String";
    xor_a(bytes, length, pwd);
    jbyteArray ba;
    ba = (*env)->NewByteArray(env,length);
    (*env)->SetByteArrayRegion(env, ba, 0, length, bytes);
    return ba;
}

