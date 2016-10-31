/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 */

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "common.h"

#define BUILDSEARCH  (0)   // Control 'build' versus 'load' search from file
#define GENDER_DETECT (1)  // Enable/disable gender detection
#define NBEST              (1)                /* Number of results */
#define PHRASESPOT_PARAMA_OFFSET   (0)        /* Phrasespotting ParamA Offset */
#define PHRASESPOT_BEAM    (100.f)            /* Pruning beam */
#define PHRASESPOT_ABSBEAM (100.f)            /* Pruning absolute beam */
#define PHRASESPOT_DELAY   (100)	            /* Phrasespotting Delay */
#define MAXSTR             (512)              /* Output string size */

jlong Java_com_example_macbookretina_ibmconversation_MainActivity_phrasespotInit(JNIEnv* env,
    jobject job, jstring jappDir) {
  context_t *context = malloc(sizeof(context_t));
  const char *appDir = (*env)->GetStringUTFChars(env, jappDir, NULL);
  thf_t *ses = NULL;
  recog_t *r = NULL;
  searchs_t *s = NULL;
  pronuns_t *pp = NULL;
  char str[MAXSTR], fname[MAXSTR];
  const char *ewhere, *ewhat = NULL;
  float delay;

#if BUILDSEARCH
  /* NOTE: Phrasespot parameters are phrase specific. The following paramA,paramB,paramC values are
   * tuned specifically for 'hello blue genie'. It is strongly recommended that you determine
   * optimal parameters empirically if you change vocabulary. Please note that Sensory normally
   * provides custom phrasespotted grammars as saved pre-built compiled data files intended to
   * be loaded into the recognizer with thfSearchCreateFromFile. Pre-built compiled searches
   * provided by Sensory contain additional tuning information which may provide better recognition
   * accuracy but cannot be expressed as textual form searches like the "Hello Blue Genie" trigger
   * example in this sample created with thfSearchCreateFromGrammar. */
  float paramAOffset, beam, absbeam;
  char *gra = "g=hello blue genie; \
    paramA:$g -850;                \
    paramB:$g 740;                 \
    paramC:$g 226;";
  unsigned short count = 3;
  const char *w0="hello",*w1="blue", *w2="genie";
  const char *word[] = {w0,w1,w2};
  const char *p0=". h E . l oU1 .",
  *p1=". b l u1 .",
  *p2=". dZ i1 . n i .";
  const char *pronun[] = {p0,p1,p2};
#endif

  /* Create SDK session */
  if (!(ses = thfSessionCreate())) {
    panic(cons, "thfSessionCreate", thfGetLastError(NULL), 0);
    free(context);
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 1;
  }
  displaySdkVersionAndExpirationDate(env, job);

  /* Create recognizer */
  /* NOTE: HBG_NETFILE is a custom model for 'hello blue genie'. Use a generic acoustic model
   * if you change vocabulary or contact Sensory for assistance in developing a custom
   * acoustic model specific to your vocabulary. */
  sprintf(str, "Loading recognizer: %s", HBG_NETFILE);
  disp(env, job, str);
  sprintf(fname, "%s%s", appDir, HBG_NETFILE);
  if (!(r = thfRecogCreateFromFile(ses, fname, 0xFFFF, -1, SDET)))
    THROW("thfRecogCreateFromFile");

#if BUILDSEARCH

  /* Create pronunciation object */
  sprintf(str,"Loading pronunciation model: %s", LTSFILE); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,LTSFILE);
  if(!(pp=thfPronunCreateFromFile(ses,fname,0)))
  THROW("pronunCreateFromFile");

  /* Create search */
  disp(env,job,"Compiling search...");
  if(!(s=thfSearchCreateFromGrammar(ses,r,pp,gra,word,pronun,count,NBEST,PHRASESPOTTING)))
  THROW("thfSearchCreateFromGrammar");

  /* Save search */
  sprintf(str,"Saving search: %s", PHRASESPOT_SEARCHFILE); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,PHRASESPOT_SEARCHFILE);
  if(!(thfSearchSaveToFile(ses,s, fname)))
  THROW("thfSearchSaveToFile");

  /* Configure parameters */
  disp(env,job,"Setting parameters...");
  paramAOffset=PHRASESPOT_PARAMA_OFFSET;
  beam=PHRASESPOT_BEAM;
  absbeam=PHRASESPOT_ABSBEAM;
  delay=PHRASESPOT_DELAY;
  set= PHRASESPOT_SET_PARAMA_OFFSET | PHRASESPOT_SET_BEAM | PHRASESPOT_SET_ABSBEAM | PHRASESPOT_SET_DELAY;
  if (!thfPhrasespotConfigure(ses,r,s,set,&paramAOffset,&beam,&absbeam,&delay,NULL))
  THROW("thfPhrasespotConfigure");

#else
  sprintf(str, "Loading pre-built search: %s", HBG_SEARCHFILE4);
  disp(env, job, str);
  sprintf(fname, "%s%s", appDir, HBG_SEARCHFILE4);
  if (!(s = thfSearchCreateFromFile(ses, r, fname, NBEST)))
    THROW("thfSearchCreateFromFile");

  /* Configure parameters - only DELAY... others are saved in search already */
  disp(env, job, "Setting parameters...");
  delay = PHRASESPOT_DELAY;
  if (!thfPhrasespotConfigSet(ses,r,s,PS_DELAY,delay))
    THROW("thfPhrasespotConfigSet: delay");

#endif

  /* Initialize recognizer */
  disp(env, job, "Initializing recognizer...");
  if (!thfRecogInit(ses, r, s, RECOG_KEEP_WAVE_WORD_PHONEME))
    THROW("thfRecogInit");

#if GENDER_DETECT
  /* initialize a speaker object with a single speaker, and arbitrarily set it up */
  /* to hold one recording from this speaker */
  if (!thfSpeakerInit(ses, r, 0, 1)) {
    THROW("thfSpeakerInit");
  }

  sprintf(str, "Loading gender model: %s", HBG_GENDERFILE);
  disp(env, job, str);
  sprintf(str, "%s/%s", appDir, HBG_GENDERFILE);
  /* now read the gender model, which has been tuned to the target phrase */
  if (!thfSpeakerReadGenderModel(ses, r, str)) {
    THROW("thfSpeakerReadGenderModel");
  }
#endif

  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  if (pp)
    thfPronunDestroy(pp);
  pp = NULL;
  context->ses = ses;
  context->r = r;
  context->s = s;
  disp(env, job, "Say: HELLO BLUE GENIE");
  return (jlong) (long) context;

  error: if (!ewhat && ses)
    ewhat = thfGetLastError(ses);
  if (s)
    thfSearchDestroy(s);
  if (pp)
    thfPronunDestroy(pp);
  if (r)
    thfRecogDestroy(r);
  free(context);
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  sprintf(str, "Panic - %s: %s\n\n", ewhere, ewhat);
  disp(env, job, str);
  if (ses)
    thfSessionDestroy(ses);
  return 0;
}

jstring Java_com_example_macbookretina_ibmconversation_MainActivity_phrasespotPipe(JNIEnv* env,
    jobject job, jlong arg, jobject buffer, jlong rate) {
  context_t *t = (context_t*) (long) arg;
  thf_t *ses = t->ses;
  recog_t *r = t->r;
  searchs_t *s = t->s;
  unsigned short status;
  const char *ewhere, *ewhat = NULL;
  const char *rres;
  char str[MAXSTR];
  float score;
  jstring res = NULL;
  short *sdata = (short*) (*env)->GetDirectBufferAddress(env, buffer);
  unsigned long sz = (unsigned long) (jlong) (*env)->GetDirectBufferCapacity(
      env, buffer) / 2;

  if (rate != (unsigned) thfRecogGetSampleRate(ses, r)) {
    short *cspeech = NULL;
    unsigned long clen;
    if (!thfRecogSampleConvert(ses, r, rate, sdata, sz, &cspeech, &clen))
      THROW("recogSampleConvert");
    sprintf(str, "Converted samplerate: %d -> %d", (int) rate,
        (int) thfRecogGetSampleRate(ses, r));
    disp(env, job, str);
    if (!thfRecogPipe(ses, r, clen, cspeech, RECOG_ONLY, &status))
      THROW("recogPipe");
    thfFree(cspeech);
  } else {
    if (!thfRecogPipe(ses, r, sz, sdata, RECOG_ONLY, &status))
      THROW("recogPipe");
  }
  if (status == RECOG_DONE) {
#if GENDER_DETECT
    float genderProb;
#endif
    const char *walign, *palign;
    if (!thfRecogResult(ses, r, &score, &rres, &walign, &palign, NULL, NULL,
        NULL, NULL))
      THROW("thfRecogResult");
    sprintf(str, "***RESULT=%s score=%f", rres, score);
    disp(env, job, str);

#if GENDER_DETECT
    if (!thfSpeakerGender(ses, r, &genderProb)) {
      THROW("thfSpeakerGender");
    }
    if (genderProb < 0.5) {
      sprintf(str, "Gender: MALE (score=%f)\n", (1.0 - genderProb));
    } else {
      sprintf(str, "Gender: FEMALE (score=%f)\n", genderProb);
    }
    disp(env, job, str);
#endif

    if (walign) {
      sprintf(str, "%s", walign);
      disp(env, job, str);
    } else
      disp(env, job, "No word alignment");
    if (palign) {
      sprintf(str, "%s", palign);
      disp(env, job, str);
    } else
      disp(env, job, "No phoneme alignment");

    if (rres)
      res = (*env)->NewStringUTF(env, rres);
    thfRecogReset(ses, r);
  }
  return res;
  error: if (!ewhat && ses)
    ewhat = thfGetLastError(ses);
  if (s)
    thfSearchDestroy(s);
  if (r)
    thfRecogDestroy(r);
  sprintf(str, "Panic - %s: %s", ewhere, ewhat);
  disp(env, job, str);
  if (ses)
    thfSessionDestroy(ses);
  free(t);
  return NULL;
}

void Java_com_example_macbookretina_ibmconversation_MainActivity_phrasespotClose(JNIEnv* env,
    jobject job, jlong arg) {
  context_t *t = (context_t*) (long) arg;
  thf_t *ses = t->ses;
  recog_t *r = t->r;
  searchs_t *s = t->s;
  if (r)
    thfRecogDestroy(r);
  r = NULL;
  if (s)
    thfSearchDestroy(s);
  s = NULL;
  if (ses)
    thfSessionDestroy(ses);
  ses = NULL;
  free(t);
  disp(env, job, "Done");
  return;
}

