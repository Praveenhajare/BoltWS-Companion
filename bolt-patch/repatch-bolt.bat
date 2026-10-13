@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

REM ============================================================
REM  BoltWS - bolt.jar Re-Patch Script
REM  Run this AFTER `bolt upgrade` to re-apply the
REM  bolt.parameter-metadata fix to bolt.jar.
REM
REM  What it does: replaces RunCommand*.class inside bolt.jar
REM  with the patched versions (that send parameter metadata
REM  after the DEX), so `bolt run` shows correct parameter names.
REM ============================================================

set "BOLT_JAR=%LOCALAPPDATA%\Bolt\bin\bolt.jar"
set "PATCH_DIR=%~dp0"
set "CLASSES_DIR=%PATCH_DIR%classes"

echo.
echo === BoltWS bolt.jar Re-Patch ===
echo.

if not exist "%BOLT_JAR%" (
    echo [ERROR] bolt.jar not found at: %BOLT_JAR%
    pause
    exit /b 1
)

if not exist "%CLASSES_DIR%\RunCommand.class" (
    echo [ERROR] patched classes not found in: %CLASSES_DIR%
    pause
    exit /b 1
)

REM --- Check no bolt.jar process is running (file lock) ---
echo [1/4] Checking for running bolt processes...
powershell -NoProfile -Command "$p = Get-CimInstance Win32_Process -Filter \"Name like '%java%'\" | Where-Object { $_.CommandLine -like '*bolt.jar*' }; if ($p) { Write-Host '[ERROR] bolt is still running (PID(s):'; $p | ForEach-Object { Write-Host ('  ' + $_.ProcessId) }; Write-Host 'Close the bolt run session first, then re-run this script.'; exit 1 } else { Write-Host 'OK - bolt.jar is not locked.' }"
if errorlevel 1 (
    pause
    exit /b 1
)

REM --- Backup current bolt.jar ---
echo [2/4] Backing up current bolt.jar...
for /f "delims=" %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set "TS=%%i"
copy /y "%BOLT_JAR%" "%BOLT_JAR%.backup-%TS%" >nul
echo     Backup saved: bolt.jar.backup-%TS%

REM --- Replace RunCommand*.class inside bolt.jar ---
echo [3/4] Injecting patched RunCommand classes into bolt.jar...
powershell -NoProfile -ExecutionPolicy Bypass -File "%PATCH_DIR%inject.ps1" -BoltJar "%BOLT_JAR%" -ClassesDir "%CLASSES_DIR%"
if errorlevel 1 (
    echo [ERROR] Injection failed. Your backup is safe.
    pause
    exit /b 1
)

REM --- Verify ---
echo [4/4] Verifying patch...
powershell -NoProfile -Command "$ErrorActionPreference='Stop'; Add-Type -AssemblyName System.IO.Compression.FileSystem; $z=[System.IO.Compression.ZipFile]::OpenRead('%BOLT_JAR%'); $hit=$z.Entries | Where-Object { $_.FullName -like '*RunCommand*buildParameterMetadataEnvelope*' }; $z.Dispose(); if($hit){ Write-Host 'SUCCESS - patch is active. Now run: bolt run' } else { Write-Host 'FAILED - patch not found in jar.'; exit 1 }"
if errorlevel 1 (
    pause
    exit /b 1
)

echo.
echo === DONE. Parameter metadata is now active. ===
echo.
pause
