cit-bkc-tool
============

BKC Tool for Intel(R) Cloud Integrity Technology

This tool tests the platform hardware and software stack to validate that
it is ready for Intel(R) Cloud Integrity Technology.

When you run `cit-bkc-tool` it will install all necessary dependencies and
perform various tests. The tool may automatically reboot the system when
installing dependencies and again when testing.

Usage:

 cit-bkc-tool
 cit-bkc-tool command

Available commands:

* help:         display this page
* clear:        clear self-test data
* status:       display current status
* report:       print most recent report
* uninstall:    uninstall cit-bkc-tool

Environment variables:

export CIT_BKC_REBOOT=yes
       (default) automatically reboot as needed

export CIT_BKC_REBOOT=no        
       no automatic reboots
