/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 *---------------------------------------------------------------------------
 * Sensory TrulyHandsfree SDK Example: recogGrammar.c
 * Creates a recognizer and performs recognition on a speech file.
 *  - Uses RAW PCM audio file as input
 *  - Uses pre-compiled vocabulary built with buildGrammar.c
 *  - Illustrates pipelined recognition
 *  - Illustrates how to calibrate speech detector
 *---------------------------------------------------------------------------
 */
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "common.h"

#define BEAM    (200.f) 
#define NBEST   (3)
#define BLOCKSZ (2000)   /* 250 ms blocksize */

void checkEndian(unsigned short inlen,short *inbuf)
{
  unsigned short i=1;
  unsigned char *p=(unsigned char *)&i;
#ifndef __PALMOS__
  if (*p==1) return; /* little endian */
#else
  if (*p==0) return; /* big endian, don't swap */
#endif
  for (i=0, p=(unsigned char *)inbuf; i<inlen; i++, p+=2) {
    unsigned char c;
    c = *p; *p = *(p+1); *(p+1) = c; 
  }
}

void Java_com_sensory_TrulyHandsfreeSDK_Console_recogGrammar( JNIEnv* env,jobject job, jstring jappDir)
{
  const char *appDir=(*env)->GetStringUTFChars(env, jappDir, 0);
  const char *rres;
  thf_t *ses=NULL;
  recog_t *r=NULL;
  searchs_t *s=NULL;
  short *inbuf=NULL;
  float score, from, to;
  unsigned short i, status;
  unsigned short inlen;
  FILE *fp=NULL;
  float beam, garbage, any;
  const char *ewhere, *ewhat=NULL;
  char str[MAXSTR], fname[MAXSTR];
	
  /* Check if data files exist */
  sprintf(fname,"%s%s", appDir,TIME_SEARCHFILE); 
  if(!(fp=fopen(fname,"rb"))) {
    THROW("Missing data file: Use 'BuildGrammar' Sample to create file:"
	    TIME_SEARCHFILE);
  }
  fclose(fp); fp=NULL;

  /* Create SDK session */
  if(!(ses=thfSessionCreate())) {
    panic(cons,"thfSessionCreate",thfGetLastError(NULL),0);
    return;
  }
  displaySdkVersionAndExpirationDate(env, job);

  /* Create recognizer */
  sprintf(str,"Loading recognizer: %s", NETFILE); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,NETFILE); 
  if(!(r=thfRecogCreateFromFile(ses,fname,BLOCKSZ,-1,SDET)))
    THROW("recogCreateFromFile");

  /* Create search */
  sprintf(str,"Loading search: %s", TIME_SEARCHFILE); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,TIME_SEARCHFILE); 
  if(!(s=thfSearchCreateFromFile(ses,r,fname,NBEST)))
    THROW("searchCreateFromFile");

  /* Initialize recognizer */
  disp(env,job,"Initializing recognizer...");
  if(!thfRecogInit(ses,r,s,RECOG_KEEP_NONE)) THROW("thfRecogInit");

  /* Configure parameters (optional) */
  beam=BEAM; garbage=0.5; any=0.5;
  if (!thfSearchConfigSet(ses,r,s,SCH_BEAM,beam))
    THROW("searchConfigSet: beam");
  if (!thfSearchConfigSet(ses,r,s,SCH_GARBAGE,garbage))
    THROW("searchConfigSet: garbage");
  if (!thfSearchConfigSet(ses,r,s,SCH_ANY,any))
    THROW("searchConfigSet: any");
  
  /* Feed speech to recognizer in chunks until done 
     -- Simulates pipelined recognition. */
  disp(env,job,"Recognizing...");
  /* Open speech file (16kHz Raw 16-bit PCM) */
  sprintf(fname,"%s%s", appDir,AUDIOFILE); 
  if(!(fp=fopen(fname,"rb"))) {
    sprintf(str,"Failed to open audio file: %s",AUDIOFILE);
    THROW(str);
  }
  inbuf=malloc(BLOCKSZ*sizeof(short));

  for(inlen=0; !feof(fp);) {
    inlen = fread(inbuf,sizeof(short),BLOCKSZ,fp);
    if(inlen>0) {       
      checkEndian(inlen,inbuf);
		
	   if((unsigned)thfRecogGetSampleRate(ses,r)!=16000) { // convert 16kHz raw audio if needed
			short *cspeech=NULL;
			unsigned long clen;
			if(!thfRecogSampleConvert(ses,r,16000,inbuf,inlen,&cspeech,&clen))
				THROW("recogSampleConvert");
			thfFree(inbuf); 
			inbuf=cspeech;
			inlen=clen;
			disp(env,job,"Converted samplerate");
		}
		
		
      if(!thfRecogPipe(ses,r,inlen,inbuf,SDET_RECOG,&status))
	THROW("recogPipe");
      switch (status) {
      case RECOG_QUIET:   disp(env,job,"quiet"); break;
      case RECOG_SILENCE: disp(env,job,"silence"); break;
      case RECOG_SOUND:   disp(env,job,"speech"); break;
      case RECOG_IGNORE:  disp(env,job,"ignore"); break;
      case RECOG_DONE:    disp(env,job,"done"); break;
      case RECOG_MAXREC:  disp(env,job,"maxrec"); break;
      }
      if(status!=RECOG_QUIET && status!=RECOG_SOUND) break;
    }
  }
  fclose(fp); fp=NULL;
  /* Report speech detector state */
  switch (status) {
  case RECOG_QUIET:   disp(env,job,"No speech detected"); break;
  case RECOG_SILENCE: disp(env,job,"Time out -- not speech detected"); break;
  case RECOG_SOUND:   disp(env,job,"Truncated speech"); break;
  case RECOG_IGNORE:  disp(env,job,"Non speech sound detected"); break;
  case RECOG_DONE:    disp(env,job,"Speech detected"); break;
  case RECOG_MAXREC:  disp(env,job,"Too much speech"); break;
  }
 
  /* Report N-best recognition result */
  rres=NULL;
  if (status==RECOG_DONE || status==RECOG_MAXREC || status==RECOG_SOUND) {
    thfRecogGetSpeechRange(ses,r,&from,&to);
    sprintf(str,"Found speech from %ld to %ld ms",(long)from,(long)to); disp(env,job,str);
    score=0;
    for(i=1;i<=NBEST;i++) {
      if (!thfRecogResult(ses,r,&score,&rres,NULL,NULL,NULL,NULL,NULL,NULL)) 
	THROW("recogResult");
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
  free(inbuf);
  thfSearchDestroy(s);
  thfRecogDestroy(r);
  thfSessionDestroy(ses);
  disp(env,job,"Done");
  return;

 error:
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(fp) fclose(fp);
  if(inbuf) free(inbuf);
  if(s) thfSearchDestroy(s);
  if(r) thfRecogDestroy(r);
  sprintf(str,"Panic - %s: %s\n\n",ewhere,ewhat); disp(env,job,str);
  if(ses) thfSessionDestroy(ses);
  return; 
}








