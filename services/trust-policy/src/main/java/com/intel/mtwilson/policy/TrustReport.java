/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final transient Logger log = LoggerFactory.getLogger(getClass().getName());
    private final TrustPolicy policy;
    private final ArrayList<TrustReport> marks = new ArrayList<TrustReport>(); // XXX not sure if it's how i want to do it, but works for now...
    private final ArrayList<Fault> faults = new ArrayList<Fault>();
    
    private TrustReport() { this.policy = null; } // for json deserialization support only
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
     * 
     * Right now does a depth-first recursive search for the named policy. So if it appears more
     * than once in the report, only the first instance found will be returned -- and there is no
     * guarantee of which one that would be.  Therefore this method is only useful for policies that
     * are expected to be found only once in the report, such as "TrustedBios", "TrustedVmm", 
     * "TpmQuoteRequired", etc.  Don't try to use it with "PcrMatchesConstant" there are probably too
     * many of those.
     * 
     * XXX TODO may be we need another function like List<TrustReport> findAllMarks(String policyName)
     * which would return ALL matches... then you could use it to search for "PcrMatchesConstant" and
     * get an output that is almost directly what you need for showing in the Trust Dashboard.
     * 
     * @param policyName the fully-qualified name, such as com.intel.mtwilson.policy.impl.TrustedBios
     * @return the named TrustReport if it was found (must have trusted=true since it's a checkmark), or null if it wasn't found
     */
    public TrustReport findMark(String policyName) {
        log.debug("Looking for mark: {}", policyName);
        log.debug("There are {} marks to search", marks.size());
        for(TrustReport report : marks) {
            log.debug("Looking at report {}", report.getPolicyName());
            if( report.isTrusted() ) {
                log.debug("Report {} is trusted", report.getPolicyName());
                // does this trusted report contain the checkmark we are looking for?
                if( report.getPolicyName().equals(policyName) ) {
                    log.debug("Found mark for {}", policyName);
                    return report;
                }
                // it didn't, so check the trusted report's contents for the checkmark (depth-first recursive search)
                TrustReport subreport = report.findMark(policyName);
                if( subreport != null ) {
                    return subreport;
                }
                // child reports didn't have it either, so we let the loop continue to the next report
            }
        }
        log.debug("Did not find a mark for {}", policyName);
        return null;
    }

    /**
     * Searches through the report for all checkmarks with the given name.  Use it to find
     * all the PcrMatchesConstant checkmarks, for example, which you could then display in a UI
     * like the TrustDashboard
     * 
     * @param policyName
     * @return a list of TrustReport objects... never null, but may be empty if no matching marks were found
     */
    public List<TrustReport> findAllMarks(String policyName) {
        ArrayList<TrustReport> list = new ArrayList<TrustReport>();
        log.debug("Looking for all marks: {}", policyName);
        log.debug("There are {} marks to search", marks.size());
        for(TrustReport report : marks) {
            log.debug("Looking at report {}", report.getPolicyName());
            if( report.isTrusted() ) {
                log.debug("Report {} is trusted", report.getPolicyName());
                // does this trusted report contain the checkmark we are looking for?
                if( report.getPolicyName().equals(policyName) ) {
                    log.debug("Found mark for {}", policyName);
                    list.add(report);
                }
                // it didn't, so check the trusted report's contents for the checkmark (depth-first recursive search)
                List<TrustReport> subreports = report.findAllMarks(policyName);
                list.addAll(subreports);
            }
        }
        log.debug("Found {} marks for {}", list.size(), policyName);
        return list;
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
