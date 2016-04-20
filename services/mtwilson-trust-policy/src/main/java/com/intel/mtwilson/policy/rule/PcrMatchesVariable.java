/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.dynamic.Variable;
import com.intel.mtwilson.policy.fault.PcrManifestMissing;
import com.intel.mtwilson.policy.fault.PcrValueMismatch;
import com.intel.mtwilson.policy.fault.PcrValueMissing;
import java.util.Set;

/**
 * The PcrMatchesVariable policy enforces that a specific PCR contains a dynamic
 * value that is calculated using known variables. For example, some servers
 * may extend a PCR with their UUID -  that means the PCR varies even among
 * otherwise identical hosts, but if we know what the host's UUID is then we
 * are able to calculate what the expected PCR value is and compare to the 
 * actual value.
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PcrMatchesVariable extends BaseRule {
    private PcrIndex pcrIndex;
    private Set<Variable> variables;
    private String expression;
    
    protected PcrMatchesVariable() { } // for desearializing jackson
    
    public PcrMatchesVariable(PcrIndex pcrIndex, Set<Variable> variables, String expression) {
        this.pcrIndex = pcrIndex;
        this.variables = variables;
        this.expression = expression;
    }
    
    @Override
    public RuleResult apply(HostReport hostReport) {
        RuleResult report = new RuleResult(this);
//        report.check(this);
//        report.check("%s: PCR %s is variable", getClass().getSimpleName(),pcrIndex.toString()); 
        if( hostReport.pcrManifest == null ) {
            report.fault(new PcrManifestMissing());            
        }
        else {
            Pcr actual = hostReport.pcrManifest.getPcr(pcrIndex.toInteger());
            if( actual == null ) {
                report.fault(new PcrValueMissing(pcrIndex));
            }
            else {
                /**
                 * 1) create a new database table mw_host_info where we can store host-specific constants UUID when we register a host 
                 * 2) use Rhino javascript interpreter to create javascript binding for certain utility classes (not yet written) so that
                 *    we can write something like     PCR[0] = sha1(Host.Module.BIOS + sha1(Host.UUID))   and have that be part of the 
                 *    whitelist as a computed value;  the BIOS sha1 woud be known and stored in that variable, so that we can extend it
                 *    with the UUID and come up with what the PCR[0] is expected to contain.
                 * 3) calculate expected value for pcrIndex 
                 * 4) compare with actual value
                 * 
                 * Rhino references (esp. important for security against arbitrary javascript):
                 * 
                 * http://www.ibm.com/developerworks/java/library/j-5things9/index.html
                 * http://www.javalobby.org/java/forums/t87890.html
                 * 
                 * https://developer.mozilla.org/en-US/docs/Rhino_documentation
                 * https://developer.mozilla.org/en-US/docs/Scripting_Java
                 * http://en.wikipedia.org/wiki/Rhino_(JavaScript_engine)
                 * https://developer.mozilla.org/en-US/docs/Rhino/JavaScript_Compiler
                 * 
                 * http://riven8192.blogspot.com/2010/07/java-rhino-fine-grained-classshutter.html
                 * http://codeutopia.net/blog/2009/01/02/sandboxing-rhino-in-java/
                 * http://stackoverflow.com/questions/93911/how-can-you-run-javascript-using-rhino-for-java-in-a-sandbox
                 * 
                 * http://calumleslie.blogspot.com/2008/06/simple-jvm-sandboxing.html
                 * https://developer.opencloud.com/devportal/display/RD2v0/1+Configuring+Java+Security+of+Rhino
                 */
/*
                if( !expected.equals(actual) ) {
                    report.fault(new PcrValueMismatch(pcrIndex, expected.getValue(), actual.getValue()) );
                }
*/
            }
        }
        return report;
    }
    
}
