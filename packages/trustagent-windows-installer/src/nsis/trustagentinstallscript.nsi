!include "MUI.nsh"
!include "MUI2.nsh"
!include "wordfunc.nsh"
!include "FileFunc.nsh"
!include "nsDialogs.nsh"
!include "WinMessages.nsh"
# Name of application
Name "Intel CIT Trust Agent"

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
var dialog
var label1
var /Global text1
var /Global INIFILE
var /Global MTWILSON_API_URL
var /Global MTWILSON_API_USERNAME
var /Global MTWILSON_API_PASSWORD
var /Global MTWILSON_TLS_CERT_SHA1
var /Global MTWILSON_TLS_CERT_SHA256

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
LangString CIT_SUBTITLE ${LANG_ENGLISH} "Checking installation of prerequisites for Intel CIT Trust Agent"
Page Custom CITServerPage CITServerLeave
!insertmacro MUI_PAGE_DIRECTORY
LangString INSTALL_PREREQ_TITLE ${LANG_ENGLISH} "Setting up configuration for Installation"
LangString ENV_SUBTITLE ${LANG_ENGLISH} "CIT Server and Trust Agent environment settings"
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
        File /r "..\bin\agenthandler.cmd"
        File /r "..\bin\getvmmver.cmd"
        File /r "..\bin\tasetup.cmd"
        File /r "..\bin\taupgrade.cmd"
        File /r "..\bin\tpm_signdata.exe"
        File /r "..\bin\tpm_unbindaeskey.exe"
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

        # Add TrustAgent to PATH Environment Variable
        Push "$INSTDIR\bin"
        Call AddToPath

        # Run tasetup.cmd
        IfFileExists $TEMP\TrustAgent_Backup\*.* restore fresh
        restore:
                ExecWait '$INSTDIR\bin\taupgrade.cmd'
                RMDir /r "$INSTDIR\configuration"
                CopyFiles "$TEMP\TrustAgent_Backup\java.security" "$INSTDIR\jre\lib\java.security"
                CopyFiles "$TEMP\TrustAgent_Backup\configuration" "$INSTDIR"
                CopyFiles "$TEMP\TrustAgent_Backup\trustagent.env" "$INSTDIR"
                RMDir /r "$TEMP\TrustAgent_Backup"
                Goto setupcomplete
        fresh:
               nsExec::ExecToLog '$INSTDIR\bin\tasetup.cmd'
               Goto setupcomplete
        setupcomplete:
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

        ReadRegStr $3 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TrustAgent" "DisplayName"
        ReadRegStr $4 HKLM "Software\Wow6432Node\Microsoft\Windows\CurrentVersion\Uninstall\TrustAgent" "DisplayName"
        StrCmp $3 "TrustAgent" previous
        StrCmp $4 "TrustAgent" previous
        Goto prereq

        previous:
                 MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION "TrustAgent is already installed. Click `OK` to remove the previous version or `Cancel` to cancel this upgrade." IDOK uninst
                 Abort
        uninst:
               ClearErrors
               CreateDirectory $TEMP\TrustAgent_Backup
               CopyFiles "$INSTDIR\configuration" "$TEMP\TrustAgent_Backup\configuration"
               CopyFiles "$INSTDIR\trustagent.env" "$TEMP\TrustAgent_Backup\trustagent.env"
               CopyFiles "$INSTDIR\java.security" "$TEMP\TrustAgent_Backup\java.security"
               ExecWait $INSTDIR\Uninstall.exe


        prereq:
                ; Start Code to specify ini file path
                StrCpy "$INIFILE" "$EXEDIR\system.ini"
                IfFileExists "$INIFILE" 0 file_not_found
                            goto proceed_further
                file_not_found:
                            MessageBox MB_OK "System Configuration file doesn't exists in installer folder"
                            Abort
                proceed_further: ; End Code to specify ini file path
        
        
                StrCpy $2 "Name like '%%Microsoft Visual C++ 2013 x64 Minimum Runtime%%'"
                nsExec::ExecToStack 'wmic product where "$2" get name'
                Pop $0
                Pop $1
                ${StrStr} $0 $1 "Microsoft Visual C++ 2013 x64 Minimum Runtime"
                StrCmp $0 "" notfound
                     StrCpy $vcr1Flag 1
                     Goto done1
                notfound:
                     StrCpy $vcr1Flag 0
                     MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION "Microsoft Visual C++ not installed. Click `OK` to install or`Cancel` to cancel TrustAgent installation." IDOK inst
                     Abort
                inst:
                     SetOutPath $INSTDIR\

                     File "..\tpmtool\vcredist_x64.exe"
                     ExecWait '$INSTDIR\vcredist_x64.exe /install /passive /norestart'
                     StrCpy $2 "Name like '%%Microsoft Visual C++ 2013 x64 Minimum Runtime%%'"
                     nsExec::ExecToStack 'wmic product where "$2" get name'
                     Pop $0
                     Pop $1
                     ${StrStr} $0 $1 "Microsoft Visual C++ 2013 x64 Minimum Runtime"
                     StrCmp $0 "" +2
                     StrCpy $vcr1Flag 1

                done1:

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
                  MessageBox MB_OK "Microsoft Visual C++ not installed properly. Exiting the Trust Agent Installation.."
                  Quit
        ${endif}
        ${NSD_CreateLabel} 0 100 100% 12u "Please ensure that Intel CIT Server is running for CIT Trust Agent."
        Pop $mylabel
        nsDialogs::Show
