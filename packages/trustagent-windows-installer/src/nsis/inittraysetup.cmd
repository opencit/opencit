@echo off
set ta_dir=%CD%

set tacmdstr1="%ta_dir%\TrustAgentTray.exe"
schtasks /create /sc ONLOGON /tn TrustAgentTray /tr %tacmdstr1% /rl HIGHEST /f
schtasks /run /tn TrustAgentTray
