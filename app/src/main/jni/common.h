/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *---------------------------------------------------------------------------
 */

#ifndef COMMON_H
#define COMMON_H
#include <jni.h>
#include <android/log.h>
#include <stdarg.h>
#include "trulyhandsfree.h"
#include "audio.h"

#define NETFILE     "nn_en_us_mfcc_16k_15_250_v5.1.1.raw"
#define LTSFILE     "lts_en_us_9.5.2b.raw"
#define NAMES_SEARCHFILE  "names.raw" 
#define NAMES_VOCABFILE   "names.txt"
#define TIME_SEARCHFILE   "time.raw"            
#define WAVEFILE          "john_smith_16khz.wav"          /* 16kHz RIFF Wave       */
#define AUDIOFILE         "twelve_oclock_16khz.audio"     /* 16kHz RAW PCM audio   */
#define INCREMENTAL_SEARCHFILE  "incremental.raw"      
#define PHRASESPOT_SEARCHFILE    "myphrasespot_search.raw"   /* output search */
#define HBG_SEARCHFILE1       "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_3.raw"
#define HBG_SEARCHFILE2       "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_5.raw"
#define HBG_SEARCHFILE3       "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_7.raw"
#define HBG_SEARCHFILE4       "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_9.raw"
#define HBG_SEARCHFILE5       "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_11.raw"
#define HBG_SEARCHFILE6       "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_13.raw"
#define HBG_SEARCHFILE7       "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_15.raw"
#define HBG_NETFILE           "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_am.raw"

#define HBG_ANTISPEAKERFILE   "hbg_antiData_v6_0.raw"
#define HBG_GENDERFILE        "hbg_genderModel.raw"
#define HBG_MYSPEAKERFILE     "hbg_myspeakerdata.raw"  /* Output file */

#define MAXSTR  (512)      /* Output string size */
#define THROW(a) { ewhere=(a); goto error; }
#define THROW2(a,b) {ewhere=(a); ewhat=(b); goto error; }
#define LOG(...) __android_log_print(ANDROID_LOG_INFO, "TrulyHandsfreeSDK",__VA_ARGS__)
#define printf(a) LOG(a)

typedef struct context_s {
  thf_t *ses;
  recog_t *r;
  searchs_t *s;
  recog_t *psr;
  searchs_t *pss;
} context_t;


void disp(JNIEnv* env, jobject job, char *s);
void dispv(JNIEnv *env, jobject job, const char *format, ...);
void displaySdkVersionAndExpirationDate(JNIEnv *env, jobject job);
char *checkFlagsText(int flags);

/* recogEnroll functions */
#define THF_LVCSR_DATADIR ""

/* udtsid functions */
#define SAMPLES_DATADIR ""
#define THF_DATADIR ""
#define PHNSEARCH_FILE "phonemeSearch_1_5.raw"
#define SVSID_FILE "svsid_1_1.raw"

#define initConsole(a) ((void *)a)
#define closeConsole(a)
#define panic(c, where, why, exit) LOG("%s - %s", where, why)

#endif
