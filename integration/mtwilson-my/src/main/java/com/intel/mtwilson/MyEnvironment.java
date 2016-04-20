/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.intel.mtwilson.datatypes.ConnectionString;
import java.net.MalformedURLException;
/**
 *
 * @author jbuhacoff
 */
public class MyEnvironment {
    private final ArrayList<ConnectionString> hostConnectionStrings;
    
    public MyEnvironment(File environmentFile) throws FileNotFoundException, IOException {
        try(FileInputStream in = new FileInputStream(environmentFile)) {
        String hostList = IOUtils.toString(in); // one connection string per line
        String[] hosts = StringUtils.split(hostList, "\n\r");
        hostConnectionStrings = new ArrayList<>();
        for(String host : hosts) {
            if( host != null && !host.trim().isEmpty() ) {
                try {
                    hostConnectionStrings.add(new ConnectionString(host));
                }
                catch(MalformedURLException e) {
                    System.err.println("Invalid host URL: "+host);
                }
            }
        }
        }
    }
    public List<ConnectionString> getHostConnectionList() {
        return hostConnectionStrings;
    }
}
