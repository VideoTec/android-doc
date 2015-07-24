@echo off

set antdir="D:\ant_feinnovideo\build\apache-ant-1.9.4\bin\ant.bat"
set buildfile="D:\ant_feinnovideo\sea-monster-mvclip\build.xml"

set bdir="D:\ant_feinnovideo\build\."
set mdir="D:\ant_feinnovideo\sea-monster-mvclip"
set cdir="D:\ant_feinnovideo\sea-monster-core"
set vdir="D:\ant_feinnovideo\sea-monster-video"

echo  ----------------------------
echo         请选择打包方式
echo  ----------------------------
echo   1. 测试包-SVN重新下载代码
echo   2. 正式包-SVN重新下载代码
echo   3. 渠道包-SVN重新下载代码
echo.
echo   4. 测试包-本地代码
echo   5. 正式包-本地代码
echo   6. 渠道包-本地代码
echo  ----------------------------
set /p key=请输入 数字 并按 回车键 确认:

if %key% == 1 (
	echo.
	goto package-debug-svn
) else if %key% == 2 (
	echo.
	goto package-release-svn
) else if %key% == 3 (
	echo.
	goto package-channels-svn
) else if %key% == 4 (
	echo.
	goto package-debug-local
) else if %key% == 5 (
	echo.
	goto package-release-local
) else if %key% == 6 (
	echo.
	goto package-channels-local
) else (
	echo.
	echo 无此选项 即将退出
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
	
)

@REM ---------------------------SVN下载，打测试包----------------------------------
:package-debug-svn
call svncheck.bat

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% debug-version-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out

goto over

@REM ----------------------------SVN下载，打正式包---------------------------------
:package-release-svn
call svncheck.bat

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% release-version-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out

goto over

@REM -----------------------------SVN下载，打渠道包--------------------------------
:package-channels-svn
call svncheck.bat

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% channels-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out

goto over

@REM -----------------------------本地代码，打测试包--------------------------------
:package-debug-local

if exist %bdir% (
	echo 检验打包工具通过	
) else (
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %mdir% (
	echo 检验sea-monster-mvclip代码通过
) else ( 
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %cdir% (
	echo 检验sea-monster-core代码通过
) else (
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %vdir% (
	echo 检验sea-monster-video代码通过
) else (
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% debug-version-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out

goto over

@REM ------------------------------本地代码，打正式包-------------------------------
:package-release-local

if exist %bdir% (
	echo 检验打包工具通过	
) else (
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %mdir% (
	echo 检验sea-monster-mvclip代码通过
) else ( 
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %cdir% (
	echo 检验sea-monster-core代码通过
) else (
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %vdir% (
	echo 检验sea-monster-video代码通过
) else (
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% release-version-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out


goto over

@REM ------------------------------本地代码，打渠道包-------------------------------
:package-channels-local

if exist %bdir% (
	echo 检验打包工具通过	
) else (
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %mdir% (
	echo 检验sea-monster-mvclip代码通过
) else ( 
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %cdir% (
	echo 检验sea-monster-core代码通过
) else (
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %vdir% (
	echo 检验sea-monster-video代码通过
) else (
	echo 即将退出，缺少工具或代码，请从SVN下载
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% channels-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out

goto over

@REM -------------------------------------------------------------
:over
pause
