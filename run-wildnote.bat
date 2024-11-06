@echo off
for /f "delims=" %%i in ('dir .\wildnote\target\*.jar /b /o:-n') do (
    call :runjar %%i
    pause
    goto :EOF
)

:runjar
set r=java -jar ".\wildnote\target\%1" --spring.config.location="%cd%\..\wildnote-application.properties"
echo %r%
%r%


