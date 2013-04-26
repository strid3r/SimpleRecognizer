/*
 * Copyright (C) 2013 strider
 *
 * Simple Recognizer
 * ImagePHash ImagePHash.cpp
 * By © strider 2013.
 */

#include "ImagePHash.hpp"

//#include <android/log.h>

#include <jni.h>

#include <algorithm>
#include <vector>
#include <cmath>

typedef std::vector<std::vector<double> > pix_map_t;

template <typename T, unsigned S>
inline unsigned sizeOfArray(const T (&v)[S]) {
	return S;
}

static std::vector<double> coeff;

static void initCoefficients(int size) {
	if (size < 1) {
		return; // Illegal Argument //
	}

	if (coeff.empty() || (coeff.size() != size)) {
		coeff = std::vector<double>(size);

		if (coeff.empty()) {
			return; // OutOfMemoryError
		}

		coeff.front() = 1.0 / std::sqrt(2.0);

		std::fill(coeff.begin() + 1, coeff.end(), 1.0);
	}
}

static bool applyDCT(pix_map_t& data) {
	const int size = data.size();

	pix_map_t DCT = pix_map_t(size);

	if (DCT.empty()) {
		return false; // OutOfMemoryError
	}

	initCoefficients(size);

	for (int u = 0; u < size; u++) {
		int len = data[u].size();

		DCT[u] = std::vector<double>(len);

		if (DCT[u].empty()) {
			return false; // OutOfMemoryError
		}

		for (int v = 0; v < len; v++) {
			double sum = 0.0;

			for (int i = 0; i < size; i++) {
				for (int j = 0; j < len; j++) {
					sum += data[i][j]
							* std::cos((2 * i + 1) / (2.0 * size) * u * M_PI)
							* std::cos((2 * j + 1) / (2.0 * len) * v * M_PI);
				}
			}

			sum *= coeff[u] * coeff[v] / 4.0;

			DCT[u][v] = sum;
		}
	}

	data = DCT;

	return true;
}

static jobjectArray ImagePHash::nativeApplyDCT(JNIEnv* env, jclass clazz, jobjectArray in) {
	jsize size = env->GetArrayLength(in);

	if (size == 0) {
		return NULL; // Illegal Argument //
	}

	pix_map_t buf = pix_map_t(size);

	if (buf.empty()) {
		return NULL; // OutOfMemoryError
	}

	for (jsize i = 0; i < size; i++) {
		jdoubleArray inRow = reinterpret_cast<jdoubleArray>(env->GetObjectArrayElement(in, i));

		if (inRow == NULL) {
			return NULL; // Illegal Argument //
		}

		jsize len = env->GetArrayLength(inRow);

		buf[i] = std::vector<double>(len);

		if (buf[i].empty()) {
			return NULL; // OutOfMemoryError
		}

		env->GetDoubleArrayRegion(inRow, 0, len, &buf[i][0]);

		env->DeleteLocalRef(inRow);
	}

	if (!applyDCT(buf)) {
		return NULL; // OutOfMemoryError
	}

	jclass doubleArrayClass = env->FindClass("[D");

	if (doubleArrayClass == NULL) {
		return NULL; // Exception
	}

	jobjectArray out = env->NewObjectArray(buf.size(), doubleArrayClass, NULL);

	if (out == NULL) {
		return NULL; // OutOfMemoryError
	}

	for (jsize i = 0; i < buf.size(); i++) {
		jsize len = buf[i].size();

		jdoubleArray outRow = env->NewDoubleArray(len);

		if (outRow == NULL) {
			return NULL; // OutOfMemoryError
		}

		env->SetDoubleArrayRegion(outRow, 0, len, &buf[i][0]);

		env->SetObjectArrayElement(out, i, outRow);

		env->DeleteLocalRef(outRow);
	}

	return out;
} // ImagePHash::nativeApplyDCT

static const JNINativeMethod NATIVE_METHOD[] = {
		{ "nativeApplyDCT", "([[D)[[D", (void*) ImagePHash::nativeApplyDCT }
	};

jint ImagePHash::registerNative(JNIEnv* env) {
	if (!env) {
		return JNI_ERR;
	}

	jclass ImagePHash = env->FindClass(CLASS_NAME);

	if (ImagePHash == NULL) {
		return JNI_ERR; // Exception
	}

	jint result = env->RegisterNatives(
			ImagePHash,
			NATIVE_METHOD,
			sizeOfArray(NATIVE_METHOD)
		);

	env->DeleteLocalRef(ImagePHash);

	return result;
} // ImagePHash::registerNative

