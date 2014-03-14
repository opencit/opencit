/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.*;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRequestRepository;
import com.intel.mtwilson.tag.rest.v2.rpc.CertificateAutoDeployRunnable;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class CertificateRequestTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateRequestTest.class);
    
//    @Test
//    public void testSearchAttr() throws Exception{
//        KvAttributeRepository repo = new KvAttributeRepository();        
//        KvAttributeFilterCriteria fc = new KvAttributeFilterCriteria();
//        fc.nameEqualTo = "Country";
//        fc.valueContains = "U";
//        KvAttributeCollection search = repo.search(fc);
//        for(KvAttribute obj : search.getKvAttributes())
//            System.out.println(obj.getName() + "::" + obj.getValue());
//    }
    
    @Test
    public void testCreateCertRequest() throws Exception{
        CertificateRequestRepository repo = new CertificateRequestRepository();        
        CertificateRequest obj = new CertificateRequest();        
        obj.setId(new UUID());
        obj.setSelectionName("Country_Selection");
        obj.setSubject("76df5add-a808-4e62-916d-e53adadc166b");
        CertificateAutoDeployRunnable crrun = new CertificateAutoDeployRunnable();
        crrun.setItem(obj);
        crrun.run();
    }

//    @Test
//    public void testRetrieveAttr() throws Exception{
//        KvAttributeRepository repo = new KvAttributeRepository();        
//        KvAttributeLocator locator = new KvAttributeLocator();
//        locator.id = UUID.valueOf("449aa4e2-7621-402e-988e-1234f3f1d59a");
//        KvAttribute retrieve = repo.retrieve(locator);
//        System.out.println(retrieve.getName() + "::" + retrieve.getValue());
//    }

}
