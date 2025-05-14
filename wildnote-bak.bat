@echo off

if "%1" == "" (
    goto :usage
) else (
    set src_dir=%1
)

if "%2" == "" (
    goto :usage
) else (
    set bak_dir=%2
)

if not exist "%bak_dir%" mkdir "%bak_dir%"

if "%3" == "" (
    set rar_exe_path=C:\Program Files\WinRAR\Rar.exe
) else (
    set rar_exe_path=%3
)

echo wscript.echo Year(Now) ^& "-" ^& Month(Now) ^& "-" ^& Day(Now) ^& "-" ^& Hour(Now) ^& "-" ^& Minute(Now) ^& "-" ^& Second(Now) > %tmp%\tmp.vbs
for /f "tokens=1,2,3,4,5,6* delims=-" %%i in ('cscript /nologo %tmp%\tmp.vbs') do (
    set "y=%%i"
    set "m=%%j"
    set "mm=%%j"
    set "d=%%k"
    set "dd=%%k"
    set "h=%%l"
    set "hh=%%l"
    set "f=%%m"
    set "ff=%%m"
    set "s=%%n"
    set "ss=%%n"
)
if %m% LSS 10 set "mm=0%m%"
if %d% LSS 10 set "dd=0%d%"
if %h% LSS 10 set "hh=0%h%"
if %f% LSS 10 set "ff=0%f%"
if %s% LSS 10 set "ss=0%s%"
set filename=%y%%mm%%dd%%hh%%ff%%ss%

echo "%rar_exe_path%" a -ep1 "%bak_dir%\%filename%.rar" "%src_dir%"
"%rar_exe_path%" a -ep1 "%bak_dir%\%filename%.rar" "%src_dir%
goto :EOF

:usage
echo Usage: %0 src_dir bak_dir [rar_exe_path]
goto :EOF
