/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.PollHosts;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class PollHostsTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PollHostsTest.class);

    private static PollHosts client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new PollHosts(My.configuration().getClientProperties());
    }
    
    @Test
    public void testRetrieve() throws Exception {
        OpenStackHostTrustLevelQuery input = new OpenStackHostTrustLevelQuery();
        input.setHosts(new String[] {"10.1.71.155"});
        OpenStackHostTrustLevelReport openStackHostTrustReport = client.getOpenStackHostTrustReport(input);
        log.debug("Open Stack host trust report for {} is {}", openStackHostTrustReport.pollHosts.get(0).hostname, openStackHostTrustReport.pollHosts.get(0).trustLevel);
    }
    
}
