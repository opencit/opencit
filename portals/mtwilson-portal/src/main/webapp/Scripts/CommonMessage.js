var uploadFileHelp = [];
uploadFileHelp[0]="Provide a plain text file (.txt format) containing a list of hosts to be registered in the following format(S):";
uploadFileHelp[1]="Agent | Host_Name | connection string";
uploadFileHelp[2]="For Open Source Based Hosts:";
uploadFileHelp[3]="intel|Host_Name|Host_Port_no";
uploadFileHelp[4]="e.g.   intel|myTrustAgent|1443";
uploadFileHelp[5]="For VMWare Based Hosts:";
uploadFileHelp[6]="vmware|Host_Name|Vmware_Connection_String*";
uploadFileHelp[7]="e.g. vmware|myVmwareHost|https://192.168.1.0:443/sdk;Administrator;Password";
uploadFileHelp[8]="For Citrix Type";
uploadFileHelp[9]="citrix|Host_Name|Citrix_Connection_String*";
uploadFileHelp[10]="e.g.   citrix|myCitrixHost|https://192.168.1.0:443/;Administrator;Password";
uploadFileHelp[11]="* connection strings must be valid urls.  Please notice the / after the port number.  That is required"

var vCenterStringHelp = [];
vCenterStringHelp[0]="Please provide the hostname or IP address of VCenter. If the default port [443] is not used, specify the same after \":\" as shown below.";
vCenterStringHelp[1]="Ex: hostname.com";
vCenterStringHelp[2]="Ex: 192.168.1.1";
vCenterStringHelp[3]="Ex: 192.168.1.0:444";

var addLocationHelp = [];
addLocationHelp[0]="Please provide Location for server.";

var requiredPCRValuesHelp = [];
requiredPCRValuesHelp[0]="This option allows the user to change the PCRs to be used for White Listing.";
requiredPCRValuesHelp[1]=" ";
requiredPCRValuesHelp[2]="By default the tool selects the minimum required PCRs based on the host type.";
requiredPCRValuesHelp[3] = "PCR[0]- Holds the Core Root of Trust Measurement (CRTM) including BIOS start-up code, Power-on-self-test, and other OEM provided code such as embedded Option ROMs and other Host Platform Extensions.";
requiredPCRValuesHelp[4] = "PCR[1]- Host Platform Configuration measurement which includes BIOS setup or devices controlled by embedded Option ROMS.";
requiredPCRValuesHelp[5] = "PCR[2]- Option ROM Code measures option ROM code for add-in devices not provided by the OEM as an embedded device (e.g., PCI RAID adaptor, Network adaptor, etc.).";
requiredPCRValuesHelp[6] = "PCR[3]- Option ROM Configuration and Data - measures configuration data covered by option ROM code for add-in devices not provided by the OEM.";
requiredPCRValuesHelp[7] = "PCR[4]- IPL code that performs the initial boot, usually the Master Boot Record (MBR).";
requiredPCRValuesHelp[8] = "PCR[5]- IPL Configuration and Data (i.e., the configuration and data used by the IPL Code).";
requiredPCRValuesHelp[9] = "PCR[17]- Code and data associated with the secure launch process [SINIT, BIOS ACM, & Launch Control Policies.";
requiredPCRValuesHelp[10] = "PCR[18]- Measurement of the MLE [Tboot + First module in the grub.conf (if applicable)]";
requiredPCRValuesHelp[11] = "PCR[19-20]- Measurement of the core kernel module(s).";

var applicableWhiteListTargetHelp = [];
applicableWhiteListTargetHelp[0]="This option allows users to choose how the white list is configured.";
applicableWhiteListTargetHelp[1]=" ";
applicableWhiteListTargetHelp[2]="<b>BIOS Configuration</b>";
applicableWhiteListTargetHelp[3]="Select the <b>OEM</b> Option to provide whitelist for each OEM Type and BIOS Version.  All the Servers/Hosts of a given OEM Type & BIOS Version will be verified with this Whitelist.";
applicableWhiteListTargetHelp[4]="Select the <b>HOST</b> option to provide whitelist by Host and BIOS Version. (ex: for HP Servers, by design PCR 0 measurement changes for every host).";
applicableWhiteListTargetHelp[5]=" ";
applicableWhiteListTargetHelp[6]="<b>Hypervisor (VMM) Configuration</b>";
applicableWhiteListTargetHelp[7]="Select the <b>OEM</b> Option to provide whitelist for each OEM Type and VMM/OS Version.  All the Servers/Hosts of a given OEM Type & VMM/OS Version will be verified with this Whitelist.";
applicableWhiteListTargetHelp[8]="Select the <b>HOST</b> option to provide whitelist by Host and VMM/OS Version.  This whitelist will be used to verify <b>ONLY</b> the specified host.";
applicableWhiteListTargetHelp[9]="Select the <b>GLOBAL</b> option to provide the whitelist for each VMM/OS version and build number.  All the servers/hosts across all OEM types run this specified VMM/OS version and build will be verified with this whitelist.";



var configureWhiteHelp = [];
configureWhiteHelp[0]="This option allows the user to choose the White Lists that need to be configured.";
configureWhiteHelp[1]=" ";
configureWhiteHelp[2]="If both BIOS and VMM are selected, user will get an option to register the host also when he/she clicks on the 'Upload White List' button.";

var pcrHelp = [];

pcrHelp[0] = "PCR[0]- Holds the Core Root of Trust Measurement (CRTM) including BIOS start-up code, Power-on-self-test, and other OEM provided code such as embedded Option ROMs and other Host Platform Extensions.";
pcrHelp[1] = "PCR[1]- Host Platform Configuration measurement which includes BIOS setup or devices controlled by embedded Option ROMS.";
pcrHelp[2] = "PCR[2]- Option ROM Code � measures option ROM code for add-in devices not provided by the OEM as an embedded device (e.g., PCI RAID adaptor, Network adaptor, etc.).";
pcrHelp[3] = "PCR[3]- Option ROM Configuration and Data - measures configuration data covered by option ROM code for add-in devices not provided by the OEM.";
pcrHelp[4] = "PCR[4]- IPL code that performs the initial boot, usually the Master Boot Record (MBR).";
pcrHelp[5] = "PCR[5]- IPL Configuration and Data (i.e., the configuration and data used by the IPL Code).";
pcrHelp [6] = "PCR[17]- Code and data associated with the secure launch process.";
pcrHelp [7] = "PCR[18]- Measurement of the tBoot package.";
pcrHelp [8] = "PCR[19-20]- Measurement of the key packages of the of the MLE.";


var uploadManifestFileHelp = [];

uploadManifestFileHelp[0]="Please provide file in following format :";
uploadManifestFileHelp[1]="PCR Name:PCR Value";
uploadManifestFileHelp[2]="e.g.   17:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8";

var Manifestloadertool= [];

Manifestloadertool[0]="White List Manifest Loader Tool :";
Manifestloadertool[1]="";
Manifestloadertool[2]="";
Manifestloadertool[3]="* The White List Manifest Loader Tool installation package comes along with the WLM Portal installation. [C:\inetpub\wwwroot\WLMPortal\WLMLoaderToo\].";
Manifestloadertool[4]="* The tool can be installed either on the same server as the WLM portal or on system that is on the same network as the Mt. Wilson database.";
Manifestloadertool[5]="* The pre-requisite for running the tool is the .NET Framework 4.0.";
Manifestloadertool[6]="* Detailed documentation on the configuration and running of the tool would be available to the user post installation.";
