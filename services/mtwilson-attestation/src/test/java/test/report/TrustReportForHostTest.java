/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.report;

import com.intel.mtwilson.My;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.util.Aes128DataCipher;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.datatypes.HostTrustStatus;
import com.intel.mtwilson.policy.Fault;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.TrustReport;
import com.intel.mtwilson.util.ASDataCipher;
import java.io.IOException;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TrustReportForHostTest {
    private transient Logger log = LoggerFactory.getLogger(getClass());
    private transient String hostname = "10.1.70.126";
    
    @BeforeClass
    public static void setMwHostsDek() throws CryptographyException, IOException {
        ASDataCipher.cipher = new Aes128DataCipher(new Aes128(Base64.decodeBase64(My.configuration().getDataEncryptionKeyBase64())));
    }
    
    @Test
    public void testGetTrustReportForHost() throws IOException, CryptographyException {
        TblHosts host = My.jpa().mwHosts().findByName(hostname);
        assertNotNull(host); 
        HostBO hostBO = new HostBO();
        HostTrustBO hostTrustBO = new HostTrustBO();
        hostTrustBO.setHostBO(hostBO);
        TrustReport report = hostTrustBO.getTrustReportForHost(host, hostname);
        List<RuleResult> results = report.getResultsForMarker("VMM");
        for(RuleResult result : results) {
            log.debug("Rule: {}", result.getRuleName());
            List<Fault> faults = result.getFaults();
            log.debug("Faults: {}", faults.size());
            for(Fault fault : faults) {
                log.debug("- Fault: {}: {}", fault.getFaultName(), fault.toString());
            }
        }
    }
    
    @Test
    public void testGetTrustStatusForHost() throws IOException, CryptographyException {
        TblHosts host = My.jpa().mwHosts().findByName(hostname);
        assertNotNull(host); 
        HostBO hostBO = new HostBO();
        HostTrustBO hostTrustBO = new HostTrustBO();
        hostTrustBO.setHostBO(hostBO);
        HostTrustStatus hostTrustStatus = hostTrustBO.getTrustStatus(host, hostname);        
        log.debug("Trusted BIOS? {}", hostTrustStatus.bios);
        log.debug("Trusted VMM? {}", hostTrustStatus.vmm);
        log.debug("Trusted Location? {}", hostTrustStatus.location);
    }
}
