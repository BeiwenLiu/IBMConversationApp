# Android.mk for prebuilt Sensory TrulyHandsfree SDK library
# header: ./include/trulyhandsfree.h
# libs:   ./lib/libthf_<target_arch>.a
#
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := TrulyHandsfreeSDK
LOCAL_SRC_FILES := lib/libthf_$(TARGET_ARCH_ABI).a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)
