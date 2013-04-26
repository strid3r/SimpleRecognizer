/*
 * Copyright (C) 2013 strider
 *
 * Simple Recognizer
 * ImagePHash ImagePHash.hpp
 * By © strider 2013.
 */

#ifndef _RU_STRIDER_SIMPLERECOGNIZER_UTIL_IMAGEPHASH_HPP
#define _RU_STRIDER_SIMPLERECOGNIZER_UTIL_IMAGEPHASH_HPP

#include <jni.h>

extern "C++" {

	/*
	 * Class:     ru_strider_simplerecognizer_util_ImagePHash
	 */
	namespace ru_strider_simplerecognizer_util_ImagePHash {

		static const char* CLASS_NAME = "ru/strider/simplerecognizer/util/ImagePHash";

		/*
		 * Method:    nativeApplyDCT
		 * Signature: ([[D)[[D
		 */
		static jobjectArray nativeApplyDCT(JNIEnv* env, jclass clazz, jobjectArray in);

		extern jint registerNative(JNIEnv* env);

	} // namespace ru_strider_simplerecognizer_util_ImagePHash

} // extern C++

namespace ImagePHash = ru_strider_simplerecognizer_util_ImagePHash;

#endif // _RU_STRIDER_SIMPLERECOGNIZER_UTIL_IMAGEPHASH_HPP

