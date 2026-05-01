# 13장: 앱 모듈의 네이티브 빌드 진입점.
# 실제 JNI 코드는 native/jni/에 있고 여기서 참조한다.

cmake_minimum_required(VERSION 3.22)
project(app_native)

# 저장소 루트의 native/jni를 빌드 트리에 포함
add_subdirectory(
    "${CMAKE_CURRENT_SOURCE_DIR}/../../../../native/jni"
    jni_build
)
