rem @echo off

if "%1" == "" (
    goto :usage
) else (
    set arg_wildnote_path=--wildnote.path=%1
)

if "%2" == "" (
    goto :usage
) else (
    set arg_wildnote_username=--wildnote.username=%2
)

if "%3" == "" (
    goto :usage
) else (
    set arg_wildnote_password=--wildnote.password=%3
)

if not "%4" == "" (
    set arg_server_port=--server.port=%4
)

cd wildnote-web
npm install
npm run build
cd ..

set mvn_path=..\apache-maven-3.9.9-bin\apache-maven-3.9.9\bin\mvn.cmd
%mvn_path% package -Dmaven.test.skip=true -f .\wildnote-svc

if not exist ".\wildnote-svc\target\*.jar" (
    echo Wildnote jar file not found, please build first.
) else (
    for /f "delims=" %%i in ('dir .\wildnote-svc\target\*.jar /b /o:-n') do (
        call :runjar %%i "%arg_wildnote_path%" "%arg_wildnote_username%" "%arg_wildnote_password%" "%arg_server_port%"
    )
)
goto :EOF

:runjar
rem set run_wildnote_cmd=java -jar ".\wildnote-svc\target\%1" --spring.config.location="%cd%\..\wildnote.properties"
set run_wildnote_cmd=java -jar .\wildnote-svc\target\%1 --spring.web.resources.static-locations=file:.\wildnote-web\dist %~2 %~3 %~4 %~5
echo %run_wildnote_cmd%
%run_wildnote_cmd%
goto :EOF

:usage
echo Usage: %0 wildnote_path wildnote_username wildnote_password [server_port]
goto :EOF

