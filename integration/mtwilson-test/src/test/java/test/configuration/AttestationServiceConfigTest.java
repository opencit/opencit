package test.configuration;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jbuhacoff
 */
public class AttestationServiceConfigTest {
    
    /**
     * Constructor is REQUIRED for JUNIT to find the tests within NetBeans,
     * or else it says no tests found, because it first tries to instantiate
     * the class using reflection to look for a constructor, so the normal Java 
     * rule of a default empty constructor does not apply!
     */
    public AttestationServiceConfigTest() {
        
    }
    
    @Test
    public void testLoadASConfig() throws IOException {
        Configuration config = ASConfig.getConfiguration();
        assertTrue(config.containsKey("com.intel.mountwilson.as.trustagent.timeout"));
        
        Properties p = MyPersistenceManager.getASDataJpaProperties(My.configuration());
        System.out.println("javax.persistence.jdbc.url="+p.getProperty("javax.persistence.jdbc.url"));
        System.out.println("javax.persistence.jdbc.user="+p.getProperty("javax.persistence.jdbc.user"));
        System.out.println("javax.persistence.jdbc.password="+p.getProperty("javax.persistence.jdbc.password"));
        // NOTE: you do NOT want to test for specific configuration settings because they may be different on various development machines!
//        assertTrue("root".equals(config.getString("mountwilson.as.db.user")));
//        assertTrue("as_root".equals(config.getString("mountwilson.as.db.user")));
    }
}
