/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.intel.mtwilson.validation.Fault;
import com.intel.mtwilson.validation.Model;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class TrustReport implements Model {
    private transient ArrayList<Fault> faults = new ArrayList<Fault>();
    
    protected final void fault(Fault fault) {
        faults.add(fault);
    }

    protected final void fault(String description) {
        faults.add(new Fault(description));
    }
    
    protected final void fault(String format, Object... args) {
        faults.add(new Fault(format, args));
    }
    
    protected final void fault(Throwable e, String description) {
        faults.add(new Fault(e, description));
    }
    
    protected final void fault(Throwable e, String format, Object... args) {
        faults.add(new Fault(e, format, args));
    }

    protected final void fault(Model m, String format, Object... args) {
        faults.add(new Fault(m, format, args));
    }
    
    /**
     * @return true if the model is valid (host is trusted) or false if there are faults - which you can access with getFaults()
     */
    @Override
    public final boolean isValid() {
        return faults.isEmpty();
    }
    
    /**
     * 
     * @return a list of faults 
     */
    @Override
    public final List<Fault> getFaults() {
        return faults;
    }    
}
