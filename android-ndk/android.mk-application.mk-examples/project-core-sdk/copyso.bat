@echo off
cd %~dp0
:BUILD
call ndk-build V=0 NDK_DEBUG=0
echo f|xcopy D:\CoreSDK\trunk\libs\armeabi-v7a\libconvert.so D:\03Android\sea-monster-video\libs\armeabi-v7a\libconvert.so /Y
echo f|xcopy D:\CoreSDK\trunk\libs\armeabi-v7a\libconvert.so D:\03Android\animation-everfrost-engine\libs\armeabi-v7a\libconvert.so /Y
echo f|xcopy D:\CoreSDK\trunk\libs\armeabi-v7a\libconvert.so D:\CoreSDK\trunk\android-demo\VideoRecordDemo\libs\armeabi-v7a\libconvert.so /Y
:WAIT
set /p KEY=
cls
if "%KEY%"=="C" (
  call ndk-build clean
  set KEY=
  goto WAIT

) else (
  goto BUILD
)