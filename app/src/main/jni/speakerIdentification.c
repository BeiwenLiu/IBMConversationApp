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

#define BUILDSEARCH        (0)          /* Control 'build' versus 'load' search from file */
#define NBEST              (1)          /* Number of results */
#define PHRASESPOT_PARAMA_OFFSET (0)    /* Phrasespotting ParamA Offset */
#define PHRASESPOT_BEAM    (100.f)      /* Pruning beam */
#define PHRASESPOT_ABSBEAM (100.f)      /* Pruning absolute beam */
#define PHRASESPOT_DELAY   (250)	      /* Phrasespotting Delay: at least 250ms needed for speaker ID */
#define MAXSTR             (512)        /* Output string size */

jlong Java_com_sensory_TrulyHandsfreeSDK_Console_speakerIdentificationInit(JNIEnv* env,jobject job, jshort numUsers, jshort numEnroll, jstring jappDir)
{
  context_t *context=malloc(sizeof(context_t));
  const char *appDir=(*env)->GetStringUTFChars(env, jappDir, 0);
  thf_t *ses=NULL;
  recog_t *r=NULL;
  searchs_t *s=NULL;
  pronuns_t *pp=NULL;
  char str[MAXSTR], fname[MAXSTR];
  const char *ewhere, *ewhat=NULL;
  unsigned short uIdx=0;
  float delay;
#if BUILDSEARCH
  /* NOTE: Phrasespot parameters are phrase specific. The following paramA,
   * paramB,paramC values are tuned specifically for 'hello blue genie'.
   * It is strongly recommended that you determine optimal parameters
   * empirically if you change vocabulary.
   */
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
  if(!(ses=thfSessionCreate())) {
    panic(cons,"thfSessionCreate",thfGetLastError(NULL),0);
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 1;
  }
  displaySdkVersionAndExpirationDate(env, job);

  /* Create recognizer */
  sprintf(str,"Loading recognizer: %s", HBG_NETFILE); disp(env,job,str);
  sprintf(fname,"%s%s",appDir,HBG_NETFILE);
  if(!(r=thfRecogCreateFromFile(ses,fname,0xFFFF,-1,SDET)))
    THROW("thfRecogCreateFromFile");
  
 #if BUILDSEARCH
 /* Create pronunciation object */
  sprintf(str,"Loading pronunciation model: %s", LTSFILE); disp(env,job,str);
  sprintf(fname,"%s%s",appDir,LTSFILE);
  if(!(pp=thfPronunCreateFromFile(ses,fname,0)))
    THROW("pronunCreateFromFile");

  /* Create search */
  disp(env,job,"Compiling search...");
  if(!(s=thfSearchCreateFromGrammar(ses,r,pp,gra,word,pronun,count,NBEST,PHRASESPOTTING))) 
    THROW("thfSearchCreateFromGrammar");

  /* Configure parameters */
  disp(env,job,"Setting parameters...");
  paramAOffset=PHRASESPOT_PARAMA_OFFSET; 
  beam=PHRASESPOT_BEAM; 
  absbeam=PHRASESPOT_ABSBEAM; 
  delay=PHRASESPOT_DELAY;
  if (!thfPhrasespotConfigSet(ses,r,s,PS_PARAMA_OFFSET,paramAOffset))
    THROW("thfPhrasespotConfigSet: parama_offset");
  if (!thfPhrasespotConfigSet(ses,r,s,PS_BEAM,beam))
    THROW("thfPhrasespotConfigSet: beam");
  if (!thfPhrasespotConfigSet(ses,r,s,PS_ABSBEAM,absbeam))
    THROW("thfPhrasespotConfigSet: absbeam");
  if (!thfPhrasespotConfigSet(ses,r,s,PS_DELAY,delay))
    THROW("thfPhrasespotConfigSet: delay");
  
  /* Save search */
  sprintf(str,"Saving search: %s", PHRASESPOT_SEARCHFILE); disp(env,job,str);
  sprintf(fname,"%s%s",appDir,PHRASESPOT_SEARCHFILE);
  if(!(thfSearchSaveToFile(ses,s,fname))) 
    THROW("thfSearchSaveToFile");
#else
  sprintf(str,"Loading pre-built search: %s", HBG_SEARCHFILE4); disp(env,job,str);
  sprintf(fname,"%s%s",appDir,HBG_SEARCHFILE4);
  if(!(s=thfSearchCreateFromFile(ses,r,fname,NBEST)))
    THROW("thfSearchCreateFromFile");

  /* Configure parameters */
  disp(env,job,"Setting parameters...");
  // Only delay needs configuring.. other params are stored in search file */
  delay=PHRASESPOT_DELAY;
  if (!thfPhrasespotConfigSet(ses,r,s,PS_DELAY,delay))
    THROW("thfPhrasespotConfigSet: delay");


#endif

  /* Initialize recognizer - Must use "RECOG_KEEP_WAVE_WORD_PHONEME" for speaker ID */
  disp(env,job,"Initializing recognizer...");
  if(!thfRecogInit(ses,r,s,RECOG_KEEP_WAVE_WORD_PHONEME)) 
    THROW("thfRecogInit");

    /* Initialize speaker identification */
  disp(env,job,"Initializing first speaker...");
  if (!thfSpeakerInit(ses,r,1,numEnroll))
    THROW("thfSpeakerInit");
  disp(env,job,"Initializing additional speakers...");
  for (uIdx=2; uIdx <=numUsers; uIdx++) {
    if (!thfSpeakerAdd(ses,r,uIdx))
      THROW("thfSpeaker");
  }

  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  if (pp)thfPronunDestroy(pp); pp=NULL;
  context->ses=ses;
  context->r=r;
  context->s=s;
  return (jlong)(long)context;

 error:
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(s)thfSearchDestroy(s);
  if(pp)thfPronunDestroy(pp); 
  if(r)thfRecogDestroy(r);
  sprintf(str,"Panic - %s: %s\n\n",ewhere,ewhat); disp(env,job,str);
  if(ses)thfSessionDestroy(ses);
  free(context);
  return 0; 
}


jshort Java_com_sensory_TrulyHandsfreeSDK_Console_speakerIdentificationPipe( JNIEnv* env,jobject job,jlong arg,jobject buffer, jlong rate, jshort idx, jshort numUsers, jshort numEnroll, jshort numLoop) {
  context_t *c=(context_t*)(long)arg;
  unsigned short status;
  const char *ewhere, *ewhat=NULL;;
  char str[MAXSTR];
  short *sdata = (short*)(*env)->GetDirectBufferAddress(env, buffer);
  unsigned long sz = (unsigned long)(jlong)(*env)->GetDirectBufferCapacity(env, buffer)/2;
  if(rate!=(unsigned)thfRecogGetSampleRate(c->ses,c->r)) {
    short *cspeech=NULL;
    unsigned long clen;
    if(!thfRecogSampleConvert(c->ses,c->r,rate,sdata,sz,&cspeech,&clen))
      THROW("recogSampleConvert");
    sdata=cspeech;
    sz=clen;
    sprintf(str,"Converted samplerate: %d -> %d", (int)rate,(int)thfRecogGetSampleRate(c->ses,c->r));
    disp(env,job,str);
  } 
  if(!thfRecogPipe(c->ses,c->r,sz,sdata,RECOG_ONLY,&status))
    THROW("recogPipe");
  if(status==RECOG_DONE) return 2;   // phrasespotted
  return 1;  // not phrasespotted (yet)
 error:
  if(!ewhat && c->ses) ewhat=thfGetLastError(c->ses);
  if(c->s)thfSearchDestroy(c->s);
  if(c->r)thfRecogDestroy(c->r);
  sprintf(str,"Panic - %s: %s",ewhere,ewhat); disp(env,job,str);
  if(c->ses) thfSessionDestroy(c->ses);
  free(c);
  return 0;
}


jshort Java_com_sensory_TrulyHandsfreeSDK_Console_speakerIdentificationResult( JNIEnv* env,jobject job,jlong arg,jshort idx, jshort numUsers, jshort numEnroll, jshort numLoop) {
  context_t *c=(context_t*)(long)arg;
  const char *ewhere, *ewhat=NULL;
  const char *rres;
  char str[MAXSTR];
  float score;
  //  jstring res=NULL;
  unsigned short vIdx;
  unsigned short userID = idx/numEnroll+1;
  float rawScore, bestScore;
  short bestUser;
    if (!thfRecogResult(c->ses,c->r,&score,&rres,NULL,NULL,NULL,NULL,NULL,NULL)) 
      THROW("thfRecogResult");
    sprintf(str,"Result: %s (score=%.2f)",rres,score); disp(env,job,str);

    if (idx<numEnroll*numUsers) {
      if (!thfSpeakerStoreLastRecording(c->ses,c->r,userID,(int)(score*4096.0f),1))
    	  THROW("thfSpeakerStoreLastRecording");
    } 
    if (idx==numEnroll*numUsers-1) {
      // All users have enrolled. Do adaptation and switch to 'ID' mode
      disp(env,job,"\n====== Learning user. Please wait =====");
      for (vIdx=1; vIdx <= numUsers; vIdx++) {
	   if (!thfSpeakerAdapt(c->ses,c->r,vIdx,0,"IGNORE_SV_DEFAULT",512))
	    THROW("thfSpeakerAdapt");
      }
    }
    if (idx>=numEnroll*numUsers) {
      bestScore = 0.0f;
      bestUser = 1;
      for (vIdx=1; vIdx <=numUsers; vIdx++) {
    	  if (!thfSpeakerVerify(c->ses,c->r,vIdx,&rawScore,NULL))
    		  THROW("thfSpeakerVerify");
    	  if (rawScore >= bestScore) {
    		  bestScore = rawScore;
    		  bestUser = vIdx;
    	  }
      }
      sprintf(str,"USER %i (score=%.2f)\n", bestUser, bestScore);
      disp(env,job,str);
    }
    //    if (rres)res=(*env)->NewStringUTF(env,rres);
    thfRecogReset(c->ses,c->r);
  return 1;
 error:
  if(!ewhat && c->ses) ewhat=thfGetLastError(c->ses);
  if(c->s)thfSearchDestroy(c->s);
  if(c->r)thfRecogDestroy(c->r);
  sprintf(str,"Panic - %s: %s",ewhere,ewhat); disp(env,job,str);
  if(c->ses) thfSessionDestroy(c->ses);
  free(c);
  return 0; 
}


void Java_com_sensory_TrulyHandsfreeSDK_Console_speakerIdentificationClose( JNIEnv* env,jobject job, jlong arg )
{
  context_t *c=(context_t*)(long)arg;
  if(c->r)thfRecogDestroy(c->r); c->r=NULL;
  if(c->s)thfSearchDestroy(c->s); c->s=NULL;
  if(c->ses)thfSessionDestroy(c->ses); c->ses=NULL;
  disp(env,job,"Done");
  free(c);
  return;
}
