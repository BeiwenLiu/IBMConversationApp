/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 *---------------------------------------------------------------------------
 * Sensory TrulyHandsfree SDK Example: recogList.c
 * Creates a recognizer and performs recognition on the entire WAVE file.
 *  - Uses RIFF wave file as input
 *  - Uses pre-compiled vocabulary built with buildList.c
 *  - Shows how to calibrate speech detector
 *---------------------------------------------------------------------------
 */
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "common.h"

#define BEAM    (200.f)
#define NBEST   (3)


void Java_com_sensory_TrulyHandsfreeSDK_Console_recogList( JNIEnv* env,jobject job,jstring jappDir)
{
  const char *appDir=(*env)->GetStringUTFChars(env, jappDir, 0);
  const char *rres;
  thf_t *ses=NULL;
  recog_t *r=NULL;
  searchs_t *s=NULL;
  short *inbuf=NULL;
  float score, from, to, beam, garbage, any, nota;
  unsigned short i, status;
  unsigned long inlen, sampleRate;
  const char *ewhere, *ewhat=NULL;
  char str[MAXSTR],fname[MAXSTR];
  FILE *fp=NULL;

  /* Check if data files exist */
  sprintf(fname,"%s%s",appDir,NAMES_SEARCHFILE);
  if(!(fp=fopen(fname,"rb"))) {
    sprintf(str,"Missing file: Use 'BuildList' Sample to create file: "
	    NAMES_SEARCHFILE);
    THROW(str);
  }
  fclose(fp); fp=NULL;

  /* Create SDK session */
  if(!(ses=thfSessionCreate())) {
    panic(cons,"thfSessionCreate",thfGetLastError(NULL),0);
    return;
  }
  displaySdkVersionAndExpirationDate(env, job);

  /* Read wave file (8kHz 16-bit PCM) */
  sprintf(str,"Reading wavefile: %s", WAVEFILE); disp(env,job,str);
  sprintf(fname,"%s%s",appDir,WAVEFILE);
  if(!(thfWaveFromFile(ses,fname,&inbuf,&inlen,&sampleRate))) 
    THROW2("thfWaveFromFile","Failed to load wavefile");
	
  /* Create recognizer */
  sprintf(str,"Loading recognizer: %s", NETFILE); disp(env,job,str);
  sprintf(fname,"%s%s",appDir,NETFILE);
  if(!(r=thfRecogCreateFromFile(ses,fname,0xFFFF,-1,SDET)))
    THROW("thfRecogCreateFromFile");
  
  /* Create search */
  sprintf(str,"Loading search: %s", NAMES_SEARCHFILE); disp(env,job,str);
  sprintf(fname,"%s%s",appDir,NAMES_SEARCHFILE);
  if(!(s=thfSearchCreateFromFile(ses,r,fname,NBEST)))
    THROW("thfSearchCreateFromFile");
 
  /* Configure parameters */
  beam=BEAM; garbage=0.5; any=0.5; nota=0.5f;
  if (!thfSearchConfigSet(ses,r,s,SCH_BEAM,beam))
    THROW("searchConfigSet: beam");
  if (!thfSearchConfigSet(ses,r,s,SCH_GARBAGE,garbage))
    THROW("searchConfigSet: garbage");
  if (!thfSearchConfigSet(ses,r,s,SCH_ANY,any))
    THROW("searchConfigSet: any");
  if (!thfSearchConfigSet(ses,r,s,SCH_NOTA,nota))
    THROW("searchConfigSet: nota");
  
  /* Initialize recognizer */
  disp(env,job,"Initializing recognizer...");
  if (!thfRecogInit(ses,r,s,RECOG_KEEP_NONE)) THROW("thfRecogInit");
    
  /* Convert input wave samplerate (if needed) */
  if(sampleRate!=(unsigned)thfRecogGetSampleRate(ses,r)) {
    short *cspeech=NULL;
    unsigned long clen;
    if(!thfRecogSampleConvert(ses,r,sampleRate,inbuf,inlen,&cspeech,&clen))
      THROW("recogSampleConvert");
    thfFree(inbuf); 
    inbuf=cspeech;
    inlen=clen;
    disp(env,job,"Converted samplerate");
  }

  /* Passes entire wave to recognizer as a single buffer */
  disp(env,job,"Speech detection and wave recognition...");
  if(!thfRecogPipe(ses,r,inlen,inbuf,SDET_RECOG,&status))
    THROW("thfRecogPipe");
  switch (status) {
  case RECOG_QUIET:         disp(env,job,"No speech detected");           break;
  case RECOG_SILENCE:       disp(env,job,"Time out--No speech detected"); break;
  case RECOG_SOUND:         disp(env,job,"Truncated speech");             break;
  case RECOG_IGNORE:        disp(env,job,"Non speech sound detected");    break;
  case RECOG_DONE:          disp(env,job,"Speech detected");              break;
  case RECOG_MAXREC:        disp(env,job,"Too much speech");              break;
  }

 /* Report N-best recognition result */
  rres=NULL;
  if (status==RECOG_DONE || status==RECOG_MAXREC || status==RECOG_SOUND) {
    disp(env,job,"Recognition result...");
    thfRecogGetSpeechRange(ses,r,&from,&to);
    sprintf(str,"Found speech from %ld to %ld ms",(long)from,(long)to); disp(env,job,str);
    { 
      float clip;
      if (!thfRecogGetClipping(ses,r,&clip)) THROW("recogGetClipping");
      if (clip>1.0) disp(env,job,"WARNING: Speech is clipped");
    }
    score=0;
    for(i=1;i<=NBEST;i++) {
      if(!thfRecogResult(ses,r,&score,&rres,NULL,NULL,NULL,NULL,NULL,NULL)) 
	THROW("thfRecogResult");
      if (rres==0) break; 
#ifndef __PALMOS__
      sprintf(str,"Result %i: %s (%.3f)",i,rres,score); disp(env,job,str);
#else
      sprintf(str,"Result %i: %s (%ld)",i,rres,(long)(1000*score+.5)); disp(env,job,str);
#endif
    }
  } else 
    disp(env,job,"No recognition result");

  /* Clean up */
  thfFree(inbuf); inbuf=NULL;
  thfSearchDestroy(s); s=NULL;
  thfRecogDestroy(r); r=NULL;
  thfSessionDestroy(ses); ses=NULL;
  disp(env,job,"Done");
  return;

 error:
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(inbuf) thfFree(inbuf);  
  if(s)     thfSearchDestroy(s);
  if(r)     thfRecogDestroy(r);
  sprintf(str,"Panic - %s: %s\n\n",ewhere,ewhat);  disp(env,job,str);
  if(ses)   thfSessionDestroy(ses);
  return; 
}
