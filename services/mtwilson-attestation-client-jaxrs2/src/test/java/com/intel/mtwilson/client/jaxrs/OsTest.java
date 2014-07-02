/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.Oss;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class OsTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OsTest.class);

    private static Oss client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new Oss(My.configuration().getClientProperties());
    }
    
    @Test
    public void testOsOps() {
        
        Os newOs = new Os();
        newOs.setName("TestOS1");
        newOs.setVersion("1.2.3");
        newOs.setDescription("New OS Configuration.");
        newOs = client.createOs(newOs);
        log.debug("Created new Os {} with UUID {} successfully.", newOs.getName(), newOs.getId().toString());
        
        UUID newOsUuid = newOs.getId();
        
        OsFilterCriteria criteria = new OsFilterCriteria();
        criteria.nameContains = "OS1";
        OsCollection oss = client.searchOss(criteria);
        for(Os os : oss.getOss()) {
            log.debug("Searched retrieved Os {} with UUID {} successfully.", os.getName(), os.getId().toString());
        }
        
        newOs.setDescription("Updated the description");
        client.editOs(newOs);
        log.debug("Edited Os {} with UUID {} successfully.", newOs.getName(), newOs.getId().toString());
        
        Os retrieveOs = client.retrieveOs(newOsUuid.toString());
        log.debug("Retrieved Os {} with description {} successfully.", retrieveOs.getName(), retrieveOs.getDescription());
        
        client.deleteOs(newOsUuid.toString());

    }
        
}
