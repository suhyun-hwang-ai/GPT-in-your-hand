# 13장: JNI 바인딩 빌드 설정.
# 이 CMakeLists는 app/src/main/cpp/CMakeLists.txt에서 add_subdirectory로 포함된다.

cmake_minimum_required(VERSION 3.22)

# llama.cpp 헤더 위치를 LLAMA_CPP_DIR로 받는다.
# 기본값: 사용자 홈의 llama.cpp
if(NOT DEFINED LLAMA_CPP_DIR)
    set(LLAMA_CPP_DIR "$ENV{LLAMA_CPP_DIR}")
endif()
if(NOT LLAMA_CPP_DIR)
    set(LLAMA_CPP_DIR "$ENV{HOME}/llama.cpp")
endif()

if(NOT EXISTS "${LLAMA_CPP_DIR}/include/llama.h")
    message(FATAL_ERROR
        "llama.cpp 헤더를 찾을 수 없습니다: ${LLAMA_CPP_DIR}/include/llama.h\n"
        "LLAMA_CPP_DIR을 올바르게 설정해 주세요.")
endif()

add_library(llama_jni SHARED
    llama_jni.cpp
)

target_include_directories(llama_jni PRIVATE
    ${LLAMA_CPP_DIR}/include
    ${LLAMA_CPP_DIR}/ggml/include
)

# 사전 빌드된 .so를 import. native/prebuilt/${ANDROID_ABI}/ 경로 참조.
set(PREBUILT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../prebuilt/${ANDROID_ABI}")

foreach(libname llama ggml ggml-base ggml-cpu)
    add_library(${libname} SHARED IMPORTED)
    set_target_properties(${libname} PROPERTIES
        IMPORTED_LOCATION "${PREBUILT_DIR}/lib${libname}.so")
endforeach()

target_link_libraries(llama_jni
    llama
    ggml
    ggml-base
    ggml-cpu
    log
    android
)
