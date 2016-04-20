/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.*;
import com.intel.mtwilson.tag.rest.v2.repository.TpmPasswordRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class TpmPasswordTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmPasswordTest.class);
    
    @Test
    public void testSearchTpmPassword() throws Exception{
        TpmPasswordRepository repo = new TpmPasswordRepository();        
        TpmPasswordFilterCriteria fc = new TpmPasswordFilterCriteria();
        fc.id = UUID.valueOf("9d037c57-ff5c-4af3-9a4c-dcf9709fc006");
        TpmPasswordCollection search = repo.search(fc);
        for(TpmPassword obj : search.getTpmPasswords())
            System.out.println(obj.getPassword());
    }
    
    @Test
    public void testCreateTpmPassword() throws Exception{
        TpmPasswordRepository repo = new TpmPasswordRepository();        
        TpmPassword obj = new TpmPassword();
        obj.setId(new UUID());
        obj.setPassword("New password");
        repo.create(obj);
    }

    @Test
    public void testRetrieveTpmPassword() throws Exception{
        TpmPasswordRepository repo = new TpmPasswordRepository();        
        TpmPasswordLocator locator = new TpmPasswordLocator();
        locator.id = UUID.valueOf("9d037c57-ff5c-4af3-9a4c-dcf9709fc006");
        TpmPassword retrieve = repo.retrieve(locator);
        System.out.println(retrieve.getPassword());
    }

    @Test
    public void testDeleteTpmPassword() throws Exception{
        TpmPasswordRepository repo = new TpmPasswordRepository();        
        TpmPasswordLocator locator = new TpmPasswordLocator();
        locator.id = UUID.valueOf("9d037c57-ff5c-4af3-9a4c-dcf9709fc006");
        repo.delete(locator);
    }
    
}
