var uploadFileHelp = [];
uploadFileHelp[0]="Please provide file in following format :";
uploadFileHelp[1]="Host_Name|Host_Port_no";
uploadFileHelp[2]="e.g.   KVM|9999";
uploadFileHelp[3]="or for VMWare Type";
uploadFileHelp[4]="e.g.   VMWare_Exsi|https://192.168.1.0/sdk;Administrator;Password";

var vCenterStringHelp = [];
vCenterStringHelp[0]="Please provide only the IP address of VCenter. If the default port [443] is not used, specify the same after \":\" as shown below.";
vCenterStringHelp[1]="Ex: 192.168.1.1";
vCenterStringHelp[2]="Ex: 192.168.1.0:444";

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
requiredPCRValuesHelp[9] = "PCR[17]- Code and data associated with the secure launch process.";
requiredPCRValuesHelp[10] = "PCR[18]- Measurement of the tBoot package.";
requiredPCRValuesHelp[11] = "PCR[19-20]- Measurement of the key packages of the of the MLE.";

var applicableWhiteListTargetHelp = [];
applicableWhiteListTargetHelp[0]="This option allows users to choose how the white list is configured.";
applicableWhiteListTargetHelp[1]="BIOS:OEM - The White List will be used to verify the BIOS for the hosts(servers) from the same OEM vendor.";
applicableWhiteListTargetHelp[2]="BIOS:Host - The White List will be used to verify the BIOS for just one host, which is specified as the 'White List Host'";
applicableWhiteListTargetHelp[3]=" ";
applicableWhiteListTargetHelp[4]="VMM:Global - The White List will be used to verify the VMM for the hosts(servers) from multiple OEM vendors.";
applicableWhiteListTargetHelp[5]="VMM:OEM - The White List will be used to verify the VMM for the hosts(servers) from the same OEM vendor.";
applicableWhiteListTargetHelp[6]="VMM:Host - The White List will be used to verify the VMM for just one host, which is specified as the 'White List Host'";



var configureWhiteHelp = [];
configureWhiteHelp[0]="This option allows the user to choose the White Lists that need to be configured.";
configureWhiteHelp[1]=" ";
configureWhiteHelp[2]="If both BIOS and VMM are selected, user will get an option to register the host also when he/she clicks on the 'Upload White List' button.";
