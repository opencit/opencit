#Open Cloud Integrity Technology


**The Open CIT project provides:** A cloud management tool software development kit (SDK) source code and binaries

**3.2 Features**

* Added VM/Container Integrity functionality (see Product Guide for full details)
	* CIT 3.2 is now able to attest any file within the guest OS image of a virtual machine or Docker container during the VM/container power-on process.
* Added VM Privacy functionality (see Product Guide for full details)
	* CIT 3.2 now supports encrypting a virtual machine image, and gating decryption keys to positively-attested hosts that meet policy requirements.  This restricts sensitive information access to only those hosts that pass policy requirements using a trust attestation
* Added Extended Tboot support (see Product Guide for full details)
	* CIT 3.2 uses the "tbootxm" application to extend the standard trusted boot measurements to any file on the physical server.  This allows attestation of non-hypervisor servers, and carries the "chain of trust" from the TXT hardware root of trust to the application level.  
* Added the Key Broker service (KMS)
* Added the Key Broker Proxy (KMS Proxy)
* Added the Trust Director
* Added the Attestation Reporting Hub
* The CIT Attestation Service now includes an automated recurring check that runs every two minutes to search for host attestations that are within 5 minutes of expiring.  This process will then automatically trigger a new host attestation for all such hosts, automatically refreshing their trust status.  The frequency of this check and the validity period of the attestations can be configured in the Attestation Service .properties files.  By default the check will run every two minutes, and host attestations are valid for one hour.
* Using the new automatic attestation refresher service, many background attestation requests (including those performed by the OpenStack trust filter) have been changed to use existing valid host attestations rather than getting a new TPM quote.  This substantially improves the performance of all such requests.
* Cryptographic hash operations have been converted from SHA1 to SHA256, except where needed to support TPM 1.2 (which only supports SHA1)
* OpenStack integration has been updated for the Mitaka release and rewritten to use the Attestation Reporting Hub.  This adds a new interface for Nova and a table in the Nova   database for storing attestation details.  The scheduler filter now looks to the Nova database for attestation information pushed to this new table by the Attestation Reporting Hub, instead of directly requesting attestation details from the Attestation Service. See the Product Guide for additional details.

Known Issues in Release 3.2:

* Shared storage is not supported for the VM Integrity or VM Privacy functions
* VM Migration is not supported for the VM Integrity or VM Privacy functions
* Trust Policies containing files that begin with special characters cause the Trust Director to throw and error and failto save the Policy draft.  This causes any files/folders selected since the most recent draft of that policy to fail to be added.  THe Policy will generate and be attested by a host as normal, bt will be missing any of those files.  Special characters that are normally useable in Windows filesystems still work if they are in the middle of the file/folder name. Only files/folders that begin with a special character are affected.


**2.2 Features**

* TPM 2.0 support. 
    * Added support for platform and asset tag attestation of Linux and Windows hosts with TPM 2.0.
    * Support attestation of either SHA1 or SHA256 PCR banks on TPM 2.0.
    * Ubuntu 16.04 and RHEL 7.2, 7.3 (SHA1 and SHA256), Windows Server 2012 and Hyper-V Server 2012 (SHA1) are supported with TPM 2.0
* All the certificates and hashing algorithms used in CIT are upgraded to use SHA256.  SHA1 has been deprecated and will no longer be used.
* CIT Attestation Service UI has been updated to allow the user to select either the SHA1 or SHA256 PCR bank for Attestation of TPM 2.0 hosts.
    * The CIT  Attestation Service will automatically choose the strongest available algorithm for attestation (SHA1 for TPM 1.2, and SHA256 for TPM 2.0)
* CIT Attestation Service UI Whitelist tab no longer requires the user to select PCRs when whitelisting, and will automatically choose the PCRs to use based on the host OS and TPM version.  This is done to reduce confusion due to differing behaviors between TPM 1.2 and TPM 2.0 PCR usages.
* Additional changes made to support TPM 2.0
    * Linux hosts with TPM 2.0 will now utilize TPM2.0-TSS (TPM 2.0 Software Stack) and TPM2.0-tools instead of the legacy trousers and tpm-tools packages. The new TSS2 and TPM2.0-tools are packaged with the CIT Trust Agent installer.
    * TPM 2.0 Windows hosts use TSS.MSR (The TPM Software Stack from Microsoft Research) PCPTool.
    * TPM 1.2 hosts will continue to use the legacy TSS stack (trousers) and tpm-tools components.


New Prerequisites required for TPM 2.0 Support
              
* Kernel Driver must support TPM 2.0
    * RHEL 7.2 kernel version 3.10.0-327 or higher with latest update. Ubuntu 16.04 kernel needed is 4.4.
* Tboot version used for TPM 2.0 is tboot 1.9.4 or higher.

**2.1 Features**

* Attestation support for Windows platform 

**2.0.7 Features**

* Establish chain of trust of BIOS, firmware, OS kernel & hypervisor by verifying against configured good known values (Whitelists)
* Ability to tag/verify hosts with custom attributes (Asset Tags) stored in TPM. Ex: Location attributes
* Open Stack integration to utilize Platform Trust and asset tags for advanced VM management
* Mutual SSL authentication supported across all the communication channels
* RESTful API interface for easier 3rd party integration
* Audit logging for all changes including tracking of the host trust status changes
* Self-extracting installers for ease of setup & Reference UI portal
* User defined TLS policy management for host’s connections 



**Open CIT 2.2 currently supports the following Distributions and OpenStack versions for our extensions:**

* Ubuntu 16.04, RHEL 7.2 & RHEL 7.3, Windows Server 2012, and Hyper-V Server 2012 are supported with TPM 2.0
* Open Stack extensions supported:  Liberty & Mitaka

Please see the [Product Guide](https://github.com/opencit/opencit/wiki/Open-CIT-3.2-Product-Guide) for further details on how to build and get started with Open CIT.


For more information on the project, please visit our [01.org website](https://01.org/opencit)
