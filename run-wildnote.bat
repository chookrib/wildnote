@echo off
for /f "delims=" %%i in ('dir .\wildnote-svc\target\*.jar /b /o:-n') do (
    call :runjar %%i
    pause
    goto :EOF
)

:runjar
set runjarcmd=java -jar ".\wildnote-svc\target\%1" --spring.config.location="%cd%\..\wildnote.properties"
echo %runjarcmd%
%runjarcmd%


