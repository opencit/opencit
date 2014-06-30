/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.MlePcrs;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class MlePcrTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MlePcrTest.class);

    private static MlePcrs client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new MlePcrs(My.configuration().getClientProperties());
    }
    
    @Test
    public void testCreateMlePcr() throws Exception {
        MlePcr obj = new MlePcr();
        obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        obj.setPcrIndex("21");
        obj.setPcrValue("6CAB6F19330613513101F04B88BCB7B79A8F250E");
        client.createMlePcr(obj);
    }
    
    @Test
    public void testSearchMlePcrs() throws Exception {
        MlePcrFilterCriteria criteria = new MlePcrFilterCriteria();
        criteria.mleUuid = UUID.valueOf("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        criteria.indexEqualTo = "21";
        MlePcrCollection searchMlePcrs = client.searchMlePcrs(criteria);
        for (MlePcr obj : searchMlePcrs.getMlePcrs()) {
            log.debug(obj.getPcrIndex() + "::" + obj.getPcrValue());
        }
    }
    
    @Test
    public void testRetrieveMlePcr() throws Exception {
        MlePcr obj = client.retrieveMlePcr("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "21");
        log.debug(obj.getPcrIndex() + "::" + obj.getPcrValue());
    }
    
    @Test
    public void testEditMlePcr() throws Exception {
        MlePcr obj = new MlePcr();
        obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        obj.setPcrIndex("21");
        obj.setPcrValue("AAAB6F19330613513101F04B88BCB7B79A8F250E");
        MlePcr newObj = client.editMlePcr(obj);
        log.debug(newObj.getPcrIndex() + "::" + newObj.getPcrValue());        
    }
    
    @Test
    public void testDeleteMlePcr() throws Exception {
        client.deleteMlePcr("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "21");        
    }
    
}
