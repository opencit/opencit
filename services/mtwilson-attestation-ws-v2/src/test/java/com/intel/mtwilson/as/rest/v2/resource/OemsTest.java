/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.repository.OemRepository;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class OemsTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OemsTest.class);
    
    @Test
    public void testCreateOem() throws Exception {
        try {
            UUID id = new UUID();
            OemRepository repo = new OemRepository();
            Oem oem = new Oem();
            oem.setId(id);
            oem.setName("OEM1");
            repo.create(oem);

            Oem oem2 = new Oem();
            oem2.setId(id);
            oem2.setName("OEM2");
            repo.create(oem2);
        } catch (Exception ex) {
            log.debug(ex.getMessage());
        }
    }
    
}
