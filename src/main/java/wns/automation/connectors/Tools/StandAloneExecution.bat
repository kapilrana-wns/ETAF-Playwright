@echo off
setlocal

set "SUITE=%~1"
if not defined SUITE set "SUITE=testng.xml"
set "GROUPS=%~2"
if not defined GROUPS set "GROUPS=Positive"

call "%~dp0mavenTestRun.bat" "%SUITE%" "%GROUPS%"
exit /b %ERRORLEVEL%