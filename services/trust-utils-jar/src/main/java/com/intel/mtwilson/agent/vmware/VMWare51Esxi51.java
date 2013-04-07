package com.intel.mtwilson.agent.vmware;

import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.model.Sha1Digest;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmBootSecurityOptionEventDetails;
import com.vmware.vim25.HostTpmCommandEventDetails;
import com.vmware.vim25.HostTpmDigestInfo;
import com.vmware.vim25.HostTpmEventDetails;
import com.vmware.vim25.HostTpmEventLogEntry;
import com.vmware.vim25.HostTpmOptionEventDetails;
import com.vmware.vim25.HostTpmSoftwareComponentEventDetails;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VMWare51Esxi51   {
    private static Logger log = LoggerFactory.getLogger(VMWare51Esxi51.class);
    
    public static PcrManifest createPcrManifest(HostTpmAttestationReport report) {
        PcrManifest pcrManifest = new PcrManifest();
        
        // for each PCR get index and value
        for (HostTpmDigestInfo hostTpmDigestInfo : report.getTpmPcrValues()) {
            // this block just out of curiosity ... we should instantiate the right digest class using this info. expected to be SHA1... always...
            String algorithm = hostTpmDigestInfo.getDigestMethod();
            log.debug("HostTpmDigestInfo DigestMethod = {}", algorithm);
            // convert the vwmare data types to mt wilson datatypes
            String digest = VMwareClient.byteArrayToHexString(hostTpmDigestInfo.getDigestValue());
            Pcr pcr = new Pcr(hostTpmDigestInfo.getPcrNumber(), digest);
            pcrManifest.setPcr(pcr);
        }
        
        // for each event assign it to a PCR event log
        for(HostTpmEventLogEntry logEntry : report.getTpmEvents()) {
            int pcrIndex = logEntry.getPcrIndex();
            Measurement m = convertHostTpmEventLogEntryToMeasurement(logEntry);
            if( pcrManifest.containsPcrEventLog(new PcrIndex(pcrIndex)) ) {
                pcrManifest.getPcrEventLog(pcrIndex).getEventLog().add(m);
            }
            else {
                ArrayList<Measurement> list = new ArrayList<Measurement>();
                list.add(m);
                pcrManifest.setPcrEventLog(new PcrEventLog(new PcrIndex(pcrIndex),list));
            }
        }
        
        return pcrManifest;

	}
    
    private static Measurement convertHostTpmEventLogEntryToMeasurement(HostTpmEventLogEntry logEntry) {
        String label;
        String digest = VMwareClient.byteArrayToHexString(logEntry.getEventDetails().getDataHash());;
        HashMap<String,String> info = new HashMap<String,String>();
        
        HostTpmEventDetails logEventDetails = logEntry.getEventDetails();
				if (logEventDetails instanceof HostTpmSoftwareComponentEventDetails) { // ComponentName, VibName, VibVersion, VibVendor, and DataHash
					HostTpmSoftwareComponentEventDetails componentEventDetails = (HostTpmSoftwareComponentEventDetails)logEventDetails;
                    
					info.put("EventType", "HostTpmSoftwareComponentEvent"); // new, properly capture the type of event in a separate field
					info.put("EventName", "Vim25Api.HostTpmSoftwareComponentEventDetails"); // XXX TODO the name should be the component name with package name and vendor-version... not the vmware event type, it's not unique
					info.put("ComponentName", "componentName." + componentEventDetails.getComponentName()); // XXX TODO remove the "componentName." prefix because we are capturing this now in EventType
					info.put("PackageName", componentEventDetails.getVibName());
					info.put("PackageVendor", componentEventDetails.getVibVendor());
					info.put("PackageVersion", componentEventDetails.getVibVersion());
                    label = String.format("%s: %s-%s-%s", info.get("EventType"), componentEventDetails.getVibVendor(), componentEventDetails.getVibName(), componentEventDetails.getVibVersion() );
				} else if (logEventDetails instanceof HostTpmCommandEventDetails) { // CommandLine and DataHash
					HostTpmCommandEventDetails commandEventDetails = (HostTpmCommandEventDetails) logEventDetails;

					info.put("EventType", "HostTpmCommandEvent"); // new, properly capture the type of event in a separate field
					info.put("EventName", "Vim25Api.HostTpmCommandEventDetails"); // XXX TODO the name should be the component name with package name and vendor-version... not the vmware event type, it's not unique
					info.put("ComponentName", "commandLine."+ getCommandLine(commandEventDetails)); // XXX TODO remove the "commandLine." prefix because we are capturing this now in EventType
                    label = String.format("%s: %s", info.get("EventType"), getCommandLine(commandEventDetails) );

				} else if (logEventDetails instanceof HostTpmOptionEventDetails) { // OptionsFilename, BootOptions, and DataHash
					HostTpmOptionEventDetails optionEventDetails = (HostTpmOptionEventDetails)logEventDetails;

					info.put("EventType", "HostTpmOptionEvent"); // new, properly capture the type of event in a separate field
					info.put("EventName", "Vim25Api.HostTpmOptionEventDetails"); // XXX TODO the name should be the component name with package name and vendor-version... not the vmware event type, it's not unique
					info.put("ComponentName", "bootOptions."+ optionEventDetails.getOptionsFileName()); // XXX TODO remove the "bootOptions." prefix because we are capturing this now in EventType
                    // XXX TODO we can get the actual options with   info.put("BootOptions", VMwareClient.byteArrayToHexString(optionEventDetails.getBootOptions()); ... right now we are only capture the file name and not its contents;  probably ok since what the policy checks is the DIGEST anyway
                    label = String.format("%s: %s", info.get("EventType"), optionEventDetails.getOptionsFileName() );
				} else if (logEventDetails instanceof HostTpmBootSecurityOptionEventDetails) { // BootSecurityOption and DataHash
					HostTpmBootSecurityOptionEventDetails optionEventDetails = (HostTpmBootSecurityOptionEventDetails)logEventDetails;
					info.put("EventType", "HostTpmBootSecurityOptionEvent"); // new, properly capture the type of event in a separate field
					info.put("EventName", "Vim25Api.HostTpmBootSecurityOptionEventDetails"); // XXX TODO the name should be the component name with package name and vendor-version... not the vmware event type, it's not unique
					info.put("ComponentName", "bootSecurityOption."+ optionEventDetails.getBootSecurityOption()); // XXX TODO remove the "bootSecurityOption." prefix because we are capturing this now in EventType
                    label = String.format("%s: %s", info.get("EventType"), optionEventDetails.getBootSecurityOption() );

				} else {
					log.warn("Unrecognized event in module event log "
							+ logEventDetails.getClass().getName());
                    
                    List<DynamicProperty> ps = logEventDetails.getDynamicProperty();
                    for(DynamicProperty p : ps) {
                        info.put(p.getName(), p.getVal().toString());
                    }
                    label = String.format("%s: %s", logEventDetails.getClass().getSimpleName(), logEventDetails.getDynamicType());
				}
                
                return new Measurement(new Sha1Digest(digest), label, info );
                
    }

	private static String getCommandLine(HostTpmCommandEventDetails commandEventDetails) {
		String commandLine = commandEventDetails.getCommandLine();
		if (commandLine != null && commandLine.contains("no-auto-partition")){
			commandLine = "";
		}
		return commandLine;
	}    
    
    

}
