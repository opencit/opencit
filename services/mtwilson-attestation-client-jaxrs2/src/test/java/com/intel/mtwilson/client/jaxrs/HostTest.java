/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.attestation.client.jaxrs.Hosts;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.security.cert.CertificateException;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class HostTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostTest.class);

    private static Hosts client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator.class);

        client = new Hosts(My.configuration().getClientProperties());
    }
    
    @Test
    public void testSearchCollection() {
        HostFilterCriteria criteria = new HostFilterCriteria();
        criteria.nameContains = "10";
        HostCollection objCollection = client.searchHosts(criteria);
        for(Host obj : objCollection.getHosts()) {
            log.debug("Host name {}", obj.getName());
        }
    }
    
    @Test
    public void testCreate() {
        Host obj = new Host();
        obj.setName("10.1.71.155");
        obj.setConnectionUrl("https://10.1.71.87:443/sdk;Administrator;P@ssw0rd");
        obj.setBiosMleUuid("7e90c088-c9c7-486f-9480-9cd0a7a3b977");
        obj.setVmmMleUuid("fb2cb173-5e19-446b-9161-aa7368c5c882");
        obj.setTlsPolicyId("e1a527b5-2020-49c1-83be-6bd8bf641258");
        Host createHost = client.createHost(obj);
        log.debug("New Host created with UUID {}.", createHost.getId().toString());
    }
    
    @Test
    public void testEdit() {
        Host obj = new Host();
        obj.setId(UUID.valueOf("6d0bbcf9-b662-4d59-bc71-7b360afeb94a"));
        obj.setDescription("Updated the host");
        Host editHost = client.editHost(obj);
        log.debug("Host updated with new desc {}.", editHost.getDescription());
    }

    @Test
    public void testRetrieve() throws Exception {
        Host retrieveHost = client.retrieveHost("6d0bbcf9-b662-4d59-bc71-7b360afeb94a");
        log.debug(retrieveHost.getName());
    }

    @Test
    public void testDelete() {
        client.deleteHost("6d0bbcf9-b662-4d59-bc71-7b360afeb94a");
        log.debug("Revoked the asset tag certificate successfully");
    }
    
    @Test
    public void testHostPreRegistration() {
        log.debug("About to pre-register host details");
        client.preRegisterHostDetails("mhsbubu1404", "apiclient", "apipwd");
        log.debug("Registered successfully");
    }
    
}
