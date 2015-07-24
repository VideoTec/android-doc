LOCAL_PATH := $(call my-dir)

# -- define prebuilt avutil module --

include $(CLEAR_VARS)

LOCAL_MODULE := avutil
LOCAL_SRC_FILES := lib/libavutil.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt swresample module --

include $(CLEAR_VARS)

LOCAL_MODULE := swresample
LOCAL_SRC_FILES := lib/libswresample.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt swscale module --

include $(CLEAR_VARS)

LOCAL_MODULE := swscale
LOCAL_SRC_FILES := lib/libswscale.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt avcodec module --

include $(CLEAR_VARS)

LOCAL_MODULE := avcodec
LOCAL_SRC_FILES := lib/libavcodec.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt avformat module --

include $(CLEAR_VARS)

LOCAL_MODULE := avformat
LOCAL_SRC_FILES := lib/libavformat.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt avfilter module --

include $(CLEAR_VARS)

LOCAL_MODULE := avfilter
LOCAL_SRC_FILES := lib/libavfilter.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt opencore-amr module --

include $(CLEAR_VARS)

LOCAL_MODULE := opencore-amrnb
LOCAL_SRC_FILES := lib/libopencore-amrnb.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt x264 module --

include $(CLEAR_VARS)

LOCAL_MODULE := x264
LOCAL_SRC_FILES := lib/libx264.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)

include $(PREBUILT_STATIC_LIBRARY)

# -- build --

include $(CLEAR_VARS)

LOCAL_CPP_EXTENSION := .cxx .cpp .cc
LOCAL_MODULE := opensles
LOCAL_SRC_FILES := jni_entry.c \
                   libopensl.cpp \
                   fv_audio_decoder.c \
                   fv_audio_mix.c \
                   fv_audio_player.c \
                   fv_opensl_player.c \
                   ffmpeg_util.c \
                   fv_audio_cut.c
LOCAL_STATIC_LIBRARIES := avfilter avformat swresample swscale avcodec avutil opencore-amrnb x264
#LOCAL_C_INCLUDES := $(LOCAL_PATH)/include/
LOCAL_CFLAGS := -std=c99 -D__STDC_CONSTANT_MACROS
LOCAL_CPPFLAGS := -std=c++11
LOCAL_CPP_FEATURES := rtti exceptions
LOCAL_LDLIBS := -llog -ljnigraphics -landroid -lz -lGLESv2 -lEGL -lOpenSLES

include $(BUILD_SHARED_LIBRARY)

