#Open Cloud Integrity Technology


**The Open CIT project provides:** A cloud management tool software development kit (SDK) source code and binaries

**2.2 Beta Features**

* Added support for platform attestation of Linux and Windows TPM 2.0 hosts.
	* TPM 2.0 supports SHA1 and SHA256 symmetric and asymmetric cryptographic operations.
	* Ubuntu 16.04 and RHEL 7.2 (SHA1 and SHA256), Windows Server 2012 and Hyper-V Server 2012 (SHA1) are supported with TPM 2.0
* Additional changes made to support TPM 2.0
	* TPM 2.0 Linux hosts will now utilize TSS (TPM Software Stack) 2.0.
* TPM 2.0 Linux hosts will now use tpm2.0-tools instead of the legacy tpm-tools package.  The new tpm2.0-tools is packaged with the CIT Trust Agent installer.
	* TPM 2.0 Windows hosts now use TSS.MSR (The TPM Software Stack from Microsoft Research)
	* TPM 1.2 hosts will continue to use the legacy TSS stack components
* CIT Attestation Service will now use SHA256 for TLS certificate verification.  SHA1 has been deprecated and will no longer be used.
* CIT Attestation Service UI has been updated to allow the user to select either the SHA1 or SHA256 PCR bank for Attestation of TPM 2.0 hosts.
	* The CIT  Attestation Service will automatically choose the strongest available algorithm for attestation (SHA1 for TPM 1.2, and SHA256 for TPM 2.0)
* CIT Attestation Service UI Whitelist tab no longer requires the user to select PCRs when whitelisting, and will automatically choose the PCRs to use based on the host OS and TPM version.  This is done to reduce confusion due to differing behaviors between TPM 1.2 and TPM 2.0 measurements.

**2.1 Features**

* Attestation support for Windows platform 

**​2.0.7 Features**

* Establish chain of trust of BIOS, firmware, OS kernel & hypervisor by verifying against configured good known values (Whitelists)
* Ability to tag/verify hosts with custom attributes (Asset Tags) stored in TPM. Ex: Location attributes
* Open Stack integration to utilize Platform Trust and asset tags for advanced VM management
* Mutual SSL authentication supported across all the communication channels
* RESTful API interface for easier 3rd party integration
* Audit logging for all changes including tracking of the host trust status changes
* Self-extracting installers for ease of setup & Reference UI portal
* User defined TLS policy management for host’s connections 


**Below the Distributions currently supported and the Open Stack version used for our extensions:**

* Ubuntu 16.04, RHEL 7.2, Windows Server 2012, and Hyper-V Server 2012 are supported with TPM 2.0
* Open Stack extensions supported:  Liberty & Mitaka

 Please see the 3.1	Prepare the Build Environment section from the [Product Guide](https://github.com/opencit/opencit/blob/cit-dev-2.2-beta/CIT_2.2_Beta_ProductGuide.pdf) for further details on how to build and get started with Open CIT.


For more information on the project, please visit our [01.org website](https://01.org/opencit)
