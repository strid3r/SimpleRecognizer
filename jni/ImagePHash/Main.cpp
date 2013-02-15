/*
 * Copyright (C) 2013 strider
 *
 * Simple Recognizer
 * ImagePHash Main.cpp
 * By © strider 2013.
 */

#include <jni.h>

#include "ImagePHash.h"

extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;

	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
		return JNI_ERR;
	}

	if (ImagePHash::registerNative(env) != JNI_OK) {
		return JNI_ERR;
	}

	return JNI_VERSION_1_6;
}

