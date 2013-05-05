/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package mtwilson.api;

import com.intel.mtwilson.ApiException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.Vendor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.List;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class AddHost {
    
    private List<ConnectionString> getVendorHosts(List<ConnectionString> hosts, Vendor vendor) {
        ArrayList<ConnectionString> list = new ArrayList<ConnectionString>();
        for(ConnectionString host : hosts) {
            if( host.getVendor().equals(vendor) ) {
                list.add(host);
            }
        }
        return list;
    }
    
    /**
     * Use automation tool to add a host with full info - then get the info, and use it to 
     * test the regular add host command.  Does that even make sense? 
     * 
     * This method demonstrates incorrect usage of the configureWhiteList API ... it
     * requires a complete TxtHostRecord, not just hostname and connection string.
     * 
     */
    @Test(expected=ApiException.class)
    public void testIncorrectAddIntelHost() throws IOException, ApiException, SignatureException, MalformedURLException, Exception {
        // find an intel host to add
        List<ConnectionString> hosts = My.env().getHostConnectionList();
        List<ConnectionString> intel = getVendorHosts(hosts, Vendor.INTEL);
        if( intel.isEmpty() ) {
            throw new IllegalArgumentException("No Intel hosts in your environment");
        }
        ConnectionString conn = intel.get(0); // get the first one...
        System.out.println("Adding "+conn);
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = conn.getManagementServerName();
        gkvHost.AddOn_Connection_String = conn.getConnectionStringWithPrefix();
        boolean success = My.client().configureWhiteList(gkvHost); 
        assertTrue(success);
    }

    @Test
    public void testAddIntelHost() throws IOException, ApiException, SignatureException, MalformedURLException, Exception {
        // find an intel host to add
        List<ConnectionString> hosts = My.env().getHostConnectionList();
        List<ConnectionString> intel = getVendorHosts(hosts, Vendor.INTEL);
        if( intel.isEmpty() ) {
            throw new IllegalArgumentException("No Intel hosts in your environment");
        }
        for(ConnectionString conn : intel) {
            try {
                System.out.println("Adding "+conn);
                TxtHostRecord gkvHost = new TxtHostRecord();
                gkvHost.HostName = conn.getManagementServerName();
                gkvHost.AddOn_Connection_String = conn.getConnectionStringWithPrefix();
                HostConfigData hostdata = new HostConfigData();
                hostdata.setRegisterHost(false);
                hostdata.setTxtHostRecord(gkvHost);
                boolean success = My.client().configureWhiteList(hostdata);
                assertTrue(success);
            }
            catch(ApiException e) {
                if( e.getErrorCode() == ErrorCode.MS_HOST_COMMUNICATION_ERROR.getErrorCode() ) {
                    System.err.println("Cannot add host <"+conn.getConnectionStringWithPrefix()+">: "+e.getMessage());
                }
            }
        }
    }

}
