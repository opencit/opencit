/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.policy.HostReport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class PcrEventLogEqualsExcluding extends PcrEventLogEquals {
    private boolean excludeHostSpecificModules = false;
    
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
            // examin m.getInfo()  to decide if it's dynamic,   and also if excludeHostSpecificModules is true then exclude host specific modules
            if( /* dynamic */ false ) {
               modulesExcluding.add(measurement);
            }
        }
        // TODO:    make a new instance of PcrEventLog  with the modulesExcluding  list
        return null; // the new instance 
    }
    
}
