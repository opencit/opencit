/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.HostTlsPolicy;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyCollection;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class HostTlsPolicyTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileTest.class);

    private static HostTlsPolicy client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new HostTlsPolicy(My.configuration().getClientProperties());
    }
       
    @Test
    public void testRetrieve() {
        com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy retrieveHostTlsPolicy = client.retrieveHostTlsPolicy("de07c08a-7fc6-4c07-be08-0ecb2f803681");
        log.debug("Tls policy for host with uuid {} is {}.", retrieveHostTlsPolicy.getHostUuid(),retrieveHostTlsPolicy.getName());
        
        HostTlsPolicyFilterCriteria criteria = new HostTlsPolicyFilterCriteria();
        criteria.hostUuid = UUID.valueOf("de07c08a-7fc6-4c07-be08-0ecb2f803681");
        HostTlsPolicyCollection searchHostTlsPolicy = client.searchHostTlsPolicy(criteria);
        log.debug("Tls policy for host with uuid {} is {}.", searchHostTlsPolicy.getTlsPolicies().get(0).getHostUuid(),searchHostTlsPolicy.getTlsPolicies().get(0).getName());
    }

    @Test
    public void testEdit() {
        com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy obj = new com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy();
        obj.setHostUuid("de07c08a-7fc6-4c07-be08-0ecb2f803681");
        obj.setName("TRUST_FIRST_CERTIFICATE");
        com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy editHostTlsPolicy = client.editHostTlsPolicy(obj);
        log.debug("Tls policy for host with uuid {} is {}.", editHostTlsPolicy.getHostUuid(),editHostTlsPolicy.getName());        
    }

}
