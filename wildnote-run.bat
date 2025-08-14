@echo off

rem 批处理传入的每个参数都需要用""包裹

rem if "%1" == "" (
rem     goto :usage
rem ) else (
rem    set arg_wildnote_root_path=--wildnote.root-path=%1
rem )

rem if "%2" == "" (
rem    goto :usage
rem ) else (
rem    set arg_wildnote_username=--wildnote.username=%2
rem )

rem if "%3" == "" (
rem    goto :usage
rem ) else (
rem    set arg_wildnote_password=--wildnote.password=%3
rem )

rem if not "%4" == "" (
rem    set arg_server_port=--server.port=%4
rem )

cd /d "%~dp0"

set params=%*
rem set params=%1 %2 %3 %4 %5 %6 %7 %8 %9

echo %PATH%
git --version

git checkout main
git pull

cd wildnote-web
call npm install
call npm run build
cd ..

rem set mvn_path=..\wildnote-tools\apache-maven-3.9.9\bin\mvn.cmd
rem call %mvn_path% package -Dmaven.test.skip=true -f .\wildnote-server

cd wildnote-server
rem call mvnw.cmd package -DskipTests -f .\pom.xml
call mvnw.cmd package -DskipTests

if not exist ".\target\*.jar" (
    echo Wildnote jar file not found, please build first.
) else (
    for /f "delims=" %%i in ('dir .\target\*.jar /b /o:-n') do (
        rem call :runjar %%i "%arg_wildnote_root_path%" "%arg_wildnote_username%" "%arg_wildnote_password%" "%arg_server_port%"
        call :runjar %%i %params%
        goto :EOF
    )
)
goto :EOF

:runjar
rem set run_wildnote_cmd=java -jar ".\target\%1" --spring.config.location="%cd%\..\wildnote.properties"
rem set run_wildnote_cmd=java -jar .\target\%1 --spring.web.resources.static-locations=file:..\wildnote-web\dist %~2 %~3 %~4 %~5
rem set run_wildnote_cmd=java -jar .\target\%1 --spring.web.resources.static-locations=file:..\wildnote-web\dist %*
set run_wildnote_cmd=java -jar .\target\%1 --spring.web.resources.static-locations=file:..\wildnote-web\dist %2 %3 %4 %5 %6 %7 %8 %9
echo %run_wildnote_cmd%
%run_wildnote_cmd%
goto :EOF

:usage
echo Usage: %0 "[spring-boot-command-line-option-arguments-1]" "[spring-boot-command-line-option-arguments-2]" ...
goto :EOF

