!include "MUI.nsh"
!include "MUI2.nsh"
!include "InstallOptions.nsh"
!include "LogicLib.nsh"
!include "winmessages.nsh"
!include "wordfunc.nsh"
!include "FileFunc.nsh"
!include "nsDialogs.nsh"

# Name of application
Name "Intel CIT TrustAgent"

# Create Setup(installer) file
OutFile "cit_trustagentsetup.exe"

# Set the default Installation Directory
InstallDir "$PROGRAMFILES\Intel\TrustAgent"

# Set the text which prompts the user to enter the installation directory
DirText "Please choose a directory to which you'd like to install this application."

# Show install
ShowInstDetails show

var mylabel
var vcr1Flag
var vcr2Flag
!define MUI_ICON "TAicon.ico"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "TAicon_centered.bmp"
!define MUI_HEADERIMAGE_RIGHT
; ------------------------------------------------------------------
; ***************************** PAGES ******************************
; ------------------------------------------------------------------
!insertmacro MUI_PAGE_WELCOME
LangString PREREQ_TITLE ${LANG_ENGLISH} "Checking Environment for Installation"
LangString CIT_SUBTITLE ${LANG_ENGLISH} "Checking installation of prerequisites for Intel CIT TrustAgent"
Page Custom CITServerPage CITServerLeave
!insertmacro MUI_PAGE_DIRECTORY
LangString INSTALL_PREREQ_TITLE ${LANG_ENGLISH} "Setting up prerequisites for Installation"
LangString ENV_SUBTITLE ${LANG_ENGLISH} "CIT Trustagent environment settings"
Page Custom EnvCustomPage EnvCustomLeave
!insertmacro MUI_PAGE_INSTFILES

# These indented statements modify settings for MUI_PAGE_FINISH
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
!define MUI_FINISHPAGE_SHOWREADME $INSTDIR\readme.txt
!insertmacro MUI_PAGE_FINISH


;Languages
!insertmacro MUI_LANGUAGE "English"
; -------------------------------------------------------------------------
; ***************************** END OF PAGES ******************************
; -------------------------------------------------------------------------


; ----------------------------------------------------------------------------------
; ******************************** IN-BUILT FUNCTIONS ******************************
; ----------------------------------------------------------------------------------

# Built-in Function StrStr
!define StrStr "!insertmacro StrStr"

!macro StrStr ResultVar String SubString
  Push `${String}`
  Push `${SubString}`
  Call StrStr
  Pop `${ResultVar}`
!macroend

Function StrStr
/*After this point:
  ------------------------------------------
  $R0 = SubString (input)
  $R1 = String (input)
  $R2 = SubStringLen (temp)
  $R3 = StrLen (temp)
  $R4 = StartCharPos (temp)
  $R5 = TempStr (temp)*/

  ;Get input from user
  Exch $R0
  Exch
  Exch $R1
  Push $R2
  Push $R3
  Push $R4
  Push $R5

  ;Get "String" and "SubString" length
  StrLen $R2 $R0
  StrLen $R3 $R1
  ;Start "StartCharPos" counter
  StrCpy $R4 0

  ;Loop until "SubString" is found or "String" reaches its end
  ${Do}
    ;Remove everything before and after the searched part ("TempStr")
    StrCpy $R5 $R1 $R2 $R4

    ;Compare "TempStr" with "SubString"
    ${IfThen} $R5 == $R0 ${|} ${ExitDo} ${|}
    ;If not "SubString", this could be "String"'s end
    ${IfThen} $R4 >= $R3 ${|} ${ExitDo} ${|}
    ;If not, continue the loop
    IntOp $R4 $R4 + 1
  ${Loop}

/*After this point:
  ------------------------------------------
  $R0 = ResultVar (output)*/

  ;Remove part before "SubString" on "String" (if there has one)
  StrCpy $R0 $R1 `` $R4

  ;Return output to user
  Pop $R5
  Pop $R4
  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0
FunctionEnd
; ----------------------------------------------------------------------------------
; *************************** END OF IN-BUILT FUNCTIONS ****************************
; ----------------------------------------------------------------------------------

; ----------------------------------------------------------------------------------
; *************************** SECTION FOR INSTALLING *******************************
; ----------------------------------------------------------------------------------

