@echo off
setlocal enabledelayedexpansion

REM InfernoVisuals Launcher .exe builder (Windows)
REM Requirements: JDK 21+ with jpackage, internet for gson download.

set ROOT=%~dp0..\
for %%I in ("%ROOT%") do set ROOT=%%~fI

set SRC=%ROOT%src\main\java
set BUILD=%ROOT%build
set CLASSES=%BUILD%\classes
set JAR=%BUILD%\infernovisuals-launcher.jar
set DIST=%ROOT%dist
set LIB=%ROOT%lib
set GSON=%LIB%\gson-2.11.0.jar

if not exist "%LIB%" mkdir "%LIB%"
if not exist "%BUILD%" mkdir "%BUILD%"
if not exist "%CLASSES%" mkdir "%CLASSES%"
if not exist "%DIST%" mkdir "%DIST%"

if not exist "%GSON%" (
  echo Downloading gson...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$urls = @('https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar','https://repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar'); foreach ($u in $urls) { try { Invoke-WebRequest -Uri $u -OutFile '%GSON%'; break } catch { } }; if (-not (Test-Path '%GSON%')) { throw 'Failed to download gson' }"
)

echo Compiling launcher sources...
for /r "%SRC%" %%f in (*.java) do echo %%f>> "%BUILD%\sources.txt"

javac -encoding UTF-8 -cp "%GSON%" -d "%CLASSES%" @"%BUILD%\sources.txt"
if errorlevel 1 goto :fail

echo Building launcher jar...
jar --create --file "%JAR%" --main-class com.infernovisuals.launcher.LauncherMain -C "%CLASSES%" .
if errorlevel 1 goto :fail

echo Packaging .exe with jpackage...
jpackage --name "InfernoVisualsLauncher" --input "%BUILD%" --main-jar "infernovisuals-launcher.jar" --main-class com.infernovisuals.launcher.LauncherMain --type exe --win-console --dest "%DIST%"
if errorlevel 1 goto :fail

echo Done. EXE is in: %DIST%
exit /b 0

:fail
echo Build failed.
exit /b 1
