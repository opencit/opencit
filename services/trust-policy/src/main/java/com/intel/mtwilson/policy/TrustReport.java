/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import java.util.ArrayList;
import java.util.List;

/**
 * Very similar to "ObjectModel" but purposefully does NOT implement Model because the faults 
 * recorded here are not problems with THIS instance, they are simply records of policy evaluations
 * on some other instance.   This class is similar to com.intel.mtwilson.validation.Report but
 * instead of reporting faults on another model, faults are directly recorded here.
 * 
 * So if someone turns on automatic Model checking (via aspectj) we do not want the application
 * to throw an exception when a report is passed to some method just because the host represented
 * in the report didn't meet the policy. The report itself IS valid.
 * 
 * For that reason, also renamed "isValid" to "isTrusted", to make it clear it's not a "Model"
 * implementation.
 * 
 * TODO:  add mirror methods for "ok"  so policies can record POSITIVE things when they pass, 
 * so that observers can see which policies WERE CHECKED in addition to which failed.
 * 
 * @author jbuhacoff
 */
public class TrustReport {
    private final TrustPolicy policy;
    private final ArrayList<TrustReport> marks = new ArrayList<TrustReport>(); // XXX not sure if it's how i want to do it, but works for now...
    private final ArrayList<Fault> faults = new ArrayList<Fault>();
    
    public TrustReport(TrustPolicy policy) {
        this.policy = policy;
    }
    
    public final String getPolicyName() { return policy.getClass().getName(); }
    public final TrustPolicy getPolicy() { return policy; }
    
    protected final void ok(TrustReport report) {
        marks.add(report);
    }
    
    public final List<TrustReport> getMarks() { return marks; }
    
    
    /**
     * XXX may need to change the mechanism later... maybe pass a TrustPolicy object, or a Class object  like findMark(TrustedBios.class) 
     * @param policyName the fully-qualified name, such as com.intel.mtwilson.policy.impl.TrustedBios
     * @return 
     */
    public TrustReport findMark(String policyName) { 
        for(TrustReport report : marks) {
            if( report.isTrusted() ) {
                if( report.getPolicyName().equals(policyName) ) {
                    return report;
                }
            }
        }
        return null;
    }

    protected final void fault(Fault fault) {
        faults.add(fault);
    }

    // TODO: need a mirror "ok" for this
    protected final void fault(String description) {
        faults.add(new Fault(description));
    }
    
    // TODO: need a mirror "ok" for this
    protected final void fault(String format, Object... args) {
        faults.add(new Fault(format, args));
    }
    
    protected final void fault(Throwable e, String description) {
        faults.add(new Fault(e, description));
    }
    
    protected final void fault(Throwable e, String format, Object... args) {
        faults.add(new Fault(e, format, args));
    }
    
    /**
     * 
     * @return a list of faults 
     */
    public final List<Fault> getFaults() {
        return faults;
    }    


    /**
     * @return true if the host meets the policy  (host is trusted) or false if there are faults - which you can access with getFaults()
     */
    public final boolean isTrusted() {
        return faults.isEmpty();
    }

}