Section "install"

        # Set output path to the installation directory (also sets the working directory for shortcuts)
        SetOutPath $INSTDIR\

        # Copy Program Files to installation directory
        # bin
        CreateDirectory $INSTDIR\bin
        SetOutPath $INSTDIR\bin
        File /r "..\bin\tagent.cmd"
        File /r "..\bin\tasetup.cmd"
        File /r "..\bin\tpm_bindaeskey"
        File /r "..\bin\tpm_createkey"
        File /r "..\bin\tpm_signdata"
        File /r "..\bin\tpm_unbindaeskey"
        File /r "..\tpmtool\TpmAtt.dll"
        File /r "..\tpmtool\TPMTool.exe"
        
        SetOutPath $INSTDIR\
        
        File /r "..\bootdriver"
        File /r "..\configuration"
        File /r "..\env.d"
        File /r "..\hypertext"
        File /r "..\java"
        File /r "..\jre"
        File "..\feature.xml"
        File "..\java.security"
        File "..\version"
        File "readme.txt"
        File "TAicon.ico"
        File "..\tpmtool\vcredist_x64.exe"
        File "TrustAgent.exe"
        File "TrustAgentTray.exe"
        File "nocmd.vbs"
        File "initsvcsetup.cmd"
        File "inittraysetup.cmd"

        ;
        # If trustagent.env file is not already created by Installer UI, copy from extracted files
        IfFileExists "$INSTDIR\trustagent.env" exists doesnotexist
        exists:
                goto end_of_check
        doesnotexist:
                File "..\trustagent.env"
        end_of_check:
        # If silent installation, check if trustagent.env file is passed as argument '/E'
        ${GetOptions} $cmdLineParams "/E=" $R0
        IfFileExists $R0 envpara noenvpara
        envpara:
                CopyFiles $R0 $INSTDIR
                goto end_of_para_check
        noenvpara:
        end_of_para_check:

        # Create Uninstaller for application
        WriteUninstaller $INSTDIR\Uninstall.exe

        # Create Useful Shortcuts
        CreateDirectory "$SMPROGRAMS\Intel"
        CreateDirectory "$SMPROGRAMS\Intel\TrustAgent"
        CreateDirectory "$INSTDIR\logs"
        CreateDirectory "$INSTDIR\var"
        CreateShortCut "$SMPROGRAMS\Intel\TrustAgent\Uninstall Example Application 1.lnk" "$INSTDIR\Uninstall.exe"

        # Create Registry Keys for Add/Remove Programs in Control Panel
        WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TrustAgent" "DisplayName" "TrustAgent"
        WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TrustAgent" "UninstallString" "$INSTDIR\Uninstall.exe"
        WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TrustAgent" "Publisher" "Intel Corporation"
        WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TrustAgent" "DisplayVersion" "1.0"
        WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TrustAgent" "DisplayIcon" "$INSTDIR\TAicon.ico"

        # Create System Environment Variable - TRUSTAGENT_HOME
        !define env_hklm 'HKLM "SYSTEM\CurrentControlSet\Control\Session Manager\Environment"'
        !define env_hkcu 'HKCU "Environment"'
        WriteRegExpandStr ${env_hklm} TRUSTAGENT_HOME $INSTDIR
        SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment" /TIMEOUT=5000
        
        # Create Firewall Rule to open port 1443 for CIT Server
        nsExec::Exec 'cmd /k netsh advfirewall firewall add rule name="trustagent" protocol=TCP dir=in localport=1443 action=allow'

        # Run tasetup.cmd
        
        ExecWait '$INSTDIR\bin\tasetup.cmd'
        ExecWait '$INSTDIR\initsvcsetup.cmd'
        ReadRegStr $0 HKLM "Software\Microsoft\Windows NT\CurrentVersion" "ProductName"
        StrCpy $6 "Hyper-V Server 2012 R2"
        StrCmp $0 $6 hypercheck
                ExecWait '$INSTDIR\inittraysetup.cmd'
                Goto hyperdone
        hypercheck:
                 Goto hyperdone
        hyperdone:
                  Delete $INSTDIR\initsvcsetup.cmd
                  Delete $INSTDIR\inittraysetup.cmd
        

SectionEnd

; ----------------------------------------------------------------------------------
; ************************** SECTION FOR UNINSTALLING ******************************
; ----------------------------------------------------------------------------------

Section "Uninstall"

        # Remove TrustAgent service and tasks
        nsExec::Exec 'cmd /k schtasks /end /tn TrustAgent /f'
        nsExec::Exec 'cmd /k schtasks /delete /tn TrustAgent /f'
        nsExec::Exec 'sc stop TrustAgent'
        nsExec::Exec 'sc delete TrustAgent'
        nsExec::Exec 'cmd /k schtasks /end /tn TrustAgentTray /f'
        nsExec::Exec 'cmd /k schtasks /delete /tn TrustAgentTray /f'
        nsExec::Exec 'wmic process where $\"name like $\'TrustAgentTray.exe$\'$\" call terminate'


        # Uninstall CITBOOTDRIVER
        nsExec::Exec 'cmd /k "$INSTDIR\bootdriver\citbootdriversetup.exe" uninstall'
        
        # Remove Firewall rule
        nsExec::Exec 'cmd /k netsh advfirewall firewall delete rule name="trustagent"'

        # Remove files from installation directory
        Delete $INSTDIR\TrustAgent.exe

        Delete $INSTDIR\TrustAgentTray.exe
        Delete $INSTDIR\Uninstall.exe
        RMDir /r $INSTDIR

        # Remove system environment variable TRUSTAGENT_HOME
        DeleteRegValue ${env_hklm} TRUSTAGENT_HOME
        SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment" /TIMEOUT=5000

        # Delete uninstallation shortcut
        Delete "$SMPROGRAMS\Intel\TrustAgent\Uninstall Example Application 1.lnk"

        # Delete Registry Keys
        DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\TrustAgent"
        DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\TrustAgent"

