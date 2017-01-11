@echo off
REM #####################################################################
REM This script build the tpmcommands on windows platform
REM #####################################################################
setlocal enabledelayedexpansion

set me=%~n0
set pwd=%~dp0

set VsDevCmd="C:\Program Files (x86)\Microsoft Visual Studio 12.0\Common7\Tools\VsDevCmd.bat"
set "tpmcommands_home=%pwd%"

call:tpmcommands_build

:tpmcommands_build
  echo. Building tpmcommands....
  cd %tpmcommands_home%
  cd
  call %VsDevCmd%
  IF NOT %ERRORLEVEL% EQU 0 (
    echo. %me%: Visual Studio Dev Env could not be set
	call:ExitBatch
	REM EXIT /b %ERRORLEVEL%
  )
  call:tpm_signdata_build
  call:tpm_unbindaeskey_build
GOTO:EOF

:tpm_signdata_build
  echo. Building tpm_signdata....
  cd
  cl tpm_signdata_win.c /Fe:tpm_signdata
  IF NOT %ERRORLEVEL% EQU 0 (
    echo. %me%: tpm_signdata build failed
	call:ExitBatch
	REM EXIT /b %ERRORLEVEL%
  )
GOTO:EOF

:tpm_unbindaeskey_build
  echo. Building tpm_unbindaeskey....
  cd
  cl tpm_unbindaeskey_win.c /Fe:tpm_unbindaeskey
  IF NOT %ERRORLEVEL% EQU 0 (
    echo. %me%: tpm_unbindaeskey build failed
	call:ExitBatch
	REM EXIT /b %ERRORLEVEL%
  )
GOTO:EOF

:ExitBatch - Cleanly exit batch processing, regardless how many CALLs
if not exist "%temp%\ExitBatchYes.txt" call :buildYes
call :CtrlC <"%temp%\ExitBatchYes.txt" 1>nul 2>&1
:CtrlC
cmd /c exit -1073741510

:buildYes - Establish a Yes file for the language used by the OS
pushd "%temp%"
set "yes="
copy nul ExitBatchYes.txt >nul
for /f "delims=(/ tokens=2" %%Y in (
  '"copy /-y nul ExitBatchYes.txt <nul"'
) do if not defined yes set "yes=%%Y"
echo %yes%>ExitBatchYes.txt
popd
exit /b

endlocal