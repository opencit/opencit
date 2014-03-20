/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.*;
import com.intel.mtwilson.tag.rest.v2.repository.SelectionRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class SelectionTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionTest.class);
        
    @Test
    public void testCreateSelection() throws Exception{
        SelectionRepository repo = new SelectionRepository();        
        Selection obj = new Selection();
        obj.setId(new UUID());
        obj.setName("Country_Selection");
        obj.setDescription("This selection denotes the country.");
        repo.create(obj);
    }

    @Test
    public void testEditSelection() throws Exception{
        SelectionRepository repo = new SelectionRepository();        
        Selection obj = new Selection();
        obj.setId(UUID.valueOf("e404ee8a-b114-40cc-b75f-a99d82fc11d7"));
        obj.setDescription("This selection denotes the country.");
        repo.store(obj);
    }

    @Test
    public void testDeleteSelection() throws Exception{
        SelectionRepository repo = new SelectionRepository();        
        SelectionLocator locator = new SelectionLocator();
        locator.id = UUID.valueOf("820524e9-5f34-4168-829c-f3efc3d41d2a");
        repo.delete(locator);
    }
    
}
