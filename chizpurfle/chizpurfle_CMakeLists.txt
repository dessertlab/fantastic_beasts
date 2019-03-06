# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)

add_library( # Sets the name of the library.
             chizpurfle-native

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

find_library( dl-lib
              dl)

add_library( frida-core
             SHARED
             IMPORTED )

set_target_properties(frida-core
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_CURRENT_SOURCE_DIR}/libs/frida-core/libfrida-core.a )

include_directories( ${CMAKE_CURRENT_SOURCE_DIR}/libs/frida-core/ )

target_link_libraries( chizpurfle-native
                       frida-core
                       ${dl-lib}
                       ${log-lib} )

add_library( stalker-server
             MODULE
             src/main/cpp/server/stalker-server.cpp
             src/main/cpp/server/fakeeventsink.c
             src/main/cpp/jsmn.c)

add_library( frida-gum
             SHARED
             IMPORTED )

set_target_properties(frida-gum
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_CURRENT_SOURCE_DIR}/libs/frida-gum/libfrida-gum.a )

include_directories( ${CMAKE_CURRENT_SOURCE_DIR}/libs/frida-gum/ )

target_link_libraries( stalker-server
                       frida-gum
                       ${dl-lib}
                       ${log-lib})
