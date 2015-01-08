/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.policy.HostReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PcrEventLogEqualsExcluding extends PcrEventLogEquals {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static final List<String> hostSpecificModules = Arrays.asList(new String[] {"commandLine.", "initrd", "vmlinuz"});;
    private boolean excludeHostSpecificModules = false;
    
    protected PcrEventLogEqualsExcluding() { } // for desearializing jackson
    
    public PcrEventLogEqualsExcluding(PcrEventLog expected) {
        super(expected);
    }
    
    public void setExcludeHostSpecificModules(boolean enabled) {
        excludeHostSpecificModules = enabled;
    }
    

    @Override
    protected PcrEventLog getPcrEventLog(HostReport hostReport) {
        List<Measurement> modules = hostReport.pcrManifest.getPcrEventLog(getPcrModuleManifest().getPcrIndex()).getEventLog();
        ArrayList<Measurement> modulesExcluding = new ArrayList<Measurement>();
        Iterator<Measurement> it = modules.iterator();
        while(it.hasNext()) {
            Measurement measurement = it.next();
            log.debug(measurement.getLabel() + " :: " + measurement.getValue().toString() + " :: " + measurement.getInfo().values().toString() + 
                    " :: " + measurement.getInfo().keySet().toString());
            // examin m.getInfo()  to decide if it's dynamic,   and also if excludeHostSpecificModules is true then exclude host specific modules
            if (excludeHostSpecificModules &&  hostSpecificModules.contains(measurement.getInfo().get("ComponentName")))
                continue;
            // let us skip even the dynamic modules
            if( measurement.getInfo().get("PackageName") != null && measurement.getInfo().get("PackageName").equalsIgnoreCase("") && 
                    measurement.getInfo().get("PackageVendor") != null && measurement.getInfo().get("PackageVendor").equalsIgnoreCase("")) {
                log.debug("Skipping the module since it is dynamic.");
                continue;
            }
            // Add the module to be verified.
            modulesExcluding.add(measurement);
        }
        PcrEventLog updatedPcrEventLog = new PcrEventLog(PcrIndex.PCR19, modulesExcluding);
        return updatedPcrEventLog; // the new instance 
    }
    
}
