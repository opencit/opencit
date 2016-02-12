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
OutFile "Setup_TrustAgent.exe"

# Set the default Installation Directory
InstallDir "$PROGRAMFILES\Intel\TrustAgent"

# Set the text which prompts the user to enter the installation directory
DirText "Please choose a directory to which you'd like to install this application."

# Show install
ShowInstDetails show

var mylabel
var vcr1Flag
!define Environ 'HKCU "Environment"'
!define MUI_ICON "TAicon.ico"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "ta.bmp"
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

;-------------------------------------------


; AddToPath - Appends dir to PATH (ref: https://www.smartmontools.org/browser/trunk/smartmontools/os_win32/installer.nsi?rev=4110#L636)
;   (does not work on Win9x/ME)
;
; Usage:
;   Push "dir"
;   Call AddToPath

Function AddToPath
  Exch $0
  Push $1
  Push $2
  Push $3
  Push $4

  ; NSIS ReadRegStr returns empty string on string overflow
  ; Native calls are used here to check actual length of PATH

  ; $4 = RegOpenKey(HKEY_CURRENT_USER, "Environment", &$3)
  System::Call "advapi32::RegOpenKey(i 0x80000001, t'Environment', *i.r3) i.r4"
  IntCmp $4 0 0 done done
  ; $4 = RegQueryValueEx($3, "PATH", (DWORD*)0, (DWORD*)0, &$1, ($2=NSIS_MAX_STRLEN, &$2))
  ; RegCloseKey($3)
  System::Call "advapi32::RegQueryValueEx(i $3, t'PATH', i 0, i 0, t.r1, *i ${NSIS_MAX_STRLEN} r2) i.r4"
  System::Call "advapi32::RegCloseKey(i $3)"

  IntCmp $4 234 0 +4 +4 ; $4 == ERROR_MORE_DATA
    DetailPrint "AddToPath: original length $2 > ${NSIS_MAX_STRLEN}"
    MessageBox MB_OK "PATH not updated, original length $2 > ${NSIS_MAX_STRLEN}"
    Goto done

  IntCmp $4 0 +5 ; $4 != NO_ERROR
    IntCmp $4 2 +3 ; $4 != ERROR_FILE_NOT_FOUND
      DetailPrint "AddToPath: unexpected error code $4"
      Goto done
    StrCpy $1 ""

  ; Check if already in PATH
  Push "$1;"
  Push "$0;"
  Call StrStr
  Pop $2
  StrCmp $2 "" 0 done
  Push "$1;"
  Push "$0\;"
  Call StrStr
  Pop $2
  StrCmp $2 "" 0 done

  ; Prevent NSIS string overflow
  StrLen $2 $0
  StrLen $3 $1
  IntOp $2 $2 + $3
  IntOp $2 $2 + 2 ; $2 = strlen(dir) + strlen(PATH) + sizeof(";")
  IntCmp $2 ${NSIS_MAX_STRLEN} +4 +4 0
    DetailPrint "AddToPath: new length $2 > ${NSIS_MAX_STRLEN}"
    MessageBox MB_OK "PATH not updated, new length $2 > ${NSIS_MAX_STRLEN}."
    Goto done

  ; Append dir to PATH
  DetailPrint "Add to PATH: $0"
  StrCpy $2 $1 1 -1
  StrCmp $2 ";" 0 +2
    StrCpy $1 $1 -1 ; remove trailing ';'
  StrCmp $1 "" +2   ; no leading ';'
    StrCpy $0 "$1;$0"
  WriteRegExpandStr ${Environ} "PATH" $0
  SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment" /TIMEOUT=5000

done:
  Pop $4
  Pop $3
  Pop $2
  Pop $1
  Pop $0
FunctionEnd

;-------------------------------------

; RemoveFromPath - Removes dir from PATH
;
; Usage:
;   Push "dir"
;   Call RemoveFromPath

Function un.RemoveFromPath
  Exch $0
  Push $1
  Push $2
  Push $3
  Push $4
  Push $5
  Push $6

  ReadRegStr $1 ${Environ} "PATH"
  StrCpy $5 $1 1 -1
  StrCmp $5 ";" +2
    StrCpy $1 "$1;" ; ensure trailing ';'
  Push $1
  Push "$0;"
  Call un.StrStr1
  Pop $2 ; pos of our dir
  StrCmp $2 "" done

  DetailPrint "Remove from PATH: $0"
  StrLen $3 "$0;"
  StrLen $4 $2
  StrCpy $5 $1 -$4 ; $5 is now the part before the path to remove
  StrCpy $6 $2 "" $3 ; $6 is now the part after the path to remove
  StrCpy $3 "$5$6"
  StrCpy $5 $3 1 -1
  StrCmp $5 ";" 0 +2
    StrCpy $3 $3 -1 ; remove trailing ';'
  WriteRegExpandStr ${Environ} "PATH" $3
  SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment" /TIMEOUT=5000

done:
  Pop $6
  Pop $5
  Pop $4
  Pop $3
  Pop $2
  Pop $1
  Pop $0
FunctionEnd

;-----------------------------------
; StrStr - find substring in a string
;
; Usage:
;   Push "this is some string"
;   Push "some"
;   Call StrStr
;   Pop $0 ; "some string"

!macro StrStr1 un
Function ${un}StrStr1
  Exch $R1 ; $R1=substring, stack=[old$R1,string,...]
  Exch     ;                stack=[string,old$R1,...]
  Exch $R2 ; $R2=string,    stack=[old$R2,old$R1,...]
  Push $R3
  Push $R4
  Push $R5
  StrLen $R3 $R1
  StrCpy $R4 0
  ; $R1=substring, $R2=string, $R3=strlen(substring)
  ; $R4=count, $R5=tmp
  loop:
    StrCpy $R5 $R2 $R3 $R4
    StrCmp $R5 $R1 done
    StrCmp $R5 "" done
    IntOp $R4 $R4 + 1
    Goto loop
done:
  StrCpy $R1 $R2 "" $R4
  Pop $R5
  Pop $R4
  Pop $R3
  Pop $R2
  Exch $R1 ; $R1=old$R1, stack=[result,...]
FunctionEnd
!macroend
!insertmacro StrStr1 "un."

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
        
        # Add TrustAgent to PATH Environment Variable
        Push "$INSTDIR\bin"
        Call AddToPath
        
        # Create Firewall Rule to open port 1443 for CIT Server
        nsExec::Exec 'cmd /k netsh advfirewall firewall add rule name="trustagent" protocol=TCP dir=in localport=1443 action=allow'

        # Run tasetup.cmd
        ExecWait '$INSTDIR\bin\tasetup.cmd'
        ReadRegStr $0 HKLM "Software\Microsoft\Windows NT\CurrentVersion" "ProductName"
        StrCpy $6 "Hyper-V Server 2012 R2"
        StrCmp $0 $6 hypercheck
                ExecWait 'wscript "$INSTDIR\nocmd.vbs" "$INSTDIR\initsvcsetup.cmd"'
                ExecWait 'wscript "$INSTDIR\nocmd.vbs" "$INSTDIR\inittraysetup.cmd"'
                Goto hyperdone
        hypercheck:
                 ExecWait '$INSTDIR\initsvcsetup.cmd'
                 Goto hyperdone
        hyperdone:
                  Delete $INSTDIR\initsvcsetup.cmd
                  Delete $INSTDIR\inittraysetup.cmd
                  Delete $INSTDIR\nocmd.vbs
        

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
        
        # Remove TrustAgent from PATH Environment Variable
        Push "$INSTDIR\bin"
        Call un.RemoveFromPath

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
FunctionEnd

Function SetupVCRedist
         SetOutPath $INSTDIR\
         
         File "..\tpmtool\vcredist_x64.exe"
         ExecShell "open" '$INSTDIR\vcredist_x64.exe'
FunctionEnd


Function CITServerPage
        !insertmacro MUI_HEADER_TEXT $(PREREQ_TITLE) $(CIT_SUBTITLE)
        nsDialogs::Create 1018
        Pop $0
        ${NSD_CreateLabel} 0 0 100% 12u ""
        Pop $mylabel
        ${if} $vcr1Flag == 1
               ${NSD_CreateLabel} 0 40 100% 12u "Microsoft Visual C++ 2013 Redistributable x64 is installed"
               Pop $mylabel
        ${else}
               ${NSD_CreateLabel} 0 30 100% 12u "Microsoft Visual C++ 2013 Redistributable x64 not found."
               ${NSD_CreateLabel} 0 60 100% 12u "Please run the following Visual C++ installation for Intel CIT Trustagent setup."
               Pop $mylabel
               Call SetupVCRedist
        ${endif}
        ${NSD_CreateLabel} 0 100 100% 12u "Please ensure that Intel CIT Server is running for CIT Trustagent."
        Pop $mylabel
        nsDialogs::Show
FunctionEnd
Function CITServerLeave
        StrCpy $3 "Name like '%%Microsoft Visual C++ 2013 x64 Minimum Runtime%%'"
        nsExec::ExecToStack 'wmic product where "$3" get name'
        Pop $0
        Pop $1
        ${StrStr} $0 $1 "Microsoft Visual C++ 2013 x64 Minimum Runtime"
        StrCmp $0 "" notfound1
                StrCpy $vcr1Flag 1
                Goto done1
        notfound1:
                  MessageBox MB_OK "Microsoft Visual C++ not installed properly. Exiting the TrustAgent Installation.." 
                  Quit
        done1:
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
        FileWrite $0 "MTWILSON_API_URL=https://$R0:8443/mtwilson/v2"
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