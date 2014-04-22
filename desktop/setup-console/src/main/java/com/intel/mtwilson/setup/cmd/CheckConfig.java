/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.RemoteSetup;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.Timeout;
import com.intel.mtwilson.setup.model.SetupTarget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.userauth.UserAuthException;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class CheckConfig implements Command {

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if( options.getBoolean("jpa",false) ) {
            Properties p = MyPersistenceManager.getASDataJpaProperties(My.configuration());

            System.out.println("javax.persistence.jdbc.driver = "+p.getProperty("javax.persistence.jdbc.driver"));
            System.out.println("javax.persistence.jdbc.url = "+p.getProperty("javax.persistence.jdbc.url"));
            System.out.println("javax.persistence.jdbc.host = "+p.getProperty("javax.persistence.jdbc.host"));
            System.out.println("javax.persistence.jdbc.port = "+p.getProperty("javax.persistence.jdbc.port"));
            System.out.println("javax.persistence.jdbc.schema = "+p.getProperty("javax.persistence.jdbc.schema"));
            System.out.println("javax.persistence.jdbc.user = "+p.getProperty("javax.persistence.jdbc.user"));
            System.out.println("javax.persistence.jdbc.password = "+p.getProperty("javax.persistence.jdbc.password"));
        }
        
        ArrayList<String> keys = new ArrayList<String>();
        Iterator<String> it = My.configuration().getConfiguration().getKeys();
        while(it.hasNext()) {
            keys.add(it.next());
        }
        Collections.sort(keys);
        for(String key : keys) {
            if( options.getBoolean("verbose",false)) {
                System.out.println(String.format("%s=%s [%s]", key,My.configuration().getConfiguration().getString(key), My.configuration().getSource(key)));
            }
            else {
                System.out.println(String.format("%s [%s]", key, My.configuration().getSource(key)));
            }
        }
    }
    
}
