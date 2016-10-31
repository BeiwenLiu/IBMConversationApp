/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 *---------------------------------------------------------------------------
 * Sensory TrulyHandsfree SDK Example: recogSeq.c
 * Using a phrasespot search and a grammar search
 * performs spotting and then recognition on live speech (... or simulated live speech).
 *  - Uses live speech (or simulated live speech) as input.
 *  - Uses pre-constructed phrasespot search and grammar search (e.g. built by Sensory).
 *  - Illustrates pipelined recognition for phrasespotting and grammar recognition,
 *    as well as sequential recognition (e.g. phrasespotting followed immediately by
 *    grammar recognition)
 *  - Illustrates how to calibrate speech detector, and recognizers
 *    and searches for sequential recogntion.
 *---------------------------------------------------------------------------
 */
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "common.h"

// PhraseSpot
#define PS_NET      "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_am.raw"
#define PS_SEARCH   "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_9.raw"

// RecogSong
#define GRAMMAR_NET    "nn_en_us_mfcc_16k_15_250_v5.1.1.raw"
#define GRAMMAR_LTS    "lts_en_us_9.5.2b.raw"
#define GRAMMAR_SEARCH "names.raw"

#define KEEP_FEATURES  0
#define BEAM           (200.f)         /* Pruning beam */
#define LSILENCE       (20000.f)       /* Maximum silence allowed */
#define MAXRECORD      (10000.f)       /* Maximum record time */
#define NBEST          (3)             /* Max number of results */
#define SAMPLE_RATE  		16000
#define PHRASESPOT_DELAY  	15
#define PHRASESPOT_SEQ_BUFFER 	10000

#define STRSZ   (128)

