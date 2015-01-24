/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import com.intel.mtwilson.shiro.setup.CreateAdminUser;
import com.intel.mtwilson.My;
import java.io.File;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import java.io.FileInputStream;
import java.util.Properties;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CreateAdminUserTest {
    @Test
    public void testCreateAdminUser() throws Exception {
        CreateAdminUser task = new CreateAdminUser();
        File propertiesFile = My.configuration().getConfigurationFile(); //new File(Folde()+File.separator+"mtwilson.properties");
        Properties properties = new Properties();
        try(FileInputStream in = new FileInputStream(propertiesFile)) {
            properties.load(in);
            task.setConfiguration(new PropertiesConfiguration());
            task.run();
        }
    }
}
