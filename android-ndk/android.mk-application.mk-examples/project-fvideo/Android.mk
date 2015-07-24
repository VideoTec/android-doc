LOCAL_PATH := $(call my-dir)

include $(LOCAL_PATH)/lib/lib.mk

include $(CLEAR_VARS)

LOCAL_MODULE    := fvideo
LOCAL_CFLAGS := -std=c++11
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_LDLIBS    := -ljnigraphics -lz -llog
LOCAL_STATIC_LIBRARIES := format codec resample swscale util x264 vo-aacenc amrnb amrwb

LOCAL_SRC_FILES :=   MediaSource.cpp \
                     MediaSourceNativeInterface.cpp \
                     MediaTarget.cpp \
                     MediaTargetNativeInterface.cpp \
                     VideoCache.cpp \
                     VideoCacheNativeInterface.cpp \
                     JNICommon.cpp

include $(BUILD_SHARED_LIBRARY)
