#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "bme_mokk_hunmorph_HunmorphStub.h"

#include "ocamorph.h"
#define MAX_ANALYSIS 100
#define ANALYSIS_MAXLEN 100

 // initialize the analysis string
  char analysis[ANALYSIS_MAXLEN];
  // initialize input buffer
  char buffer[500];
  char* analyses[MAX_ANALYSIS];

jmethodID MID_InstanceMethodCall_callback;


JNIEXPORT void JNICALL Java_bme_mokk_hunmorph_HunmorphStub_initIDs
  (JNIEnv *env, jclass cls) {

  MID_InstanceMethodCall_callback =
         (*env)->GetMethodID(env, cls, "callback", "([B)V");

}
JNIEXPORT jlong JNICALL Java_bme_mokk_hunmorph_HunmorphStub_init
  (JNIEnv * env, jobject obj, jstring bin_arg) {

 /* Convert to UTF8 */
  const char *bin_file  = (*env)->GetStringUTFChars(env, bin_arg, JNI_FALSE);

  ocamorph_startup();
  ocamorph_engine engine = init_from_bin(bin_file,0/*Don't pass the stupid no_caps argument*/);
	
  /* Release created UTF8 string */
  (*env)->ReleaseStringUTFChars(env, bin_arg, bin_file);

  int i;
  for (i=0; i<MAX_ANALYSIS;i++) {
    analyses[i] = (char *) malloc(ANALYSIS_MAXLEN * sizeof(char));
  };

  return  (jlong) engine;

}

JNIEXPORT jlong JNICALL Java_bme_mokk_hunmorph_HunmorphStub_make_1analyzer
  (JNIEnv *env, jobject obj, jlong engine , jint blocking, jint compunds, jint stop_at_first, jint guess) {

  ocamorph_engine analyzer = make_analyzer((ocamorph_engine) engine, blocking, compunds, stop_at_first, guess);

  return (jlong) analyzer;

}

JNIEXPORT void JNICALL Java_bme_mokk_hunmorph_HunmorphStub_analyze
  (JNIEnv * env, jobject obj, jlong analyzer, jbyteArray word) {

  ocamorph_engine analyzerc = (ocamorph_engine) analyzer;

  /* Convert to UTF8 */
  // const char *wordc  = (*env)->GetStringUTFChars(env, word, JNI_FALSE);

  //char *wordc =  (char *) (*env)->GetByteArrayElements( env, word, 0);
  
  const int maxInputLength = 1000;
  char wordc[maxInputLength];
  jsize len = (*env)->GetArrayLength(env,word);
  if (len>=maxInputLength) { len = maxInputLength-1; }
  
  if (len!=0)
  {
    (*env)->GetByteArrayRegion(env,word,0,len,(jbyte*)wordc);
  }
  wordc[len] = '\0';

  int n = analyze(analyzerc,wordc,analyses,MAX_ANALYSIS, ANALYSIS_MAXLEN);

  int i;
    for (i=0; i < n; ++i) {
      //  jstring ana = (*env)->NewStringUTF(env, analyses[i]);
      char* ana = analyses[i];
     jbyteArray jb=(*env)->NewByteArray(env, strlen(ana));
	 (*env)->SetByteArrayRegion(env, jb, 0, strlen(ana), (jbyte *)ana);
     (*env)->CallVoidMethod(env, obj, MID_InstanceMethodCall_callback, jb);

     }
//  (*env)->ReleaseStringUTFChars(env, word, wordc);
}
