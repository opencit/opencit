@echo off
setlocal enabledelayedexpansion

REM # SCRIPT CONFIGURATION:
set package_name=trustagent

REM the package directory should be where the trust agent installed (e.g. package_dir=C:\Program Files (x86)\Intel\trustagent)
set package_bin=%~dp0
for %%a in ("%package_bin:~0,-1%") do set package_dir=%%~dpa

REM echo. %package_bin%
echo. ==Trust Agent located at: %package_dir%

REM ==set PATH for the current cmd 
set PATH=%PATH%;%package_bin%

set intel_conf_dir=%package_dir%\configuration
set package_config_filename=%intel_conf_dir%\%package_name%.properties
set package_env_filename=%package_dir%\%package_name%.env
set package_version_filename=%package_dir%\env.d\trustagent.version
set ASSET_TAG_SETUP="y"
set trustagent_cmd=%package_dir%\bin\agenthandler.cmd
set bootdriver_dir=%package_dir%\bootdriver

set logfile=%package_dir%\logs\install.log

REM ##Private Java install $JAVA_PACKAGE
ECHO. ==Unpack JAVA JRE==
  cd "%package_dir%\jre"
  jre.exe -qo > nul
  set JAVA_HOME=%package_dir%\jre
  cd "%package_bin%" 

REM patch java.security file
ECHO. ==Patch java.security file==
if exist "%JAVA_HOME%\lib\security\java.security" (
  REM echo. ==Replacing java.security file, existing file will be backed up==
  copy "%JAVA_HOME%\lib\security\java.security" "%JAVA_HOME%\lib\security\java.security.old" > nul
  copy "%package_dir%\java.security" "%JAVA_HOME%\lib\security\java.security" > nul
)

REM  # create trustagent.version file
echo. ==Create trustagent.version==
> "%package_version_filename%"  echo. "# Installed Trust Agent on %date% %time%"
>> "%package_version_filename%"  echo. "TRUSTAGENT_VERSION=%VERSION%"
>> "%package_version_filename%"  echo "TRUSTAGENT_RELEASE=\"%BUILD%\""

REM #copy backedup configuration dir to replace the one in trustagent root directory

REM #copy the trustagent.env in backup folder to trustagent root directory
