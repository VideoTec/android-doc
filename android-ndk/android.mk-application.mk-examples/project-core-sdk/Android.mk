LOCAL_PATH := $(call my-dir)

# -- libjpeg --

include $(CLEAR_VARS)

LOCAL_MODULE := jpeg
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/libjpeg/lib/libjpeg.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/libjpeg/include

include $(PREBUILT_STATIC_LIBRARY)

# -- libpng --

include $(CLEAR_VARS)

LOCAL_MODULE := png
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/libpng/lib/libpng16.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/libpng/include/libpng16

include $(PREBUILT_STATIC_LIBRARY)

# -- freetype --

#include $(CLEAR_VARS)

LOCAL_MODULE := freetype
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/freetype/lib/libfreetype.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/freetype/include/freetype2

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt avutil module --

include $(CLEAR_VARS)

LOCAL_MODULE := avutil
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/ffmpeg/lib/libavutil.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/ffmpeg/include/libavutil

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt swresample module --

include $(CLEAR_VARS)

LOCAL_MODULE := swresample
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/ffmpeg/lib/libswresample.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/ffmpeg/include/libswresample

include $(PREBUILT_STATIC_LIBRARY)

# -- swscale --

include $(CLEAR_VARS)

LOCAL_MODULE := swscale
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/ffmpeg/lib/libswscale.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/ffmpeg/include/libswscale

include $(PREBUILT_STATIC_LIBRARY)

# -- avcodec --

include $(CLEAR_VARS)

LOCAL_MODULE := avcodec 
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/ffmpeg/lib/libavcodec.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/ffmpeg/include/libavcodec

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt avformat module --

include $(CLEAR_VARS)

LOCAL_MODULE := avformat
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/ffmpeg/lib/libavformat.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/ffmpeg/include/libavformat

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt avfilter module --

include $(CLEAR_VARS)

LOCAL_MODULE := avfilter
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/ffmpeg/lib/libavfilter.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/ffmpeg/include/libavfilter

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt opencore-amr module --

include $(CLEAR_VARS)

LOCAL_MODULE := opencore-amrnb
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/libopencore-amr/lib/libopencore-amrnb.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/libopencore-amr/include/opencore-amrnb

include $(PREBUILT_STATIC_LIBRARY)

# -- define prebuilt x264 module --

include $(CLEAR_VARS)

LOCAL_MODULE := x264
LOCAL_SRC_FILES := ../$(TARGET_ARCH_ABI)/libx264/lib/libx264.a
LOCAL_EXPORT_C_INCLUDES := $(TARGET_ARCH_ABI)/libx264/include

include $(PREBUILT_STATIC_LIBRARY)

# -- build --

include $(CLEAR_VARS)

LOCAL_MODULE := convert
FF_CODEC_FILES := ff_codec/msource_file.c \
                  ff_codec/mtarget_file.c \
                  ff_codec/crop_video.c \
                  ff_codec/image_scaler.c
RECORD_SRC_FILES := record/record_video_filter.cpp \
                    record/record_audio_resample.cpp \
                    record/record_manager.cpp \
                    record/record_recorder.cpp \
                    record/jni_media_recorder.cpp 
LOCAL_SRC_FILES := jniconvert.cpp \
                   Encoder/encoder.c \
                   Coordinator/coordinator.cpp \
                   Coordinator/bitmap_renderer.cpp \
                   Decoder/font_codec.cpp \
                   Decoder/image_codec.cpp \
                   Decoder/video_codec.cpp \
                   Decoder/decoded_sample_buffer.cpp \
                   Decoder/decoded_picture_buffer.cpp \
                   Decoder/time_r.cpp \
                   Util/scene.c \
                   Util/animation.c \
                   Util/filter.c \
                   Util/engine.c \
                   Util/action.c \
                   Util/bezier_path.c \
                   Util/matrix.c \
                   Util/vector.c \
                   fv_audio_player.c \
                   opensl_player.c \
                   fv_audio_decoder.c \
                   fv_audio_mix.c \
                   fv_audio_cut.c $(RECORD_SRC_FILES) $(FF_CODEC_FILES)

LOCAL_STATIC_LIBRARIES := avfilter avformat swresample swscale avcodec avutil opencore-amrnb x264 freetype png jpeg

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../$(TARGET_ARCH_ABI)/ffmpeg/include

LOCAL_CFLAGS := -std=c11 -D__STDC_CONSTANT_MACROS -DF_RECORD_LOG_LEVEL=F_RECORD_LOG_LEVEL_INFO -DANDROID_APP

LOCAL_CPPFLAGS := -std=gnu++11 

LOCAL_CPP_FEATURES := rtti exceptions

LOCAL_LDLIBS := -llog -ljnigraphics -landroid -lz -lGLESv2 -lEGL -lOpenSLES

include $(BUILD_SHARED_LIBRARY)

