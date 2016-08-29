#Open Cloud Integrity Technology


**The Open CIT project provides:** A cloud management tool software development kit (SDK) source code and binaries


**Key features include:**
* Provides the status of chain of trust of BIOS, firmware, OS kernel & hypervisor by verifying against configured good known values (Whitelists)
* Audit logging for all changes including tracking of the host trust status changes
* RESTful API interface for easier 3rd party integration
* Self-extracting installers for ease of setup & Reference UI portal
* Supports Role based access control (RBAC) by implementing the shiro framework for API authentication.
* Ability to tag/verify hosts with custom attributes (a.k.a Asset Tags) stored in TPM. Ex: Location attributes
* OpenStack integration to utilize asset tags for advanced VM management.
* Mutual SSL authentication supported across all the communication channels.
* User defined TLS policy management for hosts connections.


**Below the Distributions currently supported and the Open Stack version used for our extensions:**
* Linux distributions:  Ubuntu 12.04 LTS, 14.04 LTS, RHEL 6.5 and 7.x, on KVM
* OS platforms that are supported for remote attestation: Citrix XenServer 6.2, VMWare ESXi 5.5, 6, Ubuntu 12.04 LTS, 14.04 LTS, RHEL 6.5 and 7.x,
* Open Stack extensions supported:  Kilo & Liberty

 Please see the [How to Build](https://github.com/opencit/opencit/wiki/Open-CIT-2.0.7---How-To-Build) for further details on how to build and get started with Open CIT.


For more information on the project, please visit our [01.org website](https://01.org/opencit)
