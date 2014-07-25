/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api.v2;

import com.intel.mtwilson.tls.policy.client.jaxrs.TlsPolicies;//attestation.client.jaxrs.HostTlsPolicy;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyCollection;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class HostTlsPolicyTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostTlsPolicyTest.class);

    private static TlsPolicies client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new TlsPolicies(My.configuration().getClientProperties());
    }
       
    @Test
    public void testRetrieve() {
        com.intel.mtwilson.tls.policy.model.HostTlsPolicy retrieveHostTlsPolicy = client.retrieveTlsPolicy("de07c08a-7fc6-4c07-be08-0ecb2f803681");
        log.debug("Tls policy for host with uuid {} is {}.", retrieveHostTlsPolicy.getId(),retrieveHostTlsPolicy.getName());
        
        HostTlsPolicyFilterCriteria criteria = new HostTlsPolicyFilterCriteria();
        criteria.hostId = "de07c08a-7fc6-4c07-be08-0ecb2f803681";
        HostTlsPolicyCollection searchHostTlsPolicy = client.searchTlsPolicies(criteria);
        log.debug("Tls policy for host with uuid {} is {}.", searchHostTlsPolicy.getTlsPolicies().get(0).getId(),searchHostTlsPolicy.getTlsPolicies().get(0).getName());
    }

    @Test
    public void testEdit() {
        com.intel.mtwilson.tls.policy.model.HostTlsPolicy obj = new com.intel.mtwilson.tls.policy.model.HostTlsPolicy();
        obj.setId(UUID.valueOf("de07c08a-7fc6-4c07-be08-0ecb2f803681"));
        obj.setName("INSECURE");
        com.intel.mtwilson.tls.policy.model.HostTlsPolicy editHostTlsPolicy = client.editTlsPolicy(obj);
        log.debug("Tls policy for host with uuid {} is {}.", editHostTlsPolicy.getId(),editHostTlsPolicy.getName());        
    }

}