jlong Java_com_sensory_TrulyHandsfreeSDK_Console_recogSeqInit( JNIEnv* env,jobject job, jstring jappDir)
{
  const char *appDir=(*env)->GetStringUTFChars(env, jappDir, 0);
  context_t *context=malloc(sizeof(context_t));
  thf_t *ses = NULL;
  recog_t *r = NULL, *psr = NULL;
  searchs_t *s = NULL, *pss = NULL;
  float beam, garbage, any, nota, lsil, maxR, ps_seq_buffer;
  char str[MAXSTR], fname[MAXSTR];
  FILE *fp = NULL;
  const char *ewhere, *ewhat = NULL;

  /* Create SDK session */
  if(!(ses=thfSessionCreate())) {
    panic(cons,"thfSessionCreate",thfGetLastError(NULL),0);
    free(context);
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 0;
  }
  displaySdkVersionAndExpirationDate(env, job);

  /* Check if PS_NET data files exist */
  sprintf(fname,"%s%s",appDir,PS_NET);
  if(!(fp=fopen(fname,"rb"))) {
    sprintf(str,"Missing data file: %s", PS_NET);
    disp(env,job,str);
    free(context);
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 0;
  }
  fclose(fp); fp=NULL;

  /* Create phrasespot recognizer */
  sprintf(str,"Loading recognizer: %s", PS_NET); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,PS_NET);
  if(!(psr=thfRecogCreateFromFile(ses,fname,0xffff,0xffff,NO_SDET)))
    THROW("recogCreateFromFile");

  /* Check if PS_SEARCH data files exist */
  sprintf(fname,"%s%s",appDir,PS_SEARCH);
  if(!(fp=fopen(fname,"rb"))) {
    sprintf(str,"Missing data file: %s", PS_SEARCH);
    disp(env,job,str);
    free(context);
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 0;
  }
  fclose(fp); fp=NULL;

  /* Create phrasespot search */
  sprintf(str,"Loading search: %s", PS_SEARCH); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,PS_SEARCH);
  if(!(pss=thfSearchCreateFromFile(ses,psr,fname,NBEST)))
    THROW("searchCreateFromFile");

  /* Check if GRAMMAR_NET data files exist */
  sprintf(fname,"%s%s",appDir,GRAMMAR_NET);
  if(!(fp=fopen(fname,"rb"))) {
    sprintf(str,"Missing data file: %s", GRAMMAR_NET);
    disp(env,job,str);
    free(context);
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 0;
  }
  fclose(fp); fp=NULL;

  /* Create recognizer */
  sprintf(str,"Loading recognizer: %s", GRAMMAR_NET); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,GRAMMAR_NET);
  if(!(r=thfRecogCreateFromFile(ses,fname,0xffff,0xffff,SDET)))
    THROW("recogCreateFromFile");

  /* Check if GRAMMAR_NET data files exist */
  sprintf(fname,"%s%s",appDir,GRAMMAR_SEARCH);
  if(!(fp=fopen(fname,"rb"))) {
    sprintf(str,"Missing data file: %s", GRAMMAR_SEARCH);
    disp(env,job,str);
    free(context);
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 0;
  }
  fclose(fp); fp=NULL;

  /* Create search */
  sprintf(str,"Loading search: %s", GRAMMAR_SEARCH); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,GRAMMAR_SEARCH);
  if(!(s=thfSearchCreateFromFile(ses,r,fname,NBEST)))
    THROW("searchCreateFromFile");

  /* Initialize recognizer */
  disp(env,job,"Initializing grammar recognizer...");
  if(!thfRecogInit(ses,r,s,RECOG_KEEP_WAVE_WORD_PHONEME)) THROW("thfRecogInit");

  beam=BEAM; garbage=0.5; any=0.5, nota=0.5f;
  if (!thfSearchConfigSet(ses,r,s,SCH_BEAM,beam))
    THROW("searchConfigSet: beam");
  if (!thfSearchConfigSet(ses,r,s,SCH_GARBAGE,garbage))
    THROW("searchConfigSet: garbage");
  if (!thfSearchConfigSet(ses,r,s,SCH_ANY,any))
    THROW("searchConfigSet: any");
  if (!thfSearchConfigSet(ses,r,s,SCH_NOTA,nota))
    THROW("searchConfigSet: nota");

  /* Configure grammar Recognition (REC) parameters (optional) */
  lsil=LSILENCE; maxR=MAXRECORD;
  if (!thfRecogConfigSet(ses, r, REC_LSILENCE, lsil))
    THROW("thfRecogConfigSet: lsil");
  if (!thfRecogConfigSet(ses, r, REC_MAXREC, maxR))
    THROW("thfRecogConfigSet: maxrec");
  if (!thfRecogConfigSet(ses, r, REC_KEEP_FEATURES, KEEP_FEATURES))
    THROW("thfRecogConfigSet: KEEP_FEATURES");

  /* Initialize phrasespot recognizer */
  disp(env,job,"Initializing phrasespot recognizer...");
  if (!thfRecogInit(ses, psr, pss, RECOG_KEEP_WAVE_WORD)) THROW("thfRecogInit");

  /* Configure phrasespot parameters - only DELAY... others are saved in search already */
  disp(env,job,"Setting phrasespot parameters...");
  if (!thfPhrasespotConfigSet(ses, psr, pss, PS_DELAY, PHRASESPOT_DELAY))
    THROW("thfPhrasespotConfigSet: delay");

  disp(env,job,"Vocabulary: <trigger>, <command>");
  disp(env,job,"Example: Hello Blue Genie, John Smith");

  // One second sequential buffer
  ps_seq_buffer = PHRASESPOT_SEQ_BUFFER;
  if (!thfPhrasespotConfigSet(ses, psr, pss, PS_SEQ_BUFFER, ps_seq_buffer))
    THROW("thfPhrasespotConfigSet: ps_seq_buffer");

  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  context->ses=ses;
  context->r=r;
  context->s=s;
  context->psr=psr;
  context->pss=pss;
  return (jlong)(long)context;

 error:
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(s)thfSearchDestroy(s);
  if(r)thfRecogDestroy(r);
  if(pss)thfSearchDestroy(pss);
  if(psr)thfRecogDestroy(psr);
  sprintf(str,"Panic - %s: %s\n\n",ewhere,ewhat); disp(env,job,str);
  if(ses)thfSessionDestroy(ses);
  free(context);
  return 0;
}

