/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.*;
import com.intel.mtwilson.tag.rest.v2.repository.ConfigurationRepository;
import com.intel.mtwilson.tag.rest.v2.repository.KvAttributeRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class ConfigurationTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationTest.class);
    
    @Test
    public void testSearch() throws Exception{
        ConfigurationRepository repo = new ConfigurationRepository();        
        ConfigurationFilterCriteria fc = new ConfigurationFilterCriteria();
        ConfigurationCollection search = repo.search(fc);
        for(Configuration obj : search.getConfigurations())
            System.out.println(obj.getName() + "::" + obj.getXmlContent());
    }
    

}
