/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.policy;

import com.intel.mtwilson.model.*;
import com.intel.mtwilson.policy.*;
import com.intel.mtwilson.validation.Fault;
import com.intel.mtwilson.validation.Model;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class HostTrustReportTest {
    @Test
    public void testPcrMatchesConstantPolicyPass() {
        Pcr expected = new Pcr(0, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        PcrMatchesConstant policy = new PcrMatchesConstant(expected);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = new PcrManifest();
        hostReport.pcrManifest.setPcr(expected); // set actual = expected so it should pass
        TrustReport report = policy.apply(hostReport);
        assertTrue(report.isValid());
        printFaults(report);
    }
    
    @Test
    public void testPcrMatchesConstantPolicyFail() {
        Pcr expected = new Pcr(0, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        Pcr actual   = new Pcr(0, "aabbccddeeaabbccddeeaabbccddeeaabbccdd00");
        PcrMatchesConstant policy = new PcrMatchesConstant(expected);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = new PcrManifest();
        hostReport.pcrManifest.setPcr(actual); // set actual != expected so it should fail
        TrustReport report = policy.apply(hostReport);
        assertFalse(report.isValid());
        printFaults(report);
    }
    
    private void printFaults(Model m) {
        for(Fault fault : m.getFaults()) {
            System.out.println("Fault: "+fault.toString()+" ["+fault.getClass().getSimpleName()+"]");
            if( fault.getCause() != null ) {
                System.out.println("  Caused by: "+fault.getCause().toString());
            }
            if( fault.getMore() != null ) {
                printFaults(fault.getMore(), 1);
            }
        }        
    }
    private void printFaults(Fault[] faults, int indentLevel) {
        String indent = ""; for(int i=0; i<indentLevel; i++) { indent += "  "; }
        for(Fault fault : faults) {
            System.out.println(indent+"Fault: "+fault.toString()+" ["+fault.getClass().getSimpleName()+"]");
            if( fault.getCause() != null ) {
                System.out.println(indent+"  Caused by: "+fault.getCause().toString());
            }
            if( fault.getMore() != null ) {
                printFaults(fault.getMore(), indentLevel+1);
            }
        }        
    }
}
