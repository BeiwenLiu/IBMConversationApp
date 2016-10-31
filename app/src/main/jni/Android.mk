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

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := TrulyHandsfreeJNI
LOCAL_CFLAGS := -Wall -Werror
LOCAL_LDLIBS := -llog

LOCAL_SRC_FILES := common.c buildList.c buildGrammar.c buildIncremental.c\
  recogList.c recogGrammar.c phrasespot.c recogPipe.c recogSeq.c\
  speakerVerification.c speakerIdentification.c udtsid.c audio.c recogEnroll.c

LOCAL_STATIC_LIBRARIES := TrulyHandsfreeSDK

include $(BUILD_SHARED_LIBRARY)

$(call import-add-path,.)
$(call import-module,TrulyHandsfreeSDK)
