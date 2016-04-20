/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.as.rest.v2.model.Mle;
import com.intel.mtwilson.as.rest.v2.model.MleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleModuleLocator;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MlePcrLocator;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.repository.MleModuleRepository;
import com.intel.mtwilson.as.rest.v2.repository.MlePcrRepository;
import com.intel.mtwilson.as.rest.v2.repository.MleRepository;
import com.intel.mtwilson.as.rest.v2.repository.MleSourceRepository;
import com.intel.mtwilson.datatypes.ManifestData;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class MlesTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MlesTest.class);
    
    @Test
    public void testSearchMles() throws Exception{
        MleRepository mleRepo = new MleRepository();
        MleFilterCriteria criteria = new MleFilterCriteria();
        criteria.nameEqualTo = "Intel_Corporation";
        MleCollection search = mleRepo.search(criteria);
    }
    
    @Test
    public void testCreateMle() throws Exception {
        MleRepository mleRepo = new MleRepository();
        Mle mleObj = new Mle();
        mleObj.setId(new UUID());
        mleObj.setName("biosmle");
        mleObj.setVersion("123");
        mleObj.setOemUuid("8eb4ccce-9461-11e3-8204-005056b5286f");
        mleObj.setDescription("Testing");
        mleObj.setAttestationType(Mle.AttestationType.PCR);
        mleObj.setMleType(Mle.MleType.BIOS);
        List mleWhiteList = new ArrayList(); 
        mleWhiteList.add(new ManifestData("0", "ADC83B19E793491B1C6EA0FD8B46CD9F32E592FC"));
        mleObj.setMleManifests(mleWhiteList);
        mleRepo.create(mleObj);
    }

    @Test
    public void testUpdateMle() throws Exception {
        MleRepository mleRepo = new MleRepository();
        Mle mleObj = new Mle();
        mleObj.setId(UUID.valueOf("e012854f-b29f-4a9d-8206-c67d474b79e3"));
        List mleWhiteList = new ArrayList(); 
        mleWhiteList.add(new ManifestData("18", "AAAAAB19E793491B1C6EA0FD8B46CD9F32E592FC"));
        mleWhiteList.add(new ManifestData("19", "AAAAAB19E793491B1C6EA0FD8B46CD9F32E592FC"));
        mleObj.setMleManifests(mleWhiteList);
        mleRepo.store(mleObj);
    }


    @Test
    public void testCreateMlePcr() throws Exception {
        MlePcrRepository repo = new MlePcrRepository();
        MlePcr obj = new MlePcr();
        obj.setId(new UUID());
        obj.setPcrIndex("20");
        obj.setPcrValue("CCCCCB19E793491B1C6EA0FD8B46CD9F32E592FC");
        obj.setMleUuid("e012854f-b29f-4a9d-8206-c67d474b79e3");
        repo.create(obj);
    }
    
    @Test
    public void testUpdateMlePcr() throws Exception {
        MlePcrRepository repo = new MlePcrRepository();
        MlePcr obj = new MlePcr();
        obj.setId(UUID.valueOf("e8b4e1e2-8b21-44ab-86a3-50c474e07f9d"));
        obj.setPcrIndex("19");
        obj.setPcrValue("CCCCCB19E793491B1C6EA0FD8B46CD9F32E592FC");
        repo.store(obj);
    }

    @Test
    public void testDeleteMlePcr() throws Exception {
        MlePcrRepository repo = new MlePcrRepository();
        MlePcrLocator locator = new MlePcrLocator();
        locator.mleUuid = UUID.valueOf("e012854f-b29f-4a9d-8206-c67d474b79e3");
        locator.pcrIndex = "20";
        repo.delete(locator);
    }
    
    @Test
    public void testSearchMlePcrs() throws Exception{
        MlePcrRepository repo = new MlePcrRepository();
        MlePcrFilterCriteria criteria = new MlePcrFilterCriteria();
        criteria.mleUuid = UUID.valueOf("8f854714-9461-11e3-8204-005056b5286f"); // This has to be specified always as it is in the pathparam        
        // criteria.indexEqualTo = "18";
        criteria.valueEqualTo = "496C8530D2B4BA6A6F3901455C8C240BBB482D85";
        MlePcrCollection search = repo.search(criteria);
        if (search != null && !search.getMlePcrs().isEmpty()) {
            for (MlePcr obj : search.getMlePcrs())
            log.debug(obj.getPcrIndex() + "--" + obj.getPcrValue());
        }
    }

    @Test
    public void testRetrieveMlePcr() throws Exception{
        MlePcrRepository repo = new MlePcrRepository();
        MlePcrLocator locator = new MlePcrLocator();
        locator.mleUuid = UUID.valueOf("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
        locator.pcrIndex = "21";
        MlePcr obj = repo.retrieve(locator);
        log.debug(obj.getPcrIndex() + "--" + obj.getPcrValue());
    }
    
    @Test
    public void testCreateMleModule() throws Exception {
        MleModuleRepository repo = new MleModuleRepository();
        MleModule obj = new MleModule();
        obj.setId(new UUID());
        obj.setModuleName("20_sakljfaslf");
        obj.setModuleValue("CCCCCB19E793491B1C6EA0FD8B46CD9F32E592FC");
        obj.setMleUuid("8f854714-9461-11e3-8204-005056b5286f");
        obj.setEventName("Vim25Api.HostTpmSoftwareComponentEventDetails");
        obj.setExtendedToPCR("19");
        obj.setPackageName("net-bnx2");
        obj.setPackageVendor("VMware");
        obj.setPackageVersion("2.0.15g.v50.11-7vmw.510.0.0.799733");
        obj.setUseHostSpecificDigest(Boolean.FALSE);
        obj.setDescription("Testing");
        repo.create(obj);
    }
    
    @Test
    public void testDeleteMleModule() throws Exception {
        MleModuleRepository repo = new MleModuleRepository();
        MleModuleLocator locator = new MleModuleLocator();
        locator.id = UUID.valueOf("69f9303a-e7e1-4233-87af-2ed8e78bb7ff");
        repo.delete(locator);
        
    }
    
    @Test
    public void testSearchMleModule() throws Exception{
        MleModuleRepository repo = new MleModuleRepository();
        MleModuleFilterCriteria criteria = new MleModuleFilterCriteria();
        criteria.mleUuid = UUID.valueOf("8f854714-9461-11e3-8204-005056b5286f"); // This has to be specified always as it is in the pathparam
        // criteria.nameContains = "sata";
        criteria.valueEqualTo = "AC2D3417E3FDCBDF51D7FA16DB025D458B2470B0";
        MleModuleCollection search = repo.search(criteria);
        if (search != null && !search.getMleModules().isEmpty()) {
            for (MleModule obj : search.getMleModules())
            log.debug(obj.getModuleName()+ "--" + obj.getModuleValue());
        }
    }
    
    @Test
    public void testUpdateMleSource() throws Exception {
        MleSourceRepository repo = new MleSourceRepository();
        MleSource obj = new MleSource();
        obj.setName("Server 02");
        obj.setMleUuid("70bae24c-ac53-4b44-adbc-e4c0c98f554e");
        repo.store(obj);
    }
    
}
