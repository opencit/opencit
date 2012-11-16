package com.intel.mountwilson.manifest.strategy.helper;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;


import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.manifest.data.ModuleManifest;
import com.intel.mountwilson.manifest.data.PcrModuleManifest;
import com.intel.mountwilson.util.vmware.VMwareClient;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmBootSecurityOptionEventDetails;
import com.vmware.vim25.HostTpmCommandEventDetails;
import com.vmware.vim25.HostTpmDigestInfo;
import com.vmware.vim25.HostTpmEventLogEntry;
import com.vmware.vim25.HostTpmOptionEventDetails;
import com.vmware.vim25.HostTpmSoftwareComponentEventDetails;

public class VMWare51Esxi51  extends VMwareClient  {
	Logger log = Logger.getLogger(getClass().getName());
	public HashMap<String, PcrModuleManifest> getPcrModuleManiFest(
			HostTpmAttestationReport report, List<String> requestedPcrs) {

		HashMap<String, PcrModuleManifest> pcrMap = new HashMap<String, PcrModuleManifest>();
		// first add all the pcrs

		getPcrManifest(report, requestedPcrs, pcrMap);

		getModuleManifest(report, pcrMap);

		if (pcrMap.size() != requestedPcrs.size()) {
			throw new ASException(ErrorCode.AS_HOST_MANIFEST_MISSING_PCRS);
		}
		return pcrMap;

	}

	private void getPcrManifest(HostTpmAttestationReport report,
			List<String> requestedPcrs,
			HashMap<String, PcrModuleManifest> pcrMap) {
		for (HostTpmDigestInfo hostTpmDigestInfo : report.getTpmPcrValues()) {
			if (requestedPcrs.contains(String.valueOf(hostTpmDigestInfo
					.getPcrNumber()))) {
				log.info("Obtain PCR " + hostTpmDigestInfo
					.getPcrNumber());
				String digestValue = byteArrayToHexString(hostTpmDigestInfo
						.getDigestValue());
				pcrMap.put(String.valueOf(hostTpmDigestInfo.getPcrNumber()),
						new PcrModuleManifest(hostTpmDigestInfo.getPcrNumber(),
								digestValue));
			}
		}
	}

	private void getModuleManifest(HostTpmAttestationReport report,
			HashMap<String, PcrModuleManifest> pcrMap) {
		
		for (HostTpmEventLogEntry logEntry : report.getTpmEvents()) {

			if (pcrMap.containsKey(String.valueOf(logEntry.getPcrIndex()))) {
				
				PcrModuleManifest pcrModuleManifest = pcrMap.get(String
						.valueOf(logEntry.getPcrIndex()));

				ModuleManifest moduleManifest = new ModuleManifest();

				if (logEntry.getEventDetails() instanceof HostTpmSoftwareComponentEventDetails) {
					HostTpmSoftwareComponentEventDetails componentEventDetails = (HostTpmSoftwareComponentEventDetails) logEntry
							.getEventDetails();

					moduleManifest
							.setEventName("Vim25Api.HostTpmSoftwareComponentEventDetails");
					moduleManifest.setComponentName("componentName."
							+ componentEventDetails.getComponentName());
					moduleManifest
							.setDigestValue(byteArrayToHexString(componentEventDetails
									.getDataHash()));
					moduleManifest.setPackageName(componentEventDetails
							.getVibName());
					moduleManifest.setPackageVendor(componentEventDetails
							.getVibVendor());
					moduleManifest.setPackageVersion(componentEventDetails
							.getVibVersion());

				} else if (logEntry.getEventDetails() instanceof HostTpmCommandEventDetails) {
					HostTpmCommandEventDetails commandEventDetails = (HostTpmCommandEventDetails) logEntry
							.getEventDetails();

					moduleManifest
							.setEventName("Vim25Api.HostTpmCommandEventDetails");
					moduleManifest.setComponentName("commandLine."
							+ getCommandLine(commandEventDetails));
					moduleManifest
							.setDigestValue(byteArrayToHexString(commandEventDetails
									.getDataHash()));

					// Add to the module manifest map of the pcr
					pcrModuleManifest.getModuleManifests().put(
							moduleManifest.getMFKey(), moduleManifest);

				} else if (logEntry.getEventDetails() instanceof HostTpmOptionEventDetails) {
					HostTpmOptionEventDetails optionEventDetails = (HostTpmOptionEventDetails) logEntry
							.getEventDetails();

					moduleManifest
							.setEventName("Vim25Api.HostTpmOptionEventDetails");
					moduleManifest.setComponentName("bootOptions."
							+ optionEventDetails.getOptionsFileName());
					moduleManifest
							.setDigestValue(byteArrayToHexString(optionEventDetails
									.getDataHash()));
				} else if (logEntry.getEventDetails() instanceof HostTpmBootSecurityOptionEventDetails) {
					HostTpmBootSecurityOptionEventDetails optionEventDetails = (HostTpmBootSecurityOptionEventDetails) logEntry
							.getEventDetails();

					moduleManifest
							.setEventName("Vim25Api.HostTpmBootSecurityOptionEventDetails");
					moduleManifest.setComponentName("bootSecurityOption."
							+ optionEventDetails.getBootSecurityOption());
					moduleManifest
							.setDigestValue(byteArrayToHexString(optionEventDetails
									.getDataHash()));

				} else {
					log.warning("Unrecognized event in module event log "
							+ logEntry.getEventDetails().getClass().getName());
				}
				
				pcrModuleManifest.appendDigest(toByteArray(logEntry.getEventDetails().getDataHash()));

				// Add to the module manifest map of the pcr
				pcrModuleManifest.getModuleManifests().put(
						moduleManifest.getMFKey(), moduleManifest);

			}

		}
	}

	private String getCommandLine(HostTpmCommandEventDetails commandEventDetails) {
		String commandLine = commandEventDetails.getCommandLine();
		if (commandLine != null && commandLine.contains("no-auto-partition")){
			commandLine = "";
		}
		return commandLine;
	}

}
