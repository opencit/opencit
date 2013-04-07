/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

/**
 * This class models a sub-policy that RequireAll or RequireAny policies evaluate in a Host Report.
 * 
 * For every policy checked, there should be a Check instance added to the Trust Report.
 * 
 * For policies that are checked and fail, an additional Fault instance should be added to the Trust Report.
 * 
 * So when the report is complete, it should contain a list of Checks showing what policy was checked,
 * and a list of Faults showing which policies failed of the policies that were checked.
 * 
 * It is expected that only the RequireAll and RequireAny policies  will use the Check class,
 * but any policy that act as a container for other policies can use the Check class to report on
 * sub-policies that were checked.
 * 
 * @since 1.1
 * @author jbuhacoff
 */
public class Check {
    private final TrustPolicy policy;
    private final String description; // XXX TODO maybe description is not necessary?
    
    public Check(TrustPolicy policy) {
        this.policy = policy;
        this.description = policy.toString();
    }
    
    
    // XXX TODO maybe delete this constructor because it allows vagueness
    public Check(String description) {
        this.policy = null;
        this.description = description;
    }
    
    public Check(TrustPolicy policy, String format, Object... args) {
        this.policy = policy;
        this.description = String.format(format, args);
    }
        
    /**
     * Checks from the given report are copied to as "more checks" for this one.
     * It is safe to reset or continue using the given model.
     * @param m
     * @param format
     * @param args 
     *//*
    public Check(TrustReport m, String format, Object... args) {
        this.description = String.format(format, args);
        int size = m.getChecks().size();
        this.more = new Check[size];
        for(int i=0; i<size; i++) {
            this.more[i] = m.getChecks().get(i);
        }
    }*/
    
    @Override
    public String toString() {
        return description;
    }
    
    public TrustPolicy getPolicy() { return policy; }
    
    public String getPolicyName() { return policy == null ? null : policy.getClass().getName(); }
    
    // XXX do we need a wrapper  List<Fault> getFaults() { return policy.getFaults(); }    ???   right now the answer is NO, because "Check" models just what we will be checking, and not results... the results are modeled by TrustReport. 
}
