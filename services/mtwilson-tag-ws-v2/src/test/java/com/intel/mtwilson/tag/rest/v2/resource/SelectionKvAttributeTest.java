/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.*;
import com.intel.mtwilson.tag.rest.v2.repository.SelectionKvAttributeRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class SelectionKvAttributeTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionKvAttributeTest.class);
    
    @Test
    public void testSearchAttr() throws Exception{
        SelectionKvAttributeRepository repo = new SelectionKvAttributeRepository();        
        SelectionKvAttributeFilterCriteria fc = new SelectionKvAttributeFilterCriteria();
        //fc.nameContains = "Coun";
        //fc.attrNameContains = "U";
        SelectionKvAttributeCollection search = repo.search(fc);
        for(SelectionKvAttribute obj : search.getSelectionKvAttributeValues())
            System.out.println(obj.getSelectionName() + "||" + obj.getKvAttributeName() + "||" + obj.getKvAttributeValue());
    }
    
    @Test
    public void testCreateAttr() throws Exception{
        SelectionKvAttributeRepository repo = new SelectionKvAttributeRepository();        
        SelectionKvAttribute obj = new SelectionKvAttribute();
        obj.setId(new UUID());
        obj.setSelectionName("Country_Selection");
        obj.setKvAttributeId(UUID.valueOf("cd6aa229-6120-4c0a-848a-b67a6a46233c"));
        repo.create(obj);
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
