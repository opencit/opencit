echo. "%CD%"
set ta_dir=%CD%
echo. "%ta_dir%"
set tacmdstr="wscript '%ta_dir%\nocmd.vbs' '%ta_dir%\bin\tagent.cmd' start"
schtasks /create /sc ONSTART /tn TrustAgent /tr %tacmdstr% /f
sc create TrustAgent binPath= "%ta_dir%\TrustAgent.exe" start= auto DisplayName= "Intel TrustAgent"
sc description TrustAgent "Runs Intel CIT TrustAgent"
sc failure TrustAgent reset= 0 actions= restart/60000
net start TrustAgent /y
echo. "%tacmdstr%"

