package com.intel.mountwilson.manifest.strategy.helper;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;


import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mountwilson.manifest.data.PcrManifest;
import com.intel.mountwilson.util.vmware.VMwareClient;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmDigestInfo;

public class VMWare50Esxi50   {
	Logger log = Logger.getLogger(getClass().getName());
	

//
//	private HashMap<String, ? extends IManifest> getQuoteInformationForHost(
//			String hostName, final List<String> requestedPcrs,
//			String vCenterConnectionString)  {
//		
//		HashMap<String, ? extends IManifest> manifestMap = new VCenterHost(hostName, vCenterConnectionString ) {
//			
//			@Override
//			public HashMap<String, ? extends IManifest> processReport(String esxVersion,HostTpmAttestationReport report) {
//
//				HashMap<String, ? extends IManifest> pcrManifestMap; // = new HashMap<String, IManifest>();
//
//				pcrManifestMap = getPcrManiFest(report, requestedPcrs);
//				
//				return pcrManifestMap;
//				
//			}
//
//			@Override
//			public HashMap<String, ? extends IManifest> processDigest(String esxVersion,
//					List<HostTpmDigestInfo> htdis) {
//
//				HashMap<String, ? extends IManifest> pcrManifestMap; // = new HashMap<String, IManifest>();
//
//				pcrManifestMap = getPcrManiFest(htdis, requestedPcrs);
//				
//				return pcrManifestMap;
//			}
//
//		}.getManifestMap(); 
//
//
//		log.info("PCR map " + manifestMap);
//
//		return manifestMap;
//	}

	public HashMap<String, PcrManifest> getPcrManiFest(
			HostTpmAttestationReport report, List<String> requestedPcrs) {
		HashMap<String, PcrManifest> pcrMap = new HashMap<String, PcrManifest>();

		for (HostTpmDigestInfo hostTpmDigestInfo : report.getTpmPcrValues()) {
			if (requestedPcrs.contains(String.valueOf(hostTpmDigestInfo
					.getPcrNumber()))) {
				String digestValue = VMwareClient.byteArrayToHexString(hostTpmDigestInfo.getDigestValue());
				pcrMap.put(String.valueOf(hostTpmDigestInfo.getPcrNumber()),
						new PcrManifest(hostTpmDigestInfo.getPcrNumber(),
								digestValue));
			}
		}
		
		if (pcrMap.size() != requestedPcrs.size()) {
			throw new ASException(ErrorCode.AS_HOST_MANIFEST_MISSING_PCRS);
		}
		return pcrMap;
	}

	public HashMap<String, ? extends IManifest> getPcrManiFest(
			List<HostTpmDigestInfo> htdis, List<String> requestedPcrs) {
		HashMap<String, PcrManifest> pcrMap = new HashMap<String, PcrManifest>();
		
		for (HostTpmDigestInfo htdi : htdis) {


			if (requestedPcrs.contains(String.valueOf(htdi
					.getPcrNumber()))) {
				String digest = VMwareClient.byteArrayToHexString(htdi.getDigestValue());
				pcrMap.put(
						String.valueOf(htdi.getPcrNumber()),
						new PcrManifest(htdi.getPcrNumber(), digest));
			}
		}
		
		return pcrMap;
	}
	
	
}
