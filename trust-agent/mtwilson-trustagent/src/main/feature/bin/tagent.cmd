@echo off
setlocal enabledelayedexpansion

set DESC=Trust Agent
set NAME=tagent

REM ###################################################################################################
REM #Set environment specific variables here 
REM ###################################################################################################


set TRUSTAGENT_HOME=C:\Program Files (x86)\Intel\trustagent
set DAEMON=%TRUSTAGENT_HOME%\bin\%NAME%

set TRUSTAGENT_CONF=%TRUSTAGENT_HOME%\configuration
set TRUSTAGENT_JAVA=%TRUSTAGENT_HOME%\java
set TRUSTAGENT_BIN=%TRUSTAGENT_HOME%\bin
set TRUSTAGENT_ENV=%TRUSTAGENT_HOME%\env
set TRUSTAGENT_VAR=%TRUSTAGENT_HOME%\var
set TRUSTAGENT_PID_FILE=%TRUSTAGENT_VAR%\run\trustagent.pid
set TRUSTAGENT_HTTP_LOG_FILE=%TRUSTAGENT_VAR%\log\http.log
set TRUSTAGENT_AUTHORIZE_TASKS=download-mtwilson-tls-certificate download-mtwilson-privacy-ca-certificate download-mtwilson-saml-certificate request-endorsement-certificate request-aik-certificate
REM set TRUSTAGENT_TPM_TASKS=create-tpm-owner-secret create-tpm-srk-secret create-aik-secret take-ownership
REM set TRUSTAGENT_START_TASKS=create-keystore-password create-tls-keypair create-admin-user take-ownership
set TRUSTAGENT_TPM_TASKS=create-aik-secret
set TRUSTAGENT_START_TASKS=create-keystore-password create-tls-keypair create-admin-user
REM set TRUSTAGENT_VM_ATTESTATION_SETUP_TASKS=create-binding-key certify-binding-key create-signing-key certify-signing-key
set TRUSTAGENT_VM_ATTESTATION_SETUP_TASKS=
REM set TRUSTAGENT_SETUP_TASKS=update-extensions-cache-file create-keystore-password create-tls-keypair create-admin-user %TRUSTAGENT_TPM_TASKS% %TRUSTAGENT_AUTHORIZE_TASKS% %TRUSTAGENT_VM_ATTESTATION_SETUP_TASKS%
set TRUSTAGENT_SETUP_TASKS=update-extensions-cache-file

:: # load environment variables (these may override the defaults set above)
if exist %TRUSTAGENT_ENV%\NUL (
::  TRUSTAGENT_ENV_FILES=$(ls -1 $TRUSTAGENT_ENV/*)
::  for env_file in $TRUSTAGENT_ENV_FILES; do
::    . $env_file
::  done
  for /f  "delims=" %%a in ('dir "%TRUSTAGENT_ENV%" /b') do (
  echo. %%a
  cd "%TRUSTAGENT_ENV%"
  call %%a
  echo. %TEST_TA%
)

REM # not including configure-from-environment because we are running it always before the user-chosen tasks
REM # not including register-tpm-password because we are prompting for it in the setup.sh

set JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
set JAVA_OPTS="-Dlogback.configurationFile=$TRUSTAGENT_CONF\logback.xml -Dfs.name=trustagent"

REM @###################################################################################################

set TA_JARS=
REM # generated variables
for /f  "delims=" %%a in ('dir "%TRUSTAGENT_JAVA%" /s /b') do (
  set TA_JARS=%%a;!TA_JARS!
)

set CLASSPATH=%TRUSTAGENT_JAVA%\*

REM set CLASSPATH=%TA_JARS%
REM echo %CLASSPATH%

echo. %JAVA_HOME%

REM Before running any tagent commands update the extensions cache file 
REM java %JAVA_OPTS% com.intel.mtwilson.launcher.console.Main setup configure-from-environment update-extensions-cache-file --force

REM create a trustagent username "mtwilson" with no password and all privileges
REM which allows mtwilson to access it until mtwilson UI is updated to allow
REM entering username and password for accessing the trust agent
REM /usr/local/bin/tagent password mtwilson --nopass *:*

if "%1"=="start" (
  call:trustagent_start BBBB
) ELSE IF "%1"=="stop" (
  call:trustagent_stop
) ELSE IF "%1"=="setup" (
  call:trustagent_setup
) ELSE IF "%1"=="authorize" (
  call:trustagent_authorize
) ELSE IF "%1"=="help" (
  call:print_help
) ELSE (
  IF "%*"=="" (
    call:print_help
  ) ELSE (
    echo. "%*"
    java %JAVA_OPTS% com.intel.mtwilson.launcher.console.Main "%*"
  )
)
GOTO:EOF

REM functions
:trustagent_start
  echo. java %JAVA_OPTS% com.intel.mtwilson.launcher.console.Main start-http-server
GOTO:EOF

:trustagent_stop
  echo. stopping the trust agent
  echo. it could do a lot of things
GOTO:EOF

:trustagent_setup
  echo. setup the trust agent
  echo. it could do a lot of things
  set HARDWARE_UUID=
  for /f  "USEBACKQ" %%a in (`wmic csproduct get UUID /VALUE ^| findstr /C:"UUID"`) do ( 
    set _tmpvar=%%a
    set HARDWARE_UUID=!_tmpvar:~5!
  )
  echo. %HARDWARE_UUID%
  set tasklist=%*
  echo. %tasklist%
  IF "%tasklist%"=="" (
    set tasklist=%TRUSTAGENT_SETUP_TASKS%
  ) ELSE IF "%tasklist%"=="--force" (
      set tasklist=%TRUSTAGENT_SETUP_TASKS% --force
  )
  echo. %tasklist%
  REM java %JAVA_OPTS% com.intel.mtwilson.launcher.console.Main setup configure-from-environment %tasklist%
  exit /b
GOTO:EOF

:trustagent_authorize
  echo. trustagent authorization
  echo. it could do a lot of things
  set HARDWARE_UUID=
  for /f  "USEBACKQ" %%a in (`wmic csproduct get UUID /VALUE ^| findstr /C:"UUID"`) do ( 
    set _tmpvar=%%a
    set HARDWARE_UUID=!_tmpvar:~5!
  )
  echo. %HARDWARE_UUID%

  REM set authorize_vars="TPM_OWNER_SECRET TPM_SRK_SECRET MTWILSON_API_URL MTWILSON_API_USERNAME MTWILSON_API_PASSWORD MTWILSON_TLS_CERT_SHA1"
  set authorize_vars="MTWILSON_API_URL MTWILSON_API_USERNAME MTWILSON_API_PASSWORD MTWILSON_TLS_CERT_SHA1"

  REM local default_value
  REM for v in $authorize_vars
  REM do
  REM  default_value=$(eval "echo \$$v")
  REM  prompt_with_default $v "Required: $v" $default_value
  REM done
  REM export_vars $authorize_vars
  call:trustagent_setup --force %TRUSTAGENT_AUTHORIZE_TASKS%
GOTO:EOF

:print_help
    REM echo. "Usage: $0 start|stop|authorize|start-http-server|version"
    echo. "Usage: $0 start|stop|authorize|start-http-server|version"
    echo. "Usage: $0 setup [--force|--noexec] [task1 task2 ...]"
    echo. "Available setup tasks:"
    echo. configure-from-environment
    echo. %TRUSTAGENT_SETUP_TASKS%
    echo. register-tpm-password
GOTO:EOF

endlocal

