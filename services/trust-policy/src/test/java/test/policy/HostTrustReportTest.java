/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.policy;

import com.intel.mtwilson.model.*;
import com.intel.mtwilson.policy.*;
import com.intel.mtwilson.policy.fault.PcrModuleManifestMissingExpectedEntries;
import com.intel.mtwilson.validation.Fault;
import com.intel.mtwilson.validation.Model;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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
    
    @Test
    public void testPcrModuleManifestFailWithDetails() {
        HashSet<Measurement> expectedModuleSet = new HashSet<Measurement>();
        expectedModuleSet.add(new Measurement(new Sha1Digest("0011001100110011001100220022002200220022"), "vendorA-moduleXYZ-1.0.2"));
        expectedModuleSet.add(new Measurement(new Sha1Digest("0011001100110011001100330033003300330033"), "vendorA-moduleXYZ-1.0.3"));
        PcrModuleManifest expected = new PcrModuleManifest(new PcrIndex(8), expectedModuleSet);
        PcrModuleManifestIncludesModuleSet policy = new PcrModuleManifestIncludesModuleSet(expected);
        HashSet<Measurement> actualModuleSet = new HashSet<Measurement>();
        actualModuleSet.add(new Measurement(new Sha1Digest("0011001100110011001100220022002200220022"), "vendorA-moduleXYZ-1.0.2"));
        actualModuleSet.add(new Measurement(new Sha1Digest("1012134056708910234580990553434570343245"), "vendorB-moduleABC-0.5.0"));
        PcrModuleManifest actual = new PcrModuleManifest(new PcrIndex(8), actualModuleSet);
        HostReport hostReport = new HostReport();
        hostReport.pcrModuleManifest = new HashMap<PcrIndex,Set<Measurement>>();
        hostReport.pcrModuleManifest.put(new PcrIndex(8), actualModuleSet); // TODO maybe we need to make an object to which we can add PcrModuleManifest object ? the "actual" object above is not used because we are adding directly to the map
        TrustReport report = policy.apply(hostReport);
        assertFalse(report.isValid());
        printFaults(report);
        // look for the list of missing modules and print it
        for(Fault fault : report.getFaults()) {
            if( fault instanceof PcrModuleManifestMissingExpectedEntries ) {
                PcrModuleManifestMissingExpectedEntries details = (PcrModuleManifestMissingExpectedEntries)fault;
                System.out.println(String.format("Modules missing from PCR %d:", details.getPcrIndex().toInteger()));
                for(Measurement m : details.getMissingEntries()) {
                    System.out.println(String.format("- %s %s", m.getValue().toString(), m.getLabel()));
                }
            }
        }
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
