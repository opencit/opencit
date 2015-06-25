package com.intel.mtwilson.trustagent.vrtmclient;

public class Factory {
	public static TCBuffer newTCBuffer(RPCCall rpcCall) {
		
		switch(rpcCall) {
			case GET_RPID :
				return new TCBuffer(RPAPIIndex.VM2RP_GETRPID, 0);
			case GET_VMMETA:
				return new TCBuffer(RPAPIIndex.VM2RP_GETVMMETA, 0);
			case IS_VM_VERIFIED:
				return new TCBuffer(RPAPIIndex.VM2RP_ISVMVERIFIED, 0);
                        case GET_VM_ATTESTATION_REPORT_PATH:
                            return new TCBuffer(RPAPIIndex.VM2RP_GET_VM_ATTESTATION_REPORT_PATH, 0);
		}
		return null;
	}
}
