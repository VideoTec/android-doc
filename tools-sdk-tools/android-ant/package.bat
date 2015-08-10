@echo off

set antdir="D:\ant_feinnovideo\build\apache-ant-1.9.4\bin\ant.bat"
set buildfile="D:\ant_feinnovideo\sea-monster-mvclip\build.xml"

set bdir="D:\ant_feinnovideo\build\."
set mdir="D:\ant_feinnovideo\sea-monster-mvclip"
set cdir="D:\ant_feinnovideo\sea-monster-core"
set vdir="D:\ant_feinnovideo\sea-monster-video"

echo  ----------------------------
echo         ��ѡ������ʽ
echo  ----------------------------
echo   1. ���԰�-SVN�������ش���
echo   2. ��ʽ��-SVN�������ش���
echo   3. ������-SVN�������ش���
echo.
echo   4. ���԰�-���ش���
echo   5. ��ʽ��-���ش���
echo   6. ������-���ش���
echo  ----------------------------
set /p key=������ ���� ���� �س��� ȷ��:

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
	echo �޴�ѡ�� �����˳�
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
	
)

@REM ---------------------------SVN���أ�����԰�----------------------------------
:package-debug-svn
call svncheck.bat

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% debug-version-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out

goto over

@REM ----------------------------SVN���أ�����ʽ��---------------------------------
:package-release-svn
call svncheck.bat

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% release-version-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out

goto over

@REM -----------------------------SVN���أ���������--------------------------------
:package-channels-svn
call svncheck.bat

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% channels-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out

goto over

@REM -----------------------------���ش��룬����԰�--------------------------------
:package-debug-local

if exist %bdir% (
	echo ����������ͨ��	
) else (
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %mdir% (
	echo ����sea-monster-mvclip����ͨ��
) else ( 
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %cdir% (
	echo ����sea-monster-core����ͨ��
) else (
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %vdir% (
	echo ����sea-monster-video����ͨ��
) else (
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% debug-version-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out

goto over

@REM ------------------------------���ش��룬����ʽ��-------------------------------
:package-release-local

if exist %bdir% (
	echo ����������ͨ��	
) else (
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %mdir% (
	echo ����sea-monster-mvclip����ͨ��
) else ( 
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %cdir% (
	echo ����sea-monster-core����ͨ��
) else (
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %vdir% (
	echo ����sea-monster-video����ͨ��
) else (
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)

cd\
d:
cd ant_feinnovideo\sea-monster-mvclip
call %antdir% -f %buildfile% release-version-package
echo out=D:\ant_feinnovideo\sea-monster-mvclip\out


goto over

@REM ------------------------------���ش��룬��������-------------------------------
:package-channels-local

if exist %bdir% (
	echo ����������ͨ��	
) else (
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %mdir% (
	echo ����sea-monster-mvclip����ͨ��
) else ( 
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %cdir% (
	echo ����sea-monster-core����ͨ��
) else (
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
	@ping 127.0.0.1 -n 5 -w 1000 > nul
	exit
)


if exist %vdir% (
	echo ����sea-monster-video����ͨ��
) else (
	echo �����˳���ȱ�ٹ��߻���룬���SVN����
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
