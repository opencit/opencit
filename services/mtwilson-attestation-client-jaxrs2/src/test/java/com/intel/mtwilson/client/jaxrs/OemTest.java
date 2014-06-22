/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.Oems;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class OemTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OemTest.class);

    private static Oems client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new Oems(My.configuration().getClientProperties());
    }
    
    @Test
    public void testSearchCollection() {
        OemFilterCriteria criteria = new OemFilterCriteria();
        //criteria.id = new UUID();
        criteria.nameContains = "ibm";
        //criteria.nameEqualTo = "nameequalto";
        OemCollection oems = client.searchOems(criteria);
        for(Oem oem : oems.getOems()) {
            log.debug("Oem name {}", oem.getName());
        }
    }
    
    @Test
    public void testCreate() {
        Oem oem = new Oem();
        oem.setName("APIOEM");
        oem.setDescription("API Created OEM");
        Oem createOem = client.createOem(oem);
        log.debug("New OEM created with UUID {}.", createOem.getId().toString());
    }
    
    @Test
    public void testRetrieve() {
        Oem retrieveOem = client.retrieveOem("27ae76f0-e678-4224-92fc-a91ebbf761b8");
        log.debug(retrieveOem.getName() + ":::" + retrieveOem.getDescription());
    }

    @Test
    public void testEdit() {
        Oem oem = new Oem();
        oem.setId(UUID.valueOf("27ae76f0-e678-4224-92fc-a91ebbf761b8"));
        oem.setDescription("Updated description");
        oem = client.editOem(oem);
        log.debug(oem.getName() + "--" + oem.getId().toString());
    }

    @Test
    public void testDelete() {
        client.deleteOem("27ae76f0-e678-4224-92fc-a91ebbf761b8");
        log.debug("Deleted the OEM successfully");
    }
    
}
