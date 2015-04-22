package com.intel.mtwilson.trustagent.vrtmclient;

public class Factory {
	public static TCBuffer newTCBuffer(int rpId, RPCCall rpcCall) {
		
		switch(rpcCall) {
			case GET_RPID :
				return new TCBuffer(rpId, RPAPIIndex.VM2RP_GETRPID, 0, rpId);
			case GET_VMMETA:
				return new TCBuffer(rpId, RPAPIIndex.VM2RP_GETVMMETA, 0, rpId);
			case IS_VM_VERIFIED:
				return new TCBuffer(rpId, RPAPIIndex.VM2RP_ISVMVERIFIED, 0, rpId);
                        case GET_VM_ATTESTATION_REPORT_PATH:
                            return new TCBuffer(rpId, RPAPIIndex.VM2RP_GET_VM_ATTESTATION_REPORT_PATH, 0, rpId);
		}
		return null;
	}
}
