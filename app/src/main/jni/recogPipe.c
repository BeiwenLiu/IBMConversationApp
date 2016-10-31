/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 *---------------------------------------------------------------------------
 * Sensory TrulyNatural SDK Example : recogPipe.c
 * Pipelined recognition using live audio input.
 *  - Uses simple explicit audio interface
 *  - Records up to 5000ms
 *  - Illustrates pipelined recognition
 *  - Uses pre-compiled vocabulary built with buildList.c
 *---------------------------------------------------------------------------
 */

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "common.h"

#define BEAM           (100.f)              /* Pruning beam */
#define LSILENCE       (20000.f)            /* Maximum silence allowed */
#define MAXRECORD      (10000.f)            /* Maximum record time */
#define NBEST          (3)                  /* Max number of results */

jlong Java_com_sensory_TrulyHandsfreeSDK_Console_recogPipeInit( JNIEnv* env,jobject job,jstring jappDir)
{
  context_t *context=malloc(sizeof(context_t));
  const char *appDir;
  thf_t *ses=NULL;
  recog_t *r=NULL;
  searchs_t *s=NULL;
  const char *ewhere, *ewhat=NULL;
  float beam,garbage,any,nota;
  float lsil, maxR;
  FILE *fp=NULL;
  char str[MAXSTR], fname[MAXSTR];
	
  /* Check if data files exist */
  appDir=(*env)->GetStringUTFChars(env, jappDir, NULL);
  sprintf(fname,"%s%s",appDir,NAMES_SEARCHFILE);
  if(!(fp=fopen(fname,"rb"))) {
    sprintf(str,"Missing data file: Use 'BuildList' Sample to create file: %s",NAMES_SEARCHFILE);
    disp(env,job,str);
    free(context);
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 0;
  }
  fclose(fp); fp=NULL;

  /* Create SDK session */
  if(!(ses=thfSessionCreate())) {
    panic(cons,"thfSessionCreate",thfGetLastError(NULL),0);
    free(context);
    (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
    return 0;
  }
  displaySdkVersionAndExpirationDate(env, job);

  /* Create recognizer */
  sprintf(str,"Loading recognizer: %s",NETFILE); disp(env,job,str);
  sprintf(fname,"%s%s",appDir,NETFILE);
  if(!(r=thfRecogCreateFromFile(ses,fname,0xFFFF,-1,SDET)))
    THROW("thfRecogCreateFromFile");

  /* Create search */
  sprintf(str,"Loading search: %s", NAMES_SEARCHFILE); disp(env,job,str);
  sprintf(fname,"%s%s",appDir,NAMES_SEARCHFILE);
  if(!(s=thfSearchCreateFromFile(ses,r,fname,NBEST)))
    THROW("thfSearchCreateFromFile");

  /* Configure parameters */
  disp(env,job,"Setting parameters...");


  beam=BEAM; garbage=0.5f; any=0.5f, nota=0.5f;
  if (!thfSearchConfigSet(ses,r,s,SCH_BEAM,beam))
    THROW("searchConfigSet: beam");
  if (!thfSearchConfigSet(ses,r,s,SCH_GARBAGE,garbage))
    THROW("searchConfigSet: garbage");
  if (!thfSearchConfigSet(ses,r,s,SCH_ANY,any))
    THROW("searchConfigSet: any");
  if (!thfSearchConfigSet(ses,r,s,SCH_NOTA,nota))
    THROW("searchConfigSet: nota");

  lsil=LSILENCE; maxR=MAXRECORD;
  if (!thfRecogConfigSet(ses, r, REC_LSILENCE, lsil))
    THROW("thfRecogConfigSet: lsil");
  if (!thfRecogConfigSet(ses, r, REC_MAXREC, maxR))
    THROW("thfRecogConfigSet: maxrec");
  
  /* Initialize recognizer */
  disp(env,job,"Initializing recognizer...");
  if(!thfRecogInit(ses,r,s,RECOG_KEEP_WAVE_WORD_PHONEME)) 
    THROW("thfRecogInit");

  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  context->ses=ses;
  context->r=r;
  context->s=s;
  return (jlong)(long)context;

 error:
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(s)thfSearchDestroy(s);
  if(r)thfRecogDestroy(r);
  sprintf(str,"Panic - %s: %s\n\n",ewhere,ewhat); disp(env,job,str);
  if(ses)thfSessionDestroy(ses);
  free(context);
  return 0; 
}

jlong Java_com_sensory_TrulyHandsfreeSDK_Console_recogPipePipe( JNIEnv* env,jobject job,jlong arg,jobject buffer,jlong rate, jstring jappDir)
{
  context_t *t=(context_t*)(long)arg;
  const char *appDir=(*env)->GetStringUTFChars(env, jappDir, NULL);
  thf_t *ses=t->ses;
  recog_t *r=t->r;
  searchs_t *s=t->s;
  float score,from,to;
  const char *ewhere, *ewhat=NULL;
  unsigned short i;
  FILE *fp=NULL;
  const char *rres=NULL;
  char str[MAXSTR],fname[MAXSTR];
  unsigned short status;
  short *sdata = (short*)(*env)->GetDirectBufferAddress(env, buffer);
  unsigned long sz = (unsigned long)(jlong)(*env)->GetDirectBufferCapacity(env, buffer)/2;

  if(rate!=(unsigned)thfRecogGetSampleRate(ses,r)) {
    short *cspeech=NULL;
    unsigned long clen;
    if(!thfRecogSampleConvert(ses,r,rate,sdata,sz,&cspeech,&clen))
      THROW("recogSampleConvert");
    sprintf(str,"Converted samplerate: %d -> %d", (int)rate,(int)thfRecogGetSampleRate(ses,r));
    disp(env,job,str);
    if(!thfRecogPipe(ses,r,clen,cspeech,SDET_RECOG,&status)) {
      thfFree(cspeech);
      THROW("recogPipe");
    }
    thfFree(cspeech);
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
      if (rres==0) {
	break;
      }
      else if (i==0 && !strcmp(rres,"*nota")) {
	disp(env,job,"NOTA recognition --- ignored");
	thfRecogReset(ses,r);
	(*env)->ReleaseStringUTFChars(env, jappDir, appDir);
	return 1; // no result yet
      }
      else {
	sprintf(str,"Result %i: %s (%.3f)",i+1,rres,score); disp(env,job,str);
      
	/* Save the recorded and speech detected audio - once only */
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
	/* save word alignments */
	if(walign) {
	  sprintf(fname,"%sresult_%i_best.wrd",appDir,i+1);
	  if(!(fp=fopen(fname,"wt"))) THROW("Opening word alignment file");
	  fprintf(fp,"MillisecondsPerFrame: 1\nEND OF HEADER\n");
	  fwrite(walign,1,strlen(walign),fp);
	  fclose(fp); fp=NULL;
	  disp(env,job,"word alignment:");
	  disp(env,job,(char *)walign);
	} else  disp(env,job,"No word alignment");
	/* save phoneme alignments */
	if(palign) {
	  sprintf(fname,"%sresult_%i_best.phn",appDir,i+1);
	  if(!(fp=fopen(fname,"wt"))) THROW("Opening word alignment file");
	  fprintf(fp,"MillisecondsPerFrame: 1\nEND OF HEADER\n");
	  fwrite(palign,1,strlen(palign),fp);
	  fclose(fp); fp=NULL;
	  disp(env,job,"phoneme alignment:");
	  disp(env,job,(char *)palign);
	} else  disp(env,job,"No phoneme alignment");
      }
    }
  } else {
    disp(env,job,"No recognition result");
  }
  thfRecogReset(ses,r);
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  return 2;

 error:
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(s)thfSearchDestroy(s);
  if(r)thfRecogDestroy(r);
  sprintf(str,"Panic - %s: %s",ewhere,ewhat); disp(env,job,str);
  if(ses) thfSessionDestroy(ses);
  free(t);
  (*env)->ReleaseStringUTFChars(env, jappDir, appDir);
  return 0; 
}


void Java_com_sensory_TrulyHandsfreeSDK_Console_recogPipeClose( JNIEnv* env,jobject job, jlong arg )
{
  context_t *t=(context_t*)(long)arg;
  thf_t *ses=t->ses;
  recog_t *r=t->r;
  searchs_t *s=t->s;
  if(r)thfRecogDestroy(r); r=NULL;
  if(s)thfSearchDestroy(s); s=NULL;
  if(ses)thfSessionDestroy(ses); ses=NULL;
  disp(env,job,"Done");
  return;
}
