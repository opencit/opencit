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
    public void testSearchSlectionKvAttr() throws Exception{
        SelectionKvAttributeRepository repo = new SelectionKvAttributeRepository();        
        SelectionKvAttributeFilterCriteria fc = new SelectionKvAttributeFilterCriteria();
        //fc.nameContains = "Coun";
        //fc.attrNameContains = "U";
        SelectionKvAttributeCollection search = repo.search(fc);
        for(SelectionKvAttribute obj : search.getSelectionKvAttributeValues())
            System.out.println(obj.getSelectionName() + "||" + obj.getKvAttributeName() + "||" + obj.getKvAttributeValue());
    }
    
    @Test
    public void testCreateSelectionKvAttr() throws Exception{
        SelectionKvAttributeRepository repo = new SelectionKvAttributeRepository();        
        SelectionKvAttribute obj = new SelectionKvAttribute();
        obj.setId(new UUID());
        obj.setSelectionName("Country_Selection");
        obj.setKvAttributeId(UUID.valueOf("cd6aa229-6120-4c0a-848a-b67a6a46233c"));
        repo.create(obj);
    }

}
