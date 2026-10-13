@echo off
setlocal

for %%I in ("%~dp0..\..\..\..\..\..\..") do set "PROJECT_ROOT=%%~fI"
if not exist "%PROJECT_ROOT%\pom.xml" (
    echo ERROR: Could not locate pom.xml from "%~dp0".
    exit /b 2
)

pushd "%PROJECT_ROOT%" || exit /b 2

set "SUITE=%~1"
if not defined SUITE set "SUITE=src/test/resources/testng.xml"
if not exist "%SUITE%" if exist "src\test\resources\%SUITE%" set "SUITE=src/test/resources/%SUITE%"
if not exist "%SUITE%" (
    echo ERROR: TestNG suite file not found: "%SUITE%"
    popd
    exit /b 2
)

set "GROUPS=%~2"
if defined GROUPS (
    call mvn clean test "-Dsurefire.suiteXmlFiles=%SUITE%" "-Dgroups=%GROUPS%"
) else (
    call mvn clean test "-Dsurefire.suiteXmlFiles=%SUITE%"
)
set "RESULT=%ERRORLEVEL%"

popd
exit /b %RESULT%
