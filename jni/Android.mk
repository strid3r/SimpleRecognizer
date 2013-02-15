#
# Copyright (C) 2013 strider
#
# Simple Recognizer
# Android.mk SimpleRecognizer
# By © strider 2013.
#

JNI_PATH := $(call my-dir)

LOCAL_PATH := $(JNI_PATH)

include $(call all-subdir-makefiles)

LOCAL_PATH := $(JNI_PATH)

#include $(CLEAR_VARS) // Pre Build Lib

#LOCAL_MODULE := libPreBuilt
#LOCAL_SRC_FILES := libPreBuilt.so

#include $(PREBUILT_SHARED_LIBRARY)

#include $(CLEAR_VARS) // Module

#LOCAL_MODULE := Module
#LOCAL_SRC_FILES := Module.cpp

#include $(BUILD_SHARED_LIBRARY)

