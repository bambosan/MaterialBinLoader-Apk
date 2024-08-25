LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := mc
LOCAL_SRC_FILES := mc-init.cpp
include $(BUILD_SHARED_LIBRARY)