jlong Java_com_sensory_TrulyHandsfreeSDK_Console_recogSeqPhrasespotPipe(JNIEnv* env,
    jobject job, jlong arg, jobject buffer, jlong rate, jstring jappDir) {
  context_t *t=(context_t*)(long)arg;
  const char *appDir=(*env)->GetStringUTFChars(env, jappDir, NULL);
  thf_t *ses=t->ses;
  recog_t *r=t->r, *psr=t->psr;
  searchs_t *s=t->s, *pss=t->pss;
  float score;
  const char *ewhere, *ewhat=NULL, *rres=NULL;
  char str[MAXSTR];
  unsigned short status;
  short *sdata = (short*)(*env)->GetDirectBufferAddress(env, buffer);
  unsigned long sz = (unsigned long)(jlong)(*env)->GetDirectBufferCapacity(env, buffer)/2;

  if(rate!=(unsigned)thfRecogGetSampleRate(ses,psr)) {
	short *cspeech=NULL;
	unsigned long clen;
	if(!thfRecogSampleConvert(ses,psr,rate,sdata,sz,&cspeech,&clen))
	  THROW("recogSampleConvert");
	sprintf(str,"Converted samplerate: %d -> %d", (int)rate,(int)thfRecogGetSampleRate(ses,psr));
	disp(env,job,str);
	if(!thfRecogPipe(ses,psr,clen,cspeech,RECOG_ONLY,&status)) {
	  thfFree(cspeech);
	  THROW("recogPipe");
	}
	thfFree(cspeech);
  } else {
	if(!thfRecogPipe(ses,psr,sz,sdata,RECOG_ONLY,&status))
	  THROW("recogPipe");
  }
  switch (status) {
  case RECOG_QUIET:   disp(env,job,"quiet");	break;
  case RECOG_SOUND:   disp(env,job,"sound");break;
  case RECOG_IGNORE:  disp(env,job,"ignored");break;
  case RECOG_SILENCE: disp(env,job,"timeout: silence");break;
  case RECOG_MAXREC:  disp(env,job,"timeout: maxrecord"); break;
  case RECOG_DONE:    disp(env,job,"end-of-speech");break;
  case RECOG_NODATA:  disp(env,job,"no audio data");break;
  default:            disp(env,job,"other");break;
  }

  if (status==RECOG_QUIET || status==RECOG_SOUND || status==RECOG_IGNORE || status==RECOG_NODATA) {
	(*env)->ReleaseStringUTFChars(env, jappDir, appDir);
	return 1; // no result yet
  }

  if (status==RECOG_DONE || status==RECOG_MAXREC) {
	disp(env,job,"Recognition results...");
	score=0;
	const char *walign, *palign;
	if (!thfRecogResult(ses,psr,&score,&rres,&walign,&palign,NULL,NULL,NULL,NULL))
	  THROW("thfRecogResult");
    sprintf(str,"Phrasespot Result: %s (%.3f)",rres,score);
	disp(env,job,str);
  } else {
	disp(env,job,"No recognition result");
  }
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);

  if (!thfRecogPrepSeq(ses, r, psr))
	THROW("thfRecogPrepSeq");
  if (!thfRecogReset(ses, psr))
	THROW("thfRecogReset");
  return 2;

 error:
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(pss)thfSearchDestroy(pss);
  if(psr)thfRecogDestroy(psr);
  if(s)thfSearchDestroy(s);
  if(r)thfRecogDestroy(r);
  sprintf(str,"Panic - %s: %s",ewhere,ewhat); disp(env,job,str);
  if(ses) thfSessionDestroy(ses);
  free(t);
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  return 0;
}

