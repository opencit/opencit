/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tls.policy.client.jaxrs;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicy;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyCollection;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.RoleFilterCriteria;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class TlsPolicyTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyTest.class);

    private static TlsPolicies client = null;
    
    @BeforeClass
    public static void init() throws Exception {
//        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.CertificateTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTrustFirstCertificateTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.PublicKeyDigestTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.PublicKeyTlsPolicyCreator.class);
        client = new TlsPolicies(My.configuration().getClientProperties());
    }
    
    @Test
    public void testTlsPolicy() {
        UUID id = new UUID();
        HostTlsPolicy tlsPolicy = new HostTlsPolicy();
        tlsPolicy.setId(id);
        tlsPolicy.setName("New_shared_policy");
        tlsPolicy.setPrivate(false);
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("certificate-digest");
        tlsPolicyDescriptor.setData(Arrays.asList("d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"));
        Map<String, String> metaData = new HashMap<>();
        metaData.put("digest_algorithm","SHA-1");
        tlsPolicyDescriptor.setMeta(metaData);
        tlsPolicy.setDescriptor(tlsPolicyDescriptor);
        HostTlsPolicy createTlsPolicy = client.createTlsPolicy(tlsPolicy);
        log.debug("Created the new tls policy with id {}.", createTlsPolicy.getId());
        
        createTlsPolicy.setComment("Updated with comments");
        HostTlsPolicy editTlsPolicy = client.editTlsPolicy(createTlsPolicy);
        HostTlsPolicy retrieveTlsPolicy = client.retrieveTlsPolicy(id.toString());
        HostTlsPolicyFilterCriteria criteria = new HostTlsPolicyFilterCriteria();
        criteria.privateEqualTo = false;
        HostTlsPolicyCollection searchTlsPolicies = client.searchTlsPolicies(criteria);
        for (HostTlsPolicy obj : searchTlsPolicies.getTlsPolicies()) {
            log.debug("Tls policy is {}.", obj.getName());
        }
        
        client.deleteTlsPolicy(criteria);
        searchTlsPolicies = client.searchTlsPolicies(criteria);
        for (HostTlsPolicy obj : searchTlsPolicies.getTlsPolicies()) {
            log.debug("Tls policy is {}.", obj.getName());
        }
    }    
}
