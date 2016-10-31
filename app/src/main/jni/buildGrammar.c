/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 *---------------------------------------------------------------------------
 * Sensory TrulyHandsfree SDK Example: buildGrammar.c
 * Compiles a grammar specification into binary search file.
 * - Creates compiled search file: time.search
 *---------------------------------------------------------------------------
 */
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "common.h" // Data files defined here

#define NBEST 3

void Java_com_sensory_TrulyHandsfreeSDK_Console_buildGrammar( JNIEnv* env,jobject job,jstring jappDir)
{
  const char *appDir=(*env)->GetStringUTFChars(env, jappDir, 0);
  thf_t *ses=NULL;
  recog_t *r=NULL;
  searchs_t *s=NULL;
  pronuns_t *p=NULL;
  const char *ewhere=NULL, *ewhat=NULL;
  char str[MAXSTR], fname[MAXSTR];
  char *gra = "hour    = one | two | three | four | five | six | \
                          seven | eight | nine | ten | eleven | twelve; \
               ampm    = am | pm; \
               halfday = noon | midnight; \
               subhour = thirty | oclock; \
               gra     = *sil%% (*nota | [twelve] $halfday | $hour [$subhour] [$ampm]) *sil%%;";
  unsigned short count = 20;
  const char *w0="oclock",*w1="am",*w2="pm",*w3="one",*w4="two",*w5="three",*w6="four",
    *w7="five",*w8="six",*w9="seven",*w10="eight",*w11="nine",*w12="ten",*w13="eleven",
    *w14="twelve",*w15="noon",*w16="midnight",*w17="thirty",*w18="*sil",*w19="*nota"; 
  const char *word[] = {w0,w1,w2,w3,w4,w5,w6,w7,w8,w9,w10,w11,w12,w13,w14,w15,w16,
			w17,w18,w19};
  const char *p0=". oU . k l A1 k .",
    *p1=". eI1 . E1 m .",
    *p2=". p i1 . E1 m .",
    *p3=". w ^1 n .",
    *p4=". t u1 .",
    *p5=". T 9 i1 .",
    *p6=". f >1 9 .",
    *p7=". f aI1 v .",
    *p8=". s I1 k s .",
    *p9=". s E1 . v ix n .",
    *p10=". eI1 t .",
    *p11=". n aI1 n .",
    *p12=". t E1 n .",
    *p13=". I . l E1 . v ix n .",
    *p14=". t w E1 l v .",
    *p15=". n u1 n .",
    *p16=". m I1 d . n aI t .",
    *p17=". T 3r1 . t i .",
    *p18=". .pau .",
	*p19=". .nota .";
  const char *pronun[] = {p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19};
#ifdef __PALMOS__
  void *inst = (void*)(UInt32)cmd;
#endif

  /* Create SDK session */
  if(!(ses=thfSessionCreate())) {
		panic(cons,"thfSessionCreate",thfGetLastError(NULL),0);
		return;
  }
  displaySdkVersionAndExpirationDate(env, job);

  /* Create recognizer */
  sprintf(str,"Loading recognizer: %s", NETFILE); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,NETFILE); 
  if(!(r=thfRecogCreateFromFile(ses,fname,250,0xffff,NO_SDET)))
    THROW("thfRecogCreateFromFile");

  /* Create pronunciation object */
  sprintf(str,"Loading pronunciation model: %s", LTSFILE); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,LTSFILE); 
  if(!(p=thfPronunCreateFromFile(ses,fname,0)))
    THROW("pronunCreateFromFile");

  /* Create search */
  disp(env,job,"Compiling search...");
  if(!(s=thfSearchCreateFromGrammar(ses,r,p,gra,word,pronun,count,NBEST, NO_PHRASESPOTTING))) 
    THROW("thfSearchCreateFromGrammar");

  /* Save search */
  sprintf(str,"Saving search: %s", TIME_SEARCHFILE); disp(env,job,str);
  sprintf(fname,"%s%s", appDir,TIME_SEARCHFILE); 
  if(!(thfSearchSaveToFile(ses,s,fname))) 
    THROW("thfSearchSaveToFile");
  
  /* Clean Up */
  thfPronunDestroy(p);   
  thfSearchDestroy(s);
  thfRecogDestroy(r);
  thfSessionDestroy(ses);

  disp(env,job,"Done");
  return;

 error:
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(s) thfSearchDestroy(s);
  if(r) thfRecogDestroy(r);
  sprintf(str,"Panic - %s: %s\n\n",ewhere,ewhat);  disp(env,job,str);
  if(ses) thfSessionDestroy(ses);
  return; 
}
