/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *---------------------------------------------------------------------------
 */

#include <stdlib.h>
#include "common.h"


int startRecord(JNIEnv *env, jobject jrecord)
{
  jclass jcls=(*env)->GetObjectClass(env, jrecord);
  jmethodID jmid=(*env)->GetMethodID(env, jcls, "startRecording", "()V");
  if (!jmid) {
    LOG("startRecording() method not found.");
    return 0;
  }
  (*env)->CallVoidMethod(env, jrecord, jmid);
  if ((*env)->ExceptionOccurred(env)) {
    LOG("Exception occurred in calling startRecording()");
    return 0;
  }
  (*env)->DeleteLocalRef(env, jcls);
  return 1;
}


int stopRecord(JNIEnv *env, jobject jrecord)
{
  jclass jcls=(*env)->GetObjectClass(env, jrecord);
  jmethodID jmid=(*env)->GetMethodID(env, jcls, "stopRecording", "()V");
  if (!jmid) {
    LOG("stopRecording() method not found.");
    return 0;
  }
  (*env)->CallVoidMethod(env, jrecord, jmid);
  if ((*env)->ExceptionOccurred(env)) {
    LOG("Exception occurred in calling stopRecording()");
    return 0;
  }
  (*env)->DeleteLocalRef(env, jcls);
  return 1;
}


audiol_t *audioGetNextBuffer(JNIEnv *env, jobject jrecord)
{
#define BYTEBUFFER_CLASS "java/nio/ByteBuffer"
  audiol_t *a=NULL;
  jclass jcls=(*env)->GetObjectClass(env, jrecord);
  jmethodID jmid=(*env)->GetMethodID(env, jcls, "getBuffer",
				     "()L" BYTEBUFFER_CLASS ";");
  jobject jobj;
  jlong jlen;
  short *data;
  if (!jmid) {
    LOG("getBuffer() method not found.");
    return NULL;  
  }
  jobj=(*env)->CallObjectMethod(env, jrecord, jmid);
  jlen=(*env)->GetDirectBufferCapacity(env, jobj);
  data=(*env)->GetDirectBufferAddress(env, jobj);
  if (!data || !jlen || jlen%2) goto error;
  a=malloc(sizeof(*a));
  a->n=NULL;
  a->len=jlen/sizeof(short);
  a->audio=malloc(a->len*sizeof(short));
  a->flags=0;
  memcpy(a->audio, data, jlen);

 error:
  (*env)->DeleteLocalRef(env, jobj);
  (*env)->DeleteLocalRef(env, jcls);
  return a;
}


void audioNukeBuffer(audiol_t *a)
{
  free(a->audio);
  free(a);
}
