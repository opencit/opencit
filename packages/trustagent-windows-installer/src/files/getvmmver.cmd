@echo off
setlocal enabledelayedexpansion

  set VMMversion=
  set TEMPVerFile=%TEMP%\vmmver.txt
  wmic datafile where name="c:\\windows\\system32\\vmms.exe" get version /value > %TEMPVerFile%
  for /F "usebackq tokens=*" %%A in (`type "%TEMPVerFile%"`) do (
  	set _tmpvar=%%A
    set _tmpvar1=!_tmpvar:~8!
    set VMMversion=!_tmpvar1:~0,-1!
  )
  echo.%VMMVersion%