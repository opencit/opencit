# ⛔️ DEPRECATED - Open Cloud Integrity Technology

DEPRECATED. No new features or support will be provided. Please consider using Intel® Security Libraries for Data Center (Intel® SecL-DC). [Intel® SecL - DC libraries](https://01.org/intel-secl) will be the delivery mechanism for all future Intel Data Center security software components.

**The Open CIT project provides:** A cloud management tool software development kit (SDK) source code and binaries


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

Please see the 3.2 Open CIT Source Code section from the [Product Guide](https://github.com/opencit/opencit/wiki/Open-CIT-2.2-Product-Guide) for further details on how to build and get started with Open CIT.


For more information on the project, please visit our [01.org website](https://01.org/opencit)
