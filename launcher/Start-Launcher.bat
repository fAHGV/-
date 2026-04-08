@echo off
setlocal

set ROOT=%~dp0
set BUILD_SCRIPT=%ROOT%scripts\build-launcher.bat
set EXE=%ROOT%dist\InfernoVisualsLauncher\InfernoVisualsLauncher.exe
set JAR=%ROOT%build\infernovisuals-launcher.jar

if exist "%EXE%" goto run_exe
if exist "%JAR%" goto run_jar

echo Launcher not built yet. Building first...
call "%BUILD_SCRIPT%"
if errorlevel 1 goto fail

if exist "%EXE%" goto run_exe
if exist "%JAR%" goto run_jar

echo Build finished but launcher executable was not found.
goto fail

:run_exe
echo Starting InfernoVisuals Launcher...
start "" "%EXE%"
exit /b 0

:run_jar
echo Starting InfernoVisuals Launcher (jar mode)...
start "" javaw -jar "%JAR%"
exit /b 0

:fail
echo Failed to start launcher.
pause
exit /b 1
