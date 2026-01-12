@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Allow overriding JavaFX SDK location via JAVA_FX_SDK environment variable.
if not "%JAVA_FX_SDK%"=="" (
    set FX_LIB=%JAVA_FX_SDK%\lib
) else (
    set FX_LIB=%~dp0lib
)

if not exist "%FX_LIB%" (
    echo [ERROR] Khong tim thay thu muc lib chua JavaFX SDK: %FX_LIB%
    echo If you have a full JavaFX SDK, set the JAVA_FX_SDK environment variable to its folder.
    exit /b 1
)

echo Using JavaFX lib: %FX_LIB%

set MAIN_CLASS=App
if not "%~1"=="" (
    set MAIN_CLASS=%~1
)

set MODULES=javafx.controls,javafx.fxml

echo === Compile %MAIN_CLASS%.java ===
javac -encoding UTF-8 --module-path "%FX_LIB%" --add-modules %MODULES% %MAIN_CLASS%.java
if errorlevel 1 (
    echo [ERROR] Compile failed.
    exit /b 1
)

echo === Run %MAIN_CLASS% ===
java --module-path "%FX_LIB%" --add-modules %MODULES% %MAIN_CLASS%

