/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *---------------------------------------------------------------------------
 */

#include <stdio.h>
#include <string.h>
#include "common.h"


void disp(JNIEnv* env, jobject job, char *s) 
{
  LOG("%s",s);
  jclass cls = (*env)->GetObjectClass(env,job);
  jmethodID method = (*env)->GetMethodID(env, cls, "asyncPrint", "(Ljava/lang/String;)V");
  if (method) {
    jstring js = (*env)->NewStringUTF(env,s);
    (*env)->CallVoidMethod(env,job,method,js);
    (*env)->DeleteLocalRef(env, js);
  }
  (*env)->DeleteLocalRef(env, cls);
}

void dispv(JNIEnv *env, jobject job, const char *format, ...)
{
#define MAX_LINE_SIZE 1023
  char tmp[MAX_LINE_SIZE+1];
  va_list ap;

  va_start(ap, format);
  vsnprintf(tmp, MAX_LINE_SIZE, format, ap);
  va_end(ap);
  disp(env, job, tmp);
}

static char *formatExpirationDate(time_t expiration)
{
  static char expdate[33];
  if (!expiration) return "never";
  strftime(expdate, 32, "%m/%d/%Y 00:00:00 GMT", gmtime(&expiration));
  return expdate;
}

/* Display TrulyHandsfree SDK version and license expiration */
void displaySdkVersionAndExpirationDate(JNIEnv *env, jobject job)
{
  dispv(env, job, "TrulyHandsfree SDK Library version: %s", thfVersion());
  dispv(env, job, "Expiration date: %s\n", formatExpirationDate(thfGetLicenseExpiration()));
}

char *checkFlagsText(int flags)
{
  struct {
    int bitfield;
    char *text;
  } problems[] = {
    {CHECKRECORDING_BITFIELD_ENERGY_MIN, "energy_min"},
    {CHECKRECORDING_BITFIELD_ENERGY_STD_DEV, "energy_std_dev"},
    {CHECKRECORDING_BITFIELD_SIL_BEG_MSEC, "sil_beg_msec"},
    {CHECKRECORDING_BITFIELD_SIL_END_MSEC, "sil_end_msec"},
    {CHECKRECORDING_BITFIELD_SNR, "snr"},
    {CHECKRECORDING_BITFIELD_RECORDING_VARIANCE, "recording_variance"},
    {CHECKRECORDING_BITFIELD_CLIPPING_PERCENT, "clipping_percent"},
    {CHECKRECORDING_BITFIELD_CLIPPING_MSEC, "clipping_msec"},
    {CHECKRECORDING_BITFIELD_POOR_RECORDINGS, "poor_recordings"},
    {CHECKRECORDING_BITFIELD_SPOT, "spot"},
    {CHECKRECORDING_BITFIELD_VOWEL_DUR, "vowel_dur"},
    {0, NULL}
  };
  static char r[1024];
  int i, n=0;

  r[0]='\0';
  for (i=0; flags && problems[i].bitfield; i++) {
    if (!(flags & problems[i].bitfield)) continue;
    flags &= ~problems[i].bitfield;
    if (n > 0) {
      if (flags) strcat(r, ", ");
      else strcat(r, " and ");
    }
    n++;
    strcat(r,problems[i].text);
  }
  return r;
}
