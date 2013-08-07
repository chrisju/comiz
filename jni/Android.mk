# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH:= $(call my-dir)

## lua
include $(CLEAR_VARS)

LOCAL_MODULE := lua
LOCAL_ARM_MODE   := arm
LOCAL_CFLAGS    := -Werror -DLUA_USE_LINUX
LOCAL_LDLIBS := -lm
LOCAL_C_INCLUDES := $(LOCAL_PATH)/lua
LOCAL_SRC_FILES :=  \
lua/lapi.c \
lua/lauxlib.c \
lua/lbaselib.c \
lua/lcode.c \
lua/ldblib.c \
lua/ldebug.c \
lua/ldo.c \
lua/ldump.c \
lua/lfunc.c \
lua/lgc.c \
lua/linit.c \
lua/liolib.c \
lua/llex.c \
lua/lmathlib.c \
lua/lmem.c \
lua/loadlib.c \
lua/lobject.c \
lua/lopcodes.c \
lua/loslib.c \
lua/lparser.c \
lua/lstate.c \
lua/lstring.c \
lua/lstrlib.c \
lua/ltable.c \
lua/ltablib.c \
lua/ltm.c \
lua/lundump.c \
lua/lvm.c \
lua/lzio.c \
lua/print.c \

include $(BUILD_STATIC_LIBRARY)


## main
include $(CLEAR_VARS)

LOCAL_MODULE    := luandroid
LOCAL_ARM_MODE   := arm
LOCAL_CFLAGS    := -Werror -D PACKAGE_NAME=gmail_mchrisju_comiz
LOCAL_STATIC_LIBRARIES := lua
LOCAL_LDLIBS := -llog
#LOCAL_LDLIBS    := -llog -lGLESv2
LOCAL_C_INCLUDES := $(LOCAL_PATH)/lua/
LOCAL_SRC_FILES :=  \
	jni.c \
	luac.c \
	util.c \

include $(BUILD_SHARED_LIBRARY)
