echo. "%CD%"
set ta_dir=%CD%
echo. "%ta_dir%"

set tacmdstr1="%ta_dir%\TrustAgentTray.exe"
schtasks /create /sc ONLOGON /tn TrustAgentTray /tr %tacmdstr1% /rl HIGHEST /f
echo. "%tacmdstr1%"
schtasks /run /tn TrustAgentTray
