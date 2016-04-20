/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.MleModules;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class MleModuleTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleModuleTest.class);

    private static MleModules client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new MleModules(My.configuration().getClientProperties());
    }
    
    @Test
    public void testCreateMleModule() throws Exception {
        MleModule obj = new MleModule();
        obj.setModuleName("20_sakljfaslf");
        obj.setModuleValue("CCCCCB19E793491B1C6EA0FD8B46CD9F32E592FC");
        obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        obj.setEventName("Vim25Api.HostTpmSoftwareComponentEventDetails");
        obj.setExtendedToPCR("19");
        obj.setPackageName("net-bnx2");
        obj.setPackageVendor("VMware");
        obj.setPackageVersion("2.0.15g.v50.11-7vmw.510.0.0.799733");
        obj.setUseHostSpecificDigest(Boolean.FALSE);
        obj.setDescription("Testing");
        MleModule createMleModule = client.createMleModule(obj);
        log.debug(createMleModule.getId().toString());
    }
    
    @Test
    public void testSearchMleModules() throws Exception {
        MleModuleFilterCriteria criteria = new MleModuleFilterCriteria();
        criteria.mleUuid = UUID.valueOf("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        //criteria.id = UUID.valueOf("5ae636d0-e748-4d30-9660-f797956d4bb7");
        MleModuleCollection searchMleModules = client.searchMleModules(criteria);
        for (MleModule obj : searchMleModules.getMleModules()) {
            log.debug(obj.getModuleName()+ "::" + obj.getModuleValue());
        }
    }
    
    @Test
    public void testRetrieveMleModule() throws Exception {
        MleModule obj = client.retrieveMleModule("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "0a863b84-e65b-4a23-b281-545d0f4afaf8");
        log.debug(obj.getModuleName()+ "::" + obj.getModuleValue());
    }
    
    @Test
    public void testEditMleModule() throws Exception {
        MleModule obj = new MleModule();
        obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        obj.setId(UUID.valueOf("5ae636d0-e748-4d30-9660-f797956d4bb7"));
        obj.setModuleValue("DDDDDB19E793491B1C6EA0FD8B46CD9F32E592FC");
        obj.setDescription("Updating desc");
        MleModule newObj = client.editMleModule(obj);
        log.debug(newObj.getModuleName()+ "::" + newObj.getModuleValue());        
    }
    
    @Test
    public void testDeleteMleModule() throws Exception {
        client.deleteMleModule("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "5ae636d0-e748-4d30-9660-f797956d4bb7");        
    }
    
}
