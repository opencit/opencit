/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.tag.model.TpmPasswordCollection;
import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class TpmPasswordTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmPasswordTest.class);

    private static TpmPasswords client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new TpmPasswords(My.configuration().getClientProperties());
    }
    
    @Test
    public void tpmPasswordTest() {
        
        TpmPassword obj = new TpmPassword();
        obj.setId(UUID.valueOf("07217f9c-f625-4c5a-a538-73f1880abdda"));
        obj.setPassword("password");
        obj = client.createTpmPassword(obj);
        
        TpmPasswordFilterCriteria criteria = new TpmPasswordFilterCriteria();
        criteria.id = obj.getId();
        TpmPasswordCollection objCollection = client.searchTpmPasswords(criteria);
        for (TpmPassword tObj : objCollection.getTpmPasswords()) {
            log.debug(tObj.getId().toString());
        }
    }
         
}
