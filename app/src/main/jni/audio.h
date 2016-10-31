/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *---------------------------------------------------------------------------
 */

#ifndef AUDIO_H
#define AUDIO_H

#define SAMPLERATE 16000

typedef struct audiol_s {
  short *audio;
  struct audiol_s *n;
  unsigned short len;
  unsigned flags;
} audiol_t;

/* These are no-ops for Android */
#define killAudio()
#define initAudio(a, b)
#define audioEventLoop()

int startRecord(JNIEnv *env, jobject jrecord);
int stopRecord(JNIEnv *env, jobject jrecord);
audiol_t *audioGetNextBuffer(JNIEnv *env, jobject jrecord);
void audioNukeBuffer(audiol_t *a);

#endif
