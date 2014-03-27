/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.*;
import com.intel.mtwilson.tag.rest.v2.repository.KvAttributeRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class KvAttributeTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KvAttributeTest.class);
    
    @Test
    public void testSearchAttr() throws Exception{
        KvAttributeRepository repo = new KvAttributeRepository();        
        KvAttributeFilterCriteria fc = new KvAttributeFilterCriteria();
        fc.nameEqualTo = "Country";
        fc.valueContains = "U";
        KvAttributeCollection search = repo.search(fc);
        for(KvAttribute obj : search.getKvAttributes())
            System.out.println(obj.getName() + "::" + obj.getValue());
    }
    
    @Test
    public void testCreateAttr() throws Exception{
        KvAttributeRepository repo = new KvAttributeRepository();        
        KvAttribute obj = new KvAttribute();
        obj.setId(new UUID());
        obj.setName("Country");
        obj.setValue("USA");
        repo.create(obj);
    }

    @Test
    public void testRetrieveAttr() throws Exception{
        KvAttributeRepository repo = new KvAttributeRepository();        
        KvAttributeLocator locator = new KvAttributeLocator();
        locator.id = UUID.valueOf("449aa4e2-7621-402e-988e-1234f3f1d59a");
        KvAttribute retrieve = repo.retrieve(locator);
        System.out.println(retrieve.getName() + "::" + retrieve.getValue());
    }

}
