@echo off
set ta_dir=%CD%
sc create TrustAgent binPath= "%ta_dir%\TrustAgent.exe" start= auto DisplayName= "Intel TrustAgent"
sc description TrustAgent "Runs Intel CIT TrustAgent"
sc failure TrustAgent reset= 0 actions= restart/60000
net start TrustAgent /y