jlong Java_com_sensory_TrulyHandsfreeSDK_Console_recogSeqGrammarPipe( JNIEnv* env,jobject job,jlong arg,jobject buffer,jlong rate, jstring jappDir)
{
  context_t *t=(context_t*)(long)arg;
  const char *appDir=(*env)->GetStringUTFChars(env, jappDir, NULL);
  thf_t *ses=t->ses;
  recog_t *r=t->r, *psr=t->psr;
  searchs_t *s=t->s, *pss=t->pss;
  float score,from,to;
  const char *ewhere, *ewhat=NULL;
  unsigned short i, status;
  const char *rres=NULL;
  FILE *fp=NULL;
  char str[MAXSTR], fname[MAXSTR];
  short *sdata = (short*)(*env)->GetDirectBufferAddress(env, buffer);
  unsigned long sz = (unsigned long)(jlong)(*env)->GetDirectBufferCapacity(env, buffer)/2;

  if(rate!=(unsigned)thfRecogGetSampleRate(ses,r)) {
    short *cspeech=NULL;
    unsigned long clen;
    if(!thfRecogSampleConvert(ses,r,rate,sdata,sz,&cspeech,&clen))
      THROW("recogSampleConvert");
    sprintf(str,"Converted samplerate: %d -> %d", (int)rate,(int)thfRecogGetSampleRate(ses,psr));
    disp(env,job,str);
    if(!thfRecogPipe(ses,r,clen,cspeech,SDET_RECOG,&status)) {
      free(cspeech);
      THROW("recogPipe");
    }
    free(cspeech);
  } else {
    if(!thfRecogPipe(ses,r,sz,sdata,SDET_RECOG,&status))
      THROW("recogPipe");
  }
  switch (status) {
  case RECOG_QUIET:   disp(env,job,"quiet");	break;
  case RECOG_SOUND:   disp(env,job,"sound");break;
  case RECOG_IGNORE:  disp(env,job,"ignored");break;
  case RECOG_SILENCE: disp(env,job,"timeout: silence");break;
  case RECOG_MAXREC:  disp(env,job,"timeout: maxrecord"); break;
  case RECOG_DONE:    disp(env,job,"end-of-speech");break;
  case RECOG_NODATA:  disp(env,job,"no audio data");break;
  default:            disp(env,job,"other");break;
  }

  if (status==RECOG_QUIET || status==RECOG_SOUND || status==RECOG_IGNORE || status==RECOG_NODATA) {
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 1; // no result yet
  }

  if (status==RECOG_DONE || status==RECOG_MAXREC) {
    float clip;
    thfRecogGetSpeechRange(ses,r,&from,&to);
    sprintf(str,"Found speech from %ld to %ld ms",(long)from,(long)to);disp(env,job,str);
    if (!thfRecogGetClipping(ses,r,&clip)) THROW("thfRecogGetClipping");
    if (clip>0.0) disp(env,job,"WARNING: Audio is clipped");

    disp(env,job,"Recognition results...");
    score=0;
    for(i=0; i<NBEST; i++) {
      const char *walign, *palign;
      unsigned long inSpeechSamples, sdetSpeechSamples;
      const short *inSpeech, *sdetSpeech;
      if (!thfRecogResult(ses,r,&score,&rres,&walign,&palign,&inSpeech,
			  &inSpeechSamples,&sdetSpeech,&sdetSpeechSamples))
	    THROW("thfRecogResult");
      if (rres==0) break;
      sprintf(str,"Result %i: %s (%.3f)",i+1,rres,score); disp(env,job,str);

      // Save the recorded and speech detected audio - once only
      if(!i) {
   	    int srate = thfRecogGetSampleRate(ses,r);
		if(inSpeech) {
		  sprintf(fname,"%srecorded.wav",appDir);
		  if(!thfWaveSaveToFile(ses,fname,(short *)inSpeech,inSpeechSamples,srate))
			THROW("thfWaveSaveToFile Recorded");
		}
		if(sdetSpeech) {
		  sprintf(fname,"%sdetected.wav",appDir);
		  if(!thfWaveSaveToFile(ses,fname,(short *)sdetSpeech,sdetSpeechSamples,srate))
			THROW("thfWaveSaveToFile Detected");
		}
      }
      // save word alignments
      if(walign) {
		sprintf(fname,"%sresult_%i_best.wrd",appDir,i+1);
		if(!(fp=fopen(fname,"wt"))) THROW("Opening word alignment file");
		fprintf(fp,"MillisecondsPerFrame: 1\nEND OF HEADER\n");
		fwrite(walign,1,strlen(walign),fp);
		fclose(fp); fp=NULL;
		//disp(env,job,"word alignment:");
		//disp(env,job,(char *)walign);
      } else  disp(env,job,"No word alignment");
      // save phoneme alignments
      if(palign) {
		sprintf(fname,"%sresult_%i_best.phn",appDir,i+1);
		if(!(fp=fopen(fname,"wt"))) THROW("Opening word alignment file");
		fprintf(fp,"MillisecondsPerFrame: 1\nEND OF HEADER\n");
		fwrite(palign,1,strlen(palign),fp);
		fclose(fp); fp=NULL;
		//disp(env,job,"phoneme alignment:");
		//disp(env,job,(char *)palign);
      } else  disp(env,job,"No phoneme alignment");
    }
  } else {
    disp(env,job,"No recognition result");
  }
  if (!thfRecogReset(ses,r))
    THROW("thfRecogReset");
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  return 2;

 error:
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(pss)thfSearchDestroy(pss);
  if(psr)thfRecogDestroy(psr);
  if(s)thfSearchDestroy(s);
  if(r)thfRecogDestroy(r);
  sprintf(str,"Panic - %s: %s",ewhere,ewhat); disp(env,job,str);
  if(ses) thfSessionDestroy(ses);
  free(t);
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  return 0;
}


void Java_com_sensory_TrulyHandsfreeSDK_Console_recogSeqClose( JNIEnv* env,jobject job, jlong arg )
{
  context_t *t=(context_t*)(long)arg;
  thf_t *ses=t->ses;
  recog_t *r=t->r, *psr=t->psr;
  searchs_t *s=t->s, *pss=t->pss;

  if(pss)thfSearchDestroy(pss);
  if(psr)thfRecogDestroy(psr);
  if(s)thfSearchDestroy(s);
  if(r)thfRecogDestroy(r);
  if(ses) thfSessionDestroy(ses);
  disp(env,job,"Done");
  free(t);
  return;
}

