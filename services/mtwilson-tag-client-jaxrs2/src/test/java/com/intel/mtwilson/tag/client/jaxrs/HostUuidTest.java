/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.model.HostUuid;
import com.intel.mtwilson.tag.model.HostUuidCollection;
import com.intel.mtwilson.tag.model.HostUuidFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class HostUuidTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostUuidTest.class);

    private static HostUuids client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new HostUuids(My.configuration().getClientProperties());
    }
    
    @Test
    public void hostUuidTest() {
                
        HostUuidFilterCriteria criteria = new HostUuidFilterCriteria();
        criteria.hostId = "10.1.71.155";
        HostUuidCollection objCollection = client.searchHostUuids(criteria);
        for (HostUuid tObj : objCollection.getHostUuids()) {
            log.debug(tObj.getHardwareUuid());
        }
    }
         
}
