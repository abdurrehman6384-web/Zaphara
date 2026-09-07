cmake_minimum_required(VERSION 3.22.1)
project(rgs-live2d)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Point this at the Live2D Cubism Native SDK copied into the host Android
# project. The Core library is supplied by Live2D under its SDK license.
set(CUBISM_SDK_ROOT "${CMAKE_CURRENT_LIST_DIR}/../../../../../../../../host-live2d-sdk"
    CACHE PATH "Live2D Cubism Native SDK root")

add_library(rgs-live2d SHARED RGSNativeRenderer.cpp)
target_include_directories(rgs-live2d PRIVATE
    "${CUBISM_SDK_ROOT}/Core/include"
    "${CUBISM_SDK_ROOT}/Framework/src"
)

find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv2)

target_link_libraries(rgs-live2d
    ${log-lib}
    ${android-lib}
    ${gles-lib}
)

# The host app should add the Cubism Framework and Core targets here. The
# exact Framework subdirectory is intentionally configurable because the
# SDK package layout differs between SDK releases.
if (EXISTS "${CUBISM_SDK_ROOT}/Framework/CMakeLists.txt")
    add_subdirectory("${CUBISM_SDK_ROOT}/Framework"
        "${CMAKE_CURRENT_BINARY_DIR}/cubism-framework")
    target_link_libraries(rgs-live2d Framework Live2DCubismCore)
endif()