package com.intel.mtwilson.trustagent.vrtmclient;

public enum RPCCall {
	GET_RPID,
	GET_VMMETA,
	IS_VM_VERIFIED,
        GET_VM_ATTESTATION_REPORT_PATH
}

class RPAPIIndex {
    public static final int RP2VM_GETRPID             =  35;  
    public static final int VM2RP_GETRPID             =  36;
    
    public static final int RP2VM_GETVMMETA           =  37;
    public static final int VM2RP_GETVMMETA           =  38;

    
    public static final int RP2VM_ISVMVERIFIED        =  39;
    public static final int VM2RP_ISVMVERIFIED        =  40;
    
    public static final int RP2VM_GET_VM_ATTESTATION_REPORT_PATH =  41;
    public static final int VM2RP_GET_VM_ATTESTATION_REPORT_PATH =  42;
    
}
