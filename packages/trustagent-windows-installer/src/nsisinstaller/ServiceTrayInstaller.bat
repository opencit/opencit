set tacmdstr="wscript '%TRUSTAGENT_HOME%\nocmd.vbs' '%TRUSTAGENT_HOME%\bin\tagent.cmd' start"
schtasks /create /sc ONSTART /tn TrustAgent /tr %tacmdstr%

sc create TrustAgent binPath= "%TRUSTAGENT_HOME%\TrustAgent.exe" start= auto DisplayName= "Intel TrustAgent"

set tacmdstr1="%TRUSTAGENT_HOME%\TrustAgentTray.exe"
schtasks /create /sc ONSTART /tn TrustAgentTray /tr %tacmdstr1%


schtasks /run /tn TrustAgentTray
sc start TrustAgent