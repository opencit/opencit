/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.model.Configuration;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class ConfigurationTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationTest.class);

    private static Configurations client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new Configurations(My.configuration().getClientProperties());
    }
    
    @Test
    public void configurationTest() {
        
        Configuration config = new Configuration();
        config.setName("Config_Backup");
        Properties props = new Properties();
        props.setProperty("allowAutomaticTagSelection", "true");
        props.setProperty("automaticTagSelectionName", "d16034fe-1f3f-4648-a305-4b7f90aa213f");
        config.setContent(props);
        config = client.createConfiguration(config);
        log.debug("Created the configuration successfully with id {}", config.getId());
    }
         
}
