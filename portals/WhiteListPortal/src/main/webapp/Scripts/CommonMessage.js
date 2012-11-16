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


var uploadFileHelp = [];

uploadFileHelp[0]="Please provide file in following format :";
uploadFileHelp[1]="PCR Name:PCR Value";
uploadFileHelp[2]="e.g.   17:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8";

var Manifestloadertool= [];

Manifestloadertool[0]="White List Manifest Loader Tool :";
Manifestloadertool[1]="";
Manifestloadertool[2]="";
Manifestloadertool[3]="* The White List Manifest Loader Tool installation package comes along with the WLM Portal installation. [C:\inetpub\wwwroot\WLMPortal\WLMLoaderToo\].";
Manifestloadertool[4]="* The tool can be installed either on the same server as the WLM portal or on system that is on the same network as the Mt. Wilson database.";
Manifestloadertool[5]="* The pre-requisite for running the tool is the .NET Framework 4.0.";
Manifestloadertool[6]="* Detailed documentation on the configuration and running of the tool would be available to the user post installation.";
