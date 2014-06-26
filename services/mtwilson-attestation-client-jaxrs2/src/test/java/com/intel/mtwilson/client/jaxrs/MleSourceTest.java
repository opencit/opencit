/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.MleSources;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class MleSourceTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleSourceTest.class);

    private static MleSources client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new MleSources(My.configuration().getClientProperties());
    }
    
    @Test
    public void testCreateMleSource() throws Exception {
        MleSource obj = new MleSource();
        obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        obj.setName("100.1.1.1");
        MleSource newObj = client.createMleSource(obj);
        log.debug(newObj.getMleUuid() + "::" + newObj.getName() + "::" + newObj.getId().toString());
    }
    
    @Test
    public void testSearchMleSources() throws Exception {
        MleSourceFilterCriteria criteria = new MleSourceFilterCriteria();
        criteria.mleUuid = UUID.valueOf("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        MleSourceCollection searchMleSources = client.searchMleSources(criteria);
        for (MleSource obj : searchMleSources.getMleSources()) {
            log.debug(obj.getMleUuid() + "::" + obj.getName() + "::" + obj.getId().toString());
        }
    }
    
    @Test
    public void testRetrieveMleSource() throws Exception {
        MleSource obj = client.retrieveMleSource("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "652e0b01-bcee-45cd-ae4d-ae561029dbd4");
        log.debug(obj.getMleUuid() + "::" + obj.getName() + "::" + obj.getId().toString());
    }
    
    @Test
    public void testEditMleSource() throws Exception {
        MleSource obj = new MleSource();
        obj.setId(UUID.valueOf("652e0b01-bcee-45cd-ae4d-ae561029dbd4"));
        obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        obj.setName("10.1.71.100");
        MleSource newObj = client.editMleSource(obj);
        log.debug(newObj.getMleUuid() + "::" + newObj.getName() + "::" + newObj.getId().toString());
    }
    
    @Test
    public void testDeleteMleSource() throws Exception {
        client.deleteMleSource("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "8bc7bdfe-fe20-4385-9263-0e689e776f92");        
    }
    
}
