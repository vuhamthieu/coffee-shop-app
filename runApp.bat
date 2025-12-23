@echo off
setlocal ENABLEDELAYEDEXPANSION

set FX_LIB=%~dp0lib
if not exist "%FX_LIB%" (
    echo [ERROR] Khong tim thay thu muc lib chua JavaFX SDK.
    exit /b 1
)

set MAIN_CLASS=App
if not "%~1"=="" (
    set MAIN_CLASS=%~1
)

set MODULES=javafx.controls,javafx.fxml

echo === Compile %MAIN_CLASS%.java ===
javac --module-path "%FX_LIB%" --add-modules %MODULES% %MAIN_CLASS%.java
if errorlevel 1 (
    echo [ERROR] Compile failed.
    exit /b 1
)

echo === Run %MAIN_CLASS% ===
java --module-path "%FX_LIB%" --add-modules %MODULES% %MAIN_CLASS%

