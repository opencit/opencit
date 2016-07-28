/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.policy;

import com.intel.mtwilson.policy.rule.*;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.policy.*;
import com.intel.mtwilson.policy.fault.*;
import com.intel.mtwilson.policy.Fault;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectWriter;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class HostTrustReportTest {
    
    @Test
    public void computeHistory() {

        Sha1Digest result1 = Sha1Digest.ZERO;
        
        result1 = result1.extend(Sha1Digest.valueOf("d2f867d36c99b9c8e9b0de45a73351b06628aeb7"));
        System.out.println(result1.toString());
        result1 = result1.extend(Sha1Digest.valueOf("e209744ec7e2cd40aed641f5172d6c0afa3619e7"));
        
        System.out.println(result1.toString());
        
    }
    
    /**
     * Output:
Check: PcrMatchesConstant: PCR 0, aabbccddeeaabbccddeeaabbccddeeaabbccddee
     * Json output: 
{
  "policy" : {
    "expectedPcr" : {
      "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
      "index" : "0"
    }
  },
  "faults" : [ ],
  "trusted" : true,
  "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
}
     * 
     */
    @Test
    public void testPcrMatchesConstantPolicyPass() {
        PcrSha1 expected = new PcrSha1(0, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        PcrMatchesConstant policy = new PcrMatchesConstant(expected);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = new PcrManifest();
        hostReport.pcrManifest.setPcr(expected); // set actual = expected so it should pass
        RuleResult report = policy.apply(hostReport);
        assertTrue(report.isTrusted());
//        printFaults(report);
        printReport(report);
        printReportJson(report);
    }
    
    /**
     * Output:
Check: PcrMatchesConstant: PCR 0, aabbccddeeaabbccddeeaabbccddeeaabbccddee
Fault: Host PCR 0 with value aabbccddeeaabbccddeeaabbccddeeaabbccdd00 does not match expected value aabbccddeeaabbccddeeaabbccddeeaabbccddee [PcrValueMismatch]
     * 
     * Json output:
{
  "policy" : {
    "expectedPcr" : {
      "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
      "index" : "0"
    }
  },
  "faults" : [ {
    "cause" : null,
    "more" : [ ],
    "pcrIndex" : "0",
    "expectedValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
    "actualValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccdd00",
    "faultName" : "com.intel.mtwilson.policy.fault.PcrValueMismatch"
  } ],
  "trusted" : false,
  "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
}
     * 
     */
    @Test
    public void testPcrMatchesConstantPolicyFail() {
        Pcr expected = new PcrSha1(0, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        Pcr actual   = new PcrSha1(0, "aabbccddeeaabbccddeeaabbccddeeaabbccdd00");
        PcrMatchesConstant policy = new PcrMatchesConstant(expected);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = new PcrManifest();
        hostReport.pcrManifest.setPcr(actual); // set actual != expected so it should fail
        RuleResult report = policy.apply(hostReport);
        assertFalse(report.isTrusted());
//        printFaults(report);
        printReport(report);
        printReportJson(report);
    }
    
    
    /**
     * Example output:
{
  "policy" : {
    "checks" : [ {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "1"
        }
      },
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    }, {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "2"
        }
      },
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    } ]
  },
  "faults" : [ ],
  "trusted" : true,
  "policyName" : "com.intel.mtwilson.policy.RequireAll"
}
     * 
     */
    @Test
    public void testPcrMatchesConstantPolicyListPass() {
        Pcr expected1 = new PcrSha1(1, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        PcrMatchesConstant policy1 = new PcrMatchesConstant(expected1);
        Pcr expected2 = new PcrSha1(2, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        PcrMatchesConstant policy2 = new PcrMatchesConstant(expected2);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = new PcrManifest();
        hostReport.pcrManifest.setPcr(expected1); // set actual = expected so it should pass
        hostReport.pcrManifest.setPcr(expected2); // set actual = expected so it should pass
//        ArrayList<TrustPolicy> policies = new ArrayList<TrustPolicy>();
//        policies.add(policy1);
//        policies.add(policy2);
        PolicyEngine engine = new PolicyEngine();
//        List<TrustReport> report = engine.apply(hostReport, policies);
        List<RuleResult> reports = engine.applyAll(hostReport, policy1, policy2);
        for(RuleResult report : reports) {
            printReport(report);
            printReportJson(report);
        }
        
        
        /**
         * Sample output for below:
{
  "policyName" : "test pcr matches constant",
  "reports" : [ {
    "rule" : {
      "markers" : [ "bios" ],
      "expectedPcr" : {
        "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
        "index" : "0"
      }
    },
    "faults" : [ ],
    "trusted" : true,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  }, {
    "rule" : {
      "markers" : [ "vmm" ],
      "expectedPcr" : {
        "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
        "index" : "17"
      }
    },
    "faults" : [ ],
    "trusted" : true,
    "ruleName" : "com.intel.mtwilson.policy.rule.PcrMatchesConstant"
  } ],
  "trusted" : true
}
         * 
         */
        // now do the same thing but with a policy
        Policy policy = new Policy("test pcr matches constant", policy1, policy2);
        TrustReport report = engine.apply(hostReport, policy);
        printReportJson(report);        
    }
    
    /**
     * Example output:
     * 
{
  "policy" : {
    "checks" : [ {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "1"
        }
      },
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    }, {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "2"
        }
      },
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    } ]
  },
  "marks" : [ {
    "policy" : {
      "expectedPcr" : {
        "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
        "index" : "2"
      }
    },
    "marks" : [ ],
    "faults" : [ ],
    "trusted" : true,
    "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
  } ],
  "faults" : [ {
    "cause" : null,
    "report" : {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "1"
        }
      },
      "marks" : [ ],
      "faults" : [ {
        "cause" : null,
        "pcrIndex" : "1",
        "expectedValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
        "actualValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccdd11",
        "faultName" : "com.intel.mtwilson.policy.fault.PcrValueMismatch"
      } ],
      "trusted" : false,
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    },
    "faultName" : "com.intel.mtwilson.policy.fault.Cite"
  } ],
  "trusted" : false,
  "policyName" : "com.intel.mtwilson.policy.RequireAll"
}
     * 
     */
    @Test
    public void testPcrMatchesConstantPolicyListFailRequireAll() {
        Pcr expected1 = new PcrSha1(1, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        Pcr actual1 = new PcrSha1(1, "aabbccddeeaabbccddeeaabbccddeeaabbccdd11");
        PcrMatchesConstant policy1 = new PcrMatchesConstant(expected1);
        Pcr expected2 = new PcrSha1(2, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        Pcr actual2 = expected2;// new Pcr(2, "aabbccddeeaabbccddeeaabbccddeeaabbccdd22");
        PcrMatchesConstant policy2 = new PcrMatchesConstant(expected2);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = new PcrManifest();
        hostReport.pcrManifest.setPcr(actual1);
        hostReport.pcrManifest.setPcr(actual2); 
//        ArrayList<TrustPolicy> policies = new ArrayList<TrustPolicy>();
//        policies.add(policy1);
//        policies.add(policy2);
        PolicyEngine engine = new PolicyEngine();
//        List<TrustReport> report = engine.apply(hostReport, policies);
        List<RuleResult> reports = engine.applyAll(hostReport, policy1, policy2);
        for(RuleResult report : reports) {
            printReport(report);
            printReportJson(report);
        }
    }
    
    
    /**
     * 
     * Example output,  notice that because all policies failed the output is just like any other failure report.
     * 
{
  "policy" : {
    "checks" : [ {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "1"
        }
      },
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    }, {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "2"
        }
      },
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    } ]
  },
  "faults" : [ {
    "cause" : null,
    "report" : {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "1"
        }
      },
      "faults" : [ {
        "cause" : null,
        "pcrIndex" : "1",
        "expectedValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
        "actualValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccdd11",
        "faultName" : "com.intel.mtwilson.policy.fault.PcrValueMismatch"
      } ],
      "trusted" : false,
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    },
    "faultName" : "com.intel.mtwilson.policy.fault.Cite"
  }, {
    "cause" : null,
    "report" : {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "2"
        }
      },
      "faults" : [ {
        "cause" : null,
        "pcrIndex" : "2",
        "expectedValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
        "actualValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccdd22",
        "faultName" : "com.intel.mtwilson.policy.fault.PcrValueMismatch"
      } ],
      "trusted" : false,
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    },
    "faultName" : "com.intel.mtwilson.policy.fault.Cite"
  } ],
  "trusted" : false,
  "policyName" : "com.intel.mtwilson.policy.RequireAny"
}
     * 
     * 
     */
    @Test
    public void testPcrMatchesConstantPolicyListFailRequireAny() {
        Pcr expected1 = new PcrSha1(1, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        Pcr actual1 = new PcrSha1(1, "aabbccddeeaabbccddeeaabbccddeeaabbccdd11");
        PcrMatchesConstant policy1 = new PcrMatchesConstant(expected1);
        Pcr expected2 = new PcrSha1(2, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        Pcr actual2 = new PcrSha1(2, "aabbccddeeaabbccddeeaabbccddeeaabbccdd22");
        PcrMatchesConstant policy2 = new PcrMatchesConstant(expected2);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = new PcrManifest();
        hostReport.pcrManifest.setPcr(actual1);
        hostReport.pcrManifest.setPcr(actual2); 
//        ArrayList<TrustPolicy> policies = new ArrayList<TrustPolicy>();
//        policies.add(policy1);
//        policies.add(policy2);
        PolicyEngine engine = new PolicyEngine();
//        List<TrustReport> report = engine.apply(hostReport, policies);
        List<RuleResult> reports = engine.applyAll(hostReport, policy1, policy2);
        for(RuleResult report : reports) {
            printReport(report);
            printReportJson(report);
        }
    }
    

    /**
     * Example output, notice that because ONE policy succeeded,  the overall result is trusted and
     * there are no faults listed... in order to be fully compatible with expectations for the TrustReport
     * object. However, for the policies that failed (and didn't matter since ONE did pass) the faults
     * are still recorded,  in the "optionalFaults"  field. 
{
  "policy" : {
    "checks" : [ {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "1"
        }
      },
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    }, {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "2"
        }
      },
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    } ]
  },
  "faults" : [ ],
  "optionalFaults" : [ {
    "cause" : null,
    "report" : {
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "1"
        }
      },
      "faults" : [ {
        "cause" : null,
        "pcrIndex" : "1",
        "expectedValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
        "actualValue" : "aabbccddeeaabbccddeeaabbccddeeaabbccdd11",
        "faultName" : "com.intel.mtwilson.policy.fault.PcrValueMismatch"
      } ],
      "trusted" : false,
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
    },
    "faultName" : "com.intel.mtwilson.policy.fault.Cite"
  } ],
  "trusted" : true,
  "policyName" : "com.intel.mtwilson.policy.RequireAny"
}
     * 
     */
    @Test
    public void testPcrMatchesConstantPolicyListPassRequireAny() {
        Pcr expected1 = new PcrSha1(1, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        Pcr actual1 = new PcrSha1(1, "aabbccddeeaabbccddeeaabbccddeeaabbccdd11");
        PcrMatchesConstant policy1 = new PcrMatchesConstant(expected1);
        Pcr expected2 = new PcrSha1(2, "aabbccddeeaabbccddeeaabbccddeeaabbccddee");
        Pcr actual2 = expected2;
        PcrMatchesConstant policy2 = new PcrMatchesConstant(expected2);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = new PcrManifest();
        hostReport.pcrManifest.setPcr(actual1);
        hostReport.pcrManifest.setPcr(actual2); 
//        ArrayList<TrustPolicy> policies = new ArrayList<TrustPolicy>();
//        policies.add(policy1);
//        policies.add(policy2);
        PolicyEngine engine = new PolicyEngine();
//        List<TrustReport> report = engine.apply(hostReport, policies);
        List<RuleResult> reports = engine.applyAll(hostReport, policy1, policy2);
        for(RuleResult report : reports) {
            printReport(report);
            printReportJson(report);
        }
    }

    
    
    /**
     * Example output using custom prints:
     * 
Check: PcrModuleManifestIncludesModuleSet: com.intel.mtwilson.policy.PcrModuleManifestIncludesModuleSet@5a07232e
Fault: Module manifest for PCR 8 missing 1 expected entries [PcrModuleManifestMissingExpectedEntries]
Modules missing from PCR 8:
- 0011001100110011001100330033003300330033 vendorA-moduleXYZ-1.0.3
     * 
     * Example output using json:
{
  "policy" : {
    "pcrModuleManifest" : {
      "pcrIndex" : "8",
      "moduleManifest" : [ {
        "label" : "vendorA-moduleXYZ-1.0.2",
        "value" : "0011001100110011001100220022002200220022"
      }, {
        "label" : "vendorA-moduleXYZ-1.0.3",
        "value" : "0011001100110011001100330033003300330033"
      } ]
    }
  },
  "faults" : [ {
    "cause" : null,
    "pcrIndex" : "8",
    "missingEntries" : [ {
      "label" : "vendorA-moduleXYZ-1.0.3",
      "value" : "0011001100110011001100330033003300330033"
    } ],
    "faultName" : "com.intel.mtwilson.policy.fault.PcrModuleManifestMissingExpectedEntries"
  } ],
  "trusted" : false,
  "policyName" : "com.intel.mtwilson.policy.PcrModuleManifestIncludesModuleSet"
}
     *
     * 
     */
    @Test
    public void testPcrModuleManifestFailWithDetails() {
        HashSet<Measurement> expectedModuleSet = new HashSet<Measurement>();
        expectedModuleSet.add(new Measurement(new Sha1Digest("0011001100110011001100220022002200220022"), "vendorA-moduleXYZ-1.0.2"));
        expectedModuleSet.add(new Measurement(new Sha1Digest("0011001100110011001100330033003300330033"), "vendorA-moduleXYZ-1.0.3"));
        PcrEventLogIncludes policy = new PcrEventLogIncludes(new PcrIndex(8), expectedModuleSet);
        ArrayList<Measurement> actualModuleSet = new ArrayList<Measurement>();
        actualModuleSet.add(new Measurement(new Sha1Digest("0011001100110011001100220022002200220022"), "vendorA-moduleXYZ-1.0.2"));
        actualModuleSet.add(new Measurement(new Sha1Digest("1012134056708910234580990553434570343245"), "vendorB-moduleABC-0.5.0"));
        PcrEventLog actual = new PcrEventLog(new PcrIndex(8), actualModuleSet);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = new PcrManifest();
        hostReport.pcrManifest.setPcrEventLog(new PcrEventLog(new PcrIndex(8),actualModuleSet));
        RuleResult report = policy.apply(hostReport);
        assertFalse(report.isTrusted());
//        printFaults(report);
        printReport(report);
        // look for the list of missing modules and print it
        for(Fault fault : report.getFaults()) {
            if( fault instanceof PcrEventLogMissingExpectedEntries ) {
                PcrEventLogMissingExpectedEntries details = (PcrEventLogMissingExpectedEntries)fault;
                System.out.println(String.format("Modules missing from PCR %d:", details.getPcrIndex().toInteger()));
                for(Measurement m : details.getMissingEntries()) {
                    System.out.println(String.format("- %s %s", m.getValue().toString(), m.getLabel()));
                }
            }
        }
        
        printReportJson(report);
    }
    
    
    private static ObjectWriter json = new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter();
    /*
    private static ObjectWriter writer;
    @BeforeClass
    public void initJsonMapper () {
        writer = mapper.writerWithDefaultPrettyPrinter();
    }
    public void printJson(Object value) {
        try {
            System.out.println(mapper.writeValueAsString(value));
        }
        catch(Exception e) {
            System.out.println("Cannot print value: "+e.toString());
        }
    }*/
    
    private void printReport(List<RuleResult> list) {
        for(RuleResult report : list) {
            printReport(report);
        }
    }
    private void printReport(RuleResult report) {
        System.out.println("Check: "+report.getRule().getClass().getSimpleName()+": "+report.getRule().toString());
        printFaults(report);
    }
    
    private void printReportJson(TrustReport report) {
        try {
            System.out.println(json.writeValueAsString(report));
        }
        catch(Exception e) {
            System.out.println("Cannot write report: "+e.toString());
        }
    }
    
    private void printReportJson(RuleResult report) {
        try {
            System.out.println(json.writeValueAsString(report));
        }
        catch(Exception e) {
            System.out.println("Cannot write report: "+e.toString());
        }
        /*
        System.out.println("Check: "+report.getPolicy().getClass().getSimpleName()+": "+report.getPolicy().toString());
        try {
            System.out.println(mapper.writeValueAsString(report.getPolicy()));
        }
        catch(Exception e) {
            System.out.println("Cannot describe policy: "+e.toString());
        }
        for(Fault fault : report.getFaults()) {
            try {
                System.out.println(mapper.writeValueAsString(fault));
            }
            catch(Exception e) {
                System.out.println("Cannot describe fault: "+e.toString());
            }
        }*/
    }
    
    private void printFaults(RuleResult m) {
        for(Fault fault : m.getFaults()) {
            System.out.println("Fault: "+fault.toString()+" ["+fault.getClass().getSimpleName()+"]");
            if( fault.getCause() != null ) {
                System.out.println("  Caused by: "+fault.getCause().toString());
            }
        }        
    }
    /*
    private void printFaults(Fault[] faults, int indentLevel) {
        String indent = ""; for(int i=0; i<indentLevel; i++) { indent += "  "; }
        for(Fault fault : faults) {
            System.out.println(indent+"Fault: "+fault.toString()+" ["+fault.getClass().getSimpleName()+"]");
            if( fault.getCause() != null ) {
                System.out.println(indent+"  Caused by: "+fault.getCause().toString());
            }
        }        
    }*/
    /*
    private void printChecks(TrustReport m) {
        for(Check check : m.getChecks()) {
            System.out.println("Check: "+check.toString()+" ["+check.getClass().getSimpleName()+"]");
//            if( check.getMore() != null ) {
//                printChecks(check.getMore(), 1);
//            }
            printFaults(m);
        }        
    }
    private void printChecks(Check[] checks, int indentLevel) {
        String indent = ""; for(int i=0; i<indentLevel; i++) { indent += "  "; }
        for(Check check : checks) {
            System.out.println(indent+"Check: "+check.toString()+" ["+check.getClass().getSimpleName()+"]");
            if( check.getMore() != null ) {
                printChecks(check.getMore(), indentLevel+1);
            }
        }        
    }*/
    
    
    // XXX this doesn't work right now.  it's impossible to deserialize the json or the xml unless
    // we include type information when we serialize... like the "policyName" but generated by jackson
    // for every object.
    @Test
    public void testReadJsonTrustReportAndFindMarks() throws IOException {
        InputStream in = getClass().getResourceAsStream("/trustreport-1.xml");
        com.fasterxml.jackson.databind.ObjectMapper xml = new XmlMapper();
        xml.registerModule(new MrBeanModule());
        RuleResult report = xml.reader(RuleResult.class).readValue(in);
        IOUtils.closeQuietly(in);
        /*
        TrustReport biosReport = report.findMark("com.intel.mtwilson.policy.impl.TrustedBios");
        assertNotNull(biosReport);
        assertTrue(biosReport.isTrusted()); 
        TrustReport vmmReport = report.findMark("com.intel.mtwilson.policy.impl.TrustedVmm");
        assertNotNull(vmmReport);
        assertTrue(vmmReport.isTrusted());
        TrustReport locationReport = report.findMark("com.intel.mtwilson.policy.impl.TrustedLocation");
        assertNull(locationReport); // the report did not include a location policy
        */
    }
}
