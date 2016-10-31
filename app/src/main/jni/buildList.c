/*
 * Copyright (C)2000-2015 Sensory Inc.
 *
 * See the file "license.sensory" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.

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
/* set this flag to create pronunciations programmatically */
#define EXPLICIT_PRONUN 1
#define NBEST 3
#define TEXTNORMCONDITION   0x3800    /* Note: value is language dependent */

void Java_com_sensory_TrulyHandsfreeSDK_Console_buildList(JNIEnv* env,
		jobject job, jstring jappDir) {
	recog_t *r = NULL;
	searchs_t *s = NULL;
	pronuns_t *p = NULL;
	thf_t *ses = NULL;
	char **vocab = NULL, **pronun = NULL;
	const char *ewhere, *ewhat = NULL;
	FILE *fp = NULL;
	unsigned short count = 0, i;
	char line[MAXSTR], str[MAXSTR], fname[MAXSTR];
	const char *pstr = NULL;
	const char *appDir = (*env)->GetStringUTFChars(env, jappDir, 0);

	if (!(ses = thfSessionCreate())) {
		panic(cons, "thfSessionCreate", thfGetLastError(NULL), 0);
		return;
	}
	displaySdkVersionAndExpirationDate(env, job);

	/* Create recognizer */
	sprintf(str, "Loading recognizer: %s", NETFILE);
	disp(env, job, str);
	sprintf(fname, "%s%s", appDir, NETFILE);
	if (!(r = thfRecogCreateFromFile(ses, fname, 250, (unsigned short) -1, NO_SDET)))
		THROW("recogCreateFromFile");

	/* Create pronunciation object */
	sprintf(str, "Loading pronunciation model: %s", LTSFILE);
	disp(env, job, str);
	sprintf(fname, "%s%s", appDir, LTSFILE);
	if (!(p = thfPronunCreateFromFile(ses, fname, 0)))
		THROW("pronunCreateFromFile");

	/* Read vocabulary file */
	sprintf(str, "Reading vocab file: %s", NAMES_VOCABFILE);
	sprintf(fname, "%s%s", appDir, NAMES_VOCABFILE);
	disp(env, job, str);
	if (!(fp = fopen(fname, "r")))
		THROW2(NAMES_VOCABFILE, "Could not open file");
	vocab = NULL;
	for (count = 0; !feof(fp);) {
		int sz;
		line[0] = 0;
		fgets(line, 1024, fp);
		LOG("line=(%s)", line);
		if ((sz = strlen(line) - 1) > 0) {
			while (sz > 0)
				if (line[sz] == '\r' || line[sz] == '\n')
					line[sz--] = 0;
				else
					break;
			if (strcmp("*nota", line)) {
				if (!(pstr = thfTextNormalize(ses, p, line, TEXTNORMCONDITION))) // Do not normalize *nota to avoid bad pronunciation
					THROW("thfTextNormalize failed");
				LOG("tnorm=(%s)", line);
			} else
				pstr = line;
			sz = strlen(pstr);
			if (sz>0) {
				LOG("vocab %i=(%s)", count,pstr);
				count++;
				vocab = realloc(vocab, count * sizeof(char*));
				vocab[count - 1] = strcpy(malloc(sz + 1), pstr);
			}
			if (pstr && pstr!=line) {
			  thfFree((char *)pstr);
			  pstr = NULL;
			}
		}
	}
	fclose(fp);
	fp = NULL;

#if EXPLICIT_PRONUN
	/* Create search using explicitly-generated pronunciations */
	disp(env, job, "Generating pronunciations...");
	pronun = malloc(count * sizeof(char*));
	for (i = 0; i < count; i++) {
		char *pro;
		disp(env, job, vocab[i]);
		if (!(pro = (char *) thfPronunCompute(ses, p, vocab[i], 1, NULL, NULL )))
			THROW("pronunCompute");
		pronun[i] = strcpy(malloc(strlen(pro) + 1), pro);
		disp(env, job, pro);
	}
	disp(env, job, "Compiling search...");
	if (!(s = thfSearchCreateFromList(ses, r, p, (const char**) vocab,
			(const char**) pronun, count, NBEST)))
		THROW("searchCreateFromList");
	for (i = 0; i < count; i++)
		free((void *) pronun[i]);
	free((void *) pronun);
	pronun = NULL;
#else
	/* Use automatic pronunciations */
	disp(env,job,"Compiling search...");
	if(!(s=thfSearchCreateFromList(ses,r,p,(const char**)vocab,NULL,count)))
	THROW("searchCreateFromList");
#endif

	/* Save search */
	sprintf(str, "Saving search: %s", NAMES_SEARCHFILE);
	disp(env, job, str);
	sprintf(fname, "%s%s", appDir, NAMES_SEARCHFILE);
	if (!(thfSearchSaveToFile(ses, s, fname)))
		THROW("searchSaveToFile");

	/* Clean Up */
	for (i = 0; i < count; i++)
		free((void *) vocab[i]);
	free((void *) vocab);
	vocab = NULL;
	thfPronunDestroy(p);
	p = NULL;
	thfRecogDestroy(r);
	r = NULL;
	thfSearchDestroy(s);
	s = NULL;
	thfSessionDestroy(ses);
	ses = NULL;
	disp(env, job, "Done");
	return;

	error: if (!ewhat && ses)
		ewhat = thfGetLastError(ses);
	if (fp)
		fclose(fp);
	if (pronun) {
		for (i = 0; i < count; i++)
			free((void *) pronun[i]);
		free((void *) pronun);
	}
	if (vocab) {
		for (i = 0; i < count; i++)
			free((void *) vocab[i]);
		free((void *) vocab);
	}
	if (p)
		thfPronunDestroy(p);
	if (r)
		thfRecogDestroy(r);
	if (s)
		thfSearchDestroy(s);
	sprintf(str, "Panic - %s: %s\n\n", ewhere, ewhat);
	disp(env, job, str);
	if (ses)
		thfSessionDestroy(ses);
	return;
}
