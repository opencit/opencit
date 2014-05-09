/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import java.security.cert.CertificateException;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class HostTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileTest.class);

    private static Hosts client = null;
    
    @BeforeClass
    public static void init() throws Exception {
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
        obj.setName("10.1.71.175");
        obj.setConnectionUrl("https://10.1.71.162:443/sdk;Administrator;intel123!");
        obj.setBiosMleUuid("b14e5039-373d-4743-aa65-1e24c23dd249");
        obj.setVmmMleUuid("3a4503a1-1632-433f-bca7-5655ccbafec4");
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
    
}