SectionEnd

; ----------------------------------------------------------------------------------
; ********************* END OF INSTALL/UNINSTALL SECTIONS **************************
; ----------------------------------------------------------------------------------


; ----------------------------------------------------------
; ********************* FUNCTIONS **************************
; ----------------------------------------------------------

Function .onInit
        !ifdef IsSilent
                SetSilent silent
        !endif
        var /GLOBAL cmdLineParams
        Push $R0
        ${GetParameters} $cmdLineParams
        Pop $R0

        StrCpy $2 "Name like '%%Microsoft Visual C++ 2013 x64 Minimum Runtime%%'"
        StrCpy $3 "Name like '%%Microsoft Visual C++ 2013 x64 Additional Runtime%%'"
        nsExec::ExecToStack 'wmic product where "$2" get name'
        Pop $0
        Pop $1
        ${StrStr} $0 $1 "Microsoft Visual C++ 2013 x64 Minimum Runtime"
        StrCmp $0 "" notfound
                StrCpy $vcr1Flag 1
                Goto done
        notfound:
                 StrCpy $vcr1Flag 0
        done:
             nsExec::ExecToStack 'wmic product where "$3" get name'
             Pop $0
             Pop $1
             ${StrStr} $0 $1 "Microsoft Visual C++ 2013 x64 Additional Runtime"
                  StrCmp $0 "" notfound1
                         StrCpy $vcr2Flag 1
                         Goto done1
                  notfound1:
                           StrCpy $vcr2Flag 0
                  done1:
FunctionEnd


Function CITServerPage
        !insertmacro MUI_HEADER_TEXT $(PREREQ_TITLE) $(CIT_SUBTITLE)
        nsDialogs::Create 1018
        Pop $0
        ${NSD_CreateLabel} 0 0 100% 12u ""
        Pop $mylabel
        ${if} $vcr1Flag == 1
        ${Andif} $vcr2Flag == 1
               ${NSD_CreateLabel} 0 40 100% 12u "Microsoft Visual C++ 2013 Redistributable x64 is installed"
               Pop $mylabel
        ${else}
               ${NSD_CreateLabel} 0 40 100% 12u "Microsoft Visual C++ 2013 Redistributable x64 not found. Please run the following Visual C++ installation for Intel CIT Trustagent setup."
               Pop $mylabel
               ExecShell "open" '$INSTDIR\vcredist_x64.exe'
        ${endif}
        ${NSD_CreateLabel} 0 100 100% 12u "Please ensure that Intel CIT Server is running for CIT Trustagent."
        Pop $mylabel
        nsDialogs::Show
FunctionEnd
Function CITServerLeave
FunctionEnd


Function EnvCustomPage
        !insertmacro MUI_HEADER_TEXT $(INSTALL_PREREQ_TITLE) $(ENV_SUBTITLE)
        ReserveFile "InstallOptionsFile.ini"
        !insertmacro MUI_INSTALLOPTIONS_EXTRACT "InstallOptionsFile.ini"
        !insertmacro MUI_INSTALLOPTIONS_DISPLAY "InstallOptionsFile.ini"
FunctionEnd
Function EnvCustomLeave
        !insertmacro MUI_INSTALLOPTIONS_READ $R0 "InstallOptionsFile.ini" "Field 3" "State"
        !insertmacro MUI_INSTALLOPTIONS_READ $R1 "InstallOptionsFile.ini" "Field 5" "State"
        !insertmacro MUI_INSTALLOPTIONS_READ $R2 "InstallOptionsFile.ini" "Field 7" "State"
        !insertmacro MUI_INSTALLOPTIONS_READ $R3 "InstallOptionsFile.ini" "Field 9" "State"
        SetOutPath $INSTDIR
        FileOpen $0 "trustagent.env" w
        FileWrite $0 "MTWILSON_API_URL=$R0"
        FileWrite $0 "$\r$\n"
        FileWrite $0 "MTWILSON_API_USERNAME=$R1"
        FileWrite $0 "$\r$\n"
        FileWrite $0 "MTWILSON_API_PASSWORD=$R2"
        FileWrite $0 "$\r$\n"
        FileWrite $0 "MTWILSON_TLS_CERT_SHA1=$R3"
        FileClose $0
FunctionEnd


; ----------------------------------------------------------
; ****************** END OF FUNCTIONS **********************
; ----------------------------------------------------------