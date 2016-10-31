/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *---------------------------------------------------------------------------
 * Sensory TrulyHandsfree SDK Example: buildList.c
 * Compiles a vocabulary text file into binary search file.
 *   - Creates compiled search file: names.search
 *---------------------------------------------------------------------------
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"  // Data files defined here

#define NBEST 3

void Java_com_sensory_TrulyHandsfreeSDK_Console_buildIncremental( JNIEnv* env,jobject job, jstring jappDir)
{
  const char *appDir=(*env)->GetStringUTFChars(env, jappDir, 0);
  recog_t *r=NULL;
  searchs_t *s=NULL;
  pronuns_t *p=NULL;
  thf_t *ses=NULL;
  const char *ewhere, *ewhat=NULL;
  char str[MAXSTR], fname[MAXSTR];
  const char *addGrams1[] = {"g=*sil call John Smith [at (cell | home | work)] *sil;", 
			     "g=*sil call Jane Doe [at home] *sil;"};
  const char *words1[] = {"call", "John", "Smith", "Jane", "Doe", "at", "cell", 
			  "home", "work", "*sil"};
  /* remGrams2 specifies what gets removed on the second call to thfSearchCreateIncremental.  
     NOTE: if we were to remove "g=*sil call John Smith [at work] *sil;", then this would remove
     both "call John Smith at work" and "call John Smith", leaving just
     "call John Smith at (cell | home);", which isn't what we want. */
  const char *remGrams2[] = {"g=*sil call John Smith at work *sil;"};
  const char *addGrams2[] = {"g=*sil call Jane Doe [at work] *sil;"};
  const char *words2[] = {"call", "John", "Smith", "Jane", "Doe", "at", "work", "*sil"};

  /* Create session */
  if(!(ses=thfSessionCreate())) {
		panic(cons,"thfSessionCreate",thfGetLastError(NULL),0);
		return;
  }
  displaySdkVersionAndExpirationDate(env, job);

  /* Create recognizer */
  disp(env,job,"Loading recognizer: " NETFILE);
  sprintf(fname,"%s%s",appDir,NETFILE);
  if(!(r=thfRecogCreateFromFile(ses,fname,(unsigned short)250,(unsigned short)-1,NO_SDET)))
    THROW("recogCreateFromFile");

  /* Create pronunciation object */
  disp(env,job,"Loading pronunciation model: " LTSFILE);
  sprintf(fname,"%s%s",appDir,LTSFILE);
  if(!(p=thfPronunCreateFromFile(ses,fname,0)))
    THROW("pronunCreateFromFile");

  /* Adding addGrams1 */
  s = thfSearchCreateIncremental(ses,r,NULL,p,NULL,0,addGrams1, 2, words1, 
				 NULL, 10, NBEST);
  if (!s)
    THROW("searchCreateIncremental");
  /* Removing remGrams2 and then adding addGrams2 */
  s = thfSearchCreateIncremental(ses,r,s,p,remGrams2,1,addGrams2,1,words2,NULL,
				 8,NBEST);
  if (!s)
    THROW("searchCreateIncremental");
  /* Save search */  
  disp(env,job,"Saving search: " INCREMENTAL_SEARCHFILE);
  sprintf(fname,"%s%s",appDir,INCREMENTAL_SEARCHFILE);
  if(!(thfSearchSaveToFile(ses,s,fname))) 
    THROW("searchSaveToFile");
  
  /* Clean Up */
  thfPronunDestroy(p);    p=NULL;
  thfRecogDestroy(r);     r=NULL;
  thfSearchDestroy(s);    s=NULL;
  thfSessionDestroy(ses); ses=NULL;
  disp(env,job,"Done");
  return;
  
 error:
  if(!ewhat && ses) ewhat=thfGetLastError(ses);
  if(p)   thfPronunDestroy(p);
  if(r)   thfRecogDestroy(r);
  if(s)   thfSearchDestroy(s);
  sprintf(str,"Panic - %s: %s\n\n",ewhere,ewhat); disp(env,job,str);
  if(ses) thfSessionDestroy(ses);
  return; 
}
