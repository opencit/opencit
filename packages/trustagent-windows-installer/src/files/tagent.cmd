@echo off
setlocal enabledelayedexpansion

set DESC="Trust Agent"
set NAME=tagent
set TRUSTAGENT_HOME=C:\Program Files (x86)\Intel\trustagent
set DAEMON=%TRUSTAGENT_HOME%\bin\%NAME%

rem ###################################################################################################
rem #Set environment specific variables here 
rem ###################################################################################################



set TRUSTAGENT_CONF=%TRUSTAGENT_HOME%\configuration
set TRUSTAGENT_JAVA=%TRUSTAGENT_HOME%\java

rem echo %TRUSTAGENT_CONF%
rem echo %TRUSTAGENT_JAVA%

set TRUSTAGENT_BIN=%TRUSTAGENT_HOME%\bin
set TRUSTAGENT_ENV=%TRUSTAGENT_HOME%\env.d
set TRUSTAGENT_VAR=%TRUSTAGENT_HOME%\var
set TRUSTAGENT_HTTP_LOG_FILE=%TRUSTAGENT_HOME%\log\http.log
set TRUSTAGENT_AUTHORIZE_TASKS="download-mtwilson-tls-certificate download-mtwilson-privacy-ca-certificate download-mtwilson-saml-certificate request-endorsement-certificate request-aik-certificate"
set TRUSTAGENT_TPM_TASKS="create-tpm-owner-secret create-tpm-srk-secret create-aik-secret take-ownership"
set TRUSTAGENT_START_TASKS="create-keystore-password create-tls-keypair create-admin-user take-ownership"
set TRUSTAGENT_VM_ATTESTATION_SETUP_TASKS="create-binding-key certify-binding-key create-signing-key certify-signing-key"
set TRUSTAGENT_SETUP_TASKS="update-extensions-cache-file create-keystore-password create-tls-keypair create-admin-user $TRUSTAGENT_TPM_TASKS $TRUSTAGENT_AUTHORIZE_TASKS $TRUSTAGENT_VM_ATTESTATION_SETUP_TASKS"
rem # not including configure-from-environment because we are running it always before the user-chosen tasks
rem # not including register-tpm-password because we are prompting for it in the setup.sh

set JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
set JAVA_OPTS="-Dlogback.configurationFile=$TRUSTAGENT_CONF\logback.xml -Dfs.name=trustagent"

rem @###################################################################################################

set TA_JARS=
rem # generated variables
for /f  "delims=" %%a in ('dir "%TRUSTAGENT_JAVA%" /s /b') do (
  set TA_JARS=%%a;!TA_JARS!
)

set CLASSPATH=%TRUSTAGENT_JAVA%\*
rem set CLASSPATH=%TA_JARS%

rem echo %CLASSPATH%

java %JAVA_OPTS% com.intel.mtwilson.launcher.console.Main start-http-server

endlocal

