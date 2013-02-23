#
# Copyright (C) 2013 strider
#
# Simple Recognizer
# Android.mk libImagePHash.so
# By © strider 2013.
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ImagePHash-version-0.9.3
LOCAL_MODULE_FILENAME := libImagePHash
LOCAL_SRC_FILES := Main.cpp ImagePHash.cpp
#LIST_SRC_FILES := $(wildcard $(LOCAL_PATH)/*.cpp)
#LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)
#LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)