FunctionEnd

Function CITServerLeave
FunctionEnd

!define __NSD_TextMultiline_CLASS EDIT
!define __NSD_TextMultiline_STYLE ${DEFAULT_STYLES}|${WS_VSCROLL}|${WS_TABSTOP}|${ES_AUTOHSCROLL}|${ES_MULTILINE}|${ES_WANTRETURN}
!define __NSD_TextMultiline_EXSTYLE ${WS_EX_WINDOWEDGE}|${WS_EX_CLIENTEDGE}
!insertmacro __NSD_DefineControl TextMultiline

Function EnvCustomPage
        IfFileExists $TEMP\TrustAgent_Backup\*.* skip continue
        skip:
                Abort
        continue:
                !insertmacro MUI_HEADER_TEXT $(INSTALL_PREREQ_TITLE) $(ENV_SUBTITLE)
                nsDialogs::Create 1018
				Pop $dialog
	        
                ReadINIStr $MTWILSON_API_URL "$INIFILE" "TRUST_AGENT" "MTWILSON_API_URL"
                ReadINIStr $MTWILSON_API_USERNAME "$INIFILE" "TRUST_AGENT" "MTWILSON_API_USERNAME"
                ReadINIStr $MTWILSON_API_PASSWORD "$INIFILE" "TRUST_AGENT" "MTWILSON_API_PASSWORD"
                ReadINIStr $MTWILSON_TLS_CERT_SHA1 "$INIFILE" "TRUST_AGENT" "MTWILSON_TLS_CERT_SHA1"
	        ReadINIStr $MTWILSON_TLS_CERT_SHA256 "$INIFILE" "TRUST_AGENT" "MTWILSON_TLS_CERT_SHA256"
	
                ${NSD_CreateLabel} 0 0 100% 20% "MTWILSON_API_URL : $MTWILSON_API_URL"
                ${NSD_CreateLabel} 0 10% 100% 20% "MTWILSON_API_USERNAME : $MTWILSON_API_USERNAME"
                ${NSD_CreateLabel} 0 20% 100% 20% "MTWILSON_API_PASSWORD : $MTWILSON_API_PASSWORD"
	        ${If} $MTWILSON_TLS_CERT_SHA1 == ""
			${NSD_CreateLabel} 0 30% 100% 20% "MTWILSON_TLS_CERT_SHA256 : $MTWILSON_TLS_CERT_SHA256"
		${ELSE}
			${NSD_CreateLabel} 0 30% 100% 20% "MTWILSON_TLS_CERT_SHA1 : $MTWILSON_TLS_CERT_SHA1"
                ${EndIf}

                StrCpy $text1 ""
                StrCpy $R1 ""
                StrCpy $R1 "MTWILSON_API_URL=$MTWILSON_API_URL"
                StrCpy $R1 "$R1$\r$\nMTWILSON_API_USERNAME=$MTWILSON_API_USERNAME"
                StrCpy $R1 "$R1$\r$\nMTWILSON_API_PASSWORD=$MTWILSON_API_PASSWORD"

		${If} $MTWILSON_TLS_CERT_SHA1 == ""
                	StrCpy $R1 "$R1$\r$\nMTWILSON_TLS_CERT_SHA256=$MTWILSON_TLS_CERT_SHA256"
                ${ELSE}
                	StrCpy $R1 "$R1$\r$\nMTWILSON_TLS_CERT_SHA1=$MTWILSON_TLS_CERT_SHA1"
                ${EndIf}

                StrCpy $text1 $R1

                ${NSD_CreateLabel} 0% 85% 100% 15% "Above settings will be saved in $INSTDIR\trustagent.env."
		Pop $label1
		ShowWindow $label1 ${SW_SHOW}

		Pop $R2
		Pop $R1
		Pop $R0

        nsDialogs::Show
FunctionEnd

Function EnvCustomLeave
        Push $R0
        Push $R1

        StrCpy $R0 $text1
        StrCmp $R0 "" textboxcheck
        SetOutPath $INSTDIR
        FileOpen $R1 "trustagent.env" w
        FileWrite $R1 $R0
        FileClose $R1
        goto exitfunc
        textboxcheck:
                Pop $R1
                Pop $R0
                MessageBox MB_OK|MB_ICONEXCLAMATION "Please enter valid settings."
                Abort
        exitfunc:
                Pop $R1
                Pop $R0
FunctionEnd

; ----------------------------------------------------------
; ****************** END OF FUNCTIONS **********************
; ----------------------------------------------------------
