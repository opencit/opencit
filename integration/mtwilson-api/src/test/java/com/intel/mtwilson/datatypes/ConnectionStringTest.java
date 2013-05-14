/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import java.net.MalformedURLException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class ConnectionStringTest {
    private transient Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testParseIntelVendorConnectionString() throws MalformedURLException {
        String intelURL = "intel:https://server.com:9999";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(intelURL);
        log.debug("Intel vendor: {}", vc.vendor);
        log.debug("Intel url: {}", vc.url);
    }

    @Test
    public void testParseCitrixVendorConnectionString() throws MalformedURLException {
        String citrixURL = "citrix:https://server.com:443;username;password";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(citrixURL);
        log.debug("Citrix vendor: {}", vc.vendor);
        log.debug("Citrix url: {}", vc.url);
        log.debug("Citrix option username: {}", vc.options == null ? "no options" : vc.options.getString("u"));
        log.debug("Citrix option password: {}", vc.options == null ? "no options" : vc.options.getString("p"));
    }

    @Test
    public void testParseCitrixVendorConnectionStringWithNamedOptions() throws MalformedURLException {
        String citrixURL = "citrix:https://server.com:443;u=username;p=password";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(citrixURL);
        log.debug("Citrix vendor: {}", vc.vendor);
        log.debug("Citrix url: {}", vc.url);
        log.debug("Citrix option username: {}", vc.options == null ? "no options" : vc.options.getString("u"));
        log.debug("Citrix option password: {}", vc.options == null ? "no options" : vc.options.getString("p"));
    }

    
    @Test
    public void testParseVmwareVendorConnectionString() throws MalformedURLException {
        String citrixURL = "vmware:https://server.com:443/sdk;username;password;hostname";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(citrixURL);
        log.debug("Vmware vendor: {}", vc.vendor);
        log.debug("Vmware url: {}", vc.url);
        log.debug("Vmware option username: {}", vc.options == null ? "no options" : vc.options.getString("u"));
        log.debug("Vmware option password: {}", vc.options == null ? "no options" : vc.options.getString("p"));
        log.debug("Vmware option hostname: {}", vc.options == null ? "no options" : vc.options.getString("h"));
    }
    
    @Test
    public void testParseVmwareVendorConnectionStringWithNamedOptions() throws MalformedURLException {
        String citrixURL = "vmware:https://server.com:443/sdk;u=username;p=password;h=hostname";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(citrixURL);
        log.debug("Vmware vendor: {}", vc.vendor);
        log.debug("Vmware url: {}", vc.url);
        log.debug("Vmware option username: {}", vc.options == null ? "no options" : vc.options.getString("u"));
        log.debug("Vmware option password: {}", vc.options == null ? "no options" : vc.options.getString("p"));
        log.debug("Vmware option hostname: {}", vc.options == null ? "no options" : vc.options.getString("h"));
    }
    
    @Test
    public void testCitrixConnectionString() throws MalformedURLException {
        String url = "citrix:https://server.com:443;username;password";
        ConnectionString cs = new ConnectionString(url);
        log.debug("Hostname: {}", cs.getHostname());
        log.debug("Port: {}", cs.getPort());
        log.debug("AddOnConnectionString: {}", cs.getAddOnConnectionString());
        log.debug("ConnectionString: {}", cs.getConnectionString());
        log.debug("ConnectionString with prefix: {}", cs.getConnectionStringWithPrefix());
        log.debug("Management Server: {}", cs.getManagementServerName());
    }

    @Test
    public void testCitrixConnectionStringWithNamedParameters() throws MalformedURLException {
        String url = "citrix:https://server.com:443;u=username;p=password";
        ConnectionString cs = new ConnectionString(url);
        log.debug("Hostname: {}", cs.getHostname().toString());
        log.debug("Port: {}", cs.getPort());
        log.debug("AddOnConnectionString: {}", cs.getAddOnConnectionString());
        log.debug("ConnectionString: {}", cs.getConnectionString());
        log.debug("ConnectionString with prefix: {}", cs.getConnectionStringWithPrefix());
        log.debug("Management Server: {}", cs.getManagementServerName());
    }

    @Test
    public void testVmwareConnectionString() throws MalformedURLException {
        String url = "vmware:https://server.com:443/sdk;username;password;hostname";
        ConnectionString cs = new ConnectionString(url);
        log.debug("Hostname: {}", cs.getHostname());
        log.debug("Port: {}", cs.getPort());
        log.debug("AddOnConnectionString: {}", cs.getAddOnConnectionString());
        log.debug("ConnectionString: {}", cs.getConnectionString());
        log.debug("ConnectionString with prefix: {}", cs.getConnectionStringWithPrefix());
        log.debug("Management Server: {}", cs.getManagementServerName());
    }

    @Test
    public void testVmwareConnectionStringWithNamedParameters() throws MalformedURLException {
        String url = "vmware:https://server.com:443/sdk;u=username;p=password;h=hostname";
        ConnectionString cs = new ConnectionString(url);
        log.debug("Hostname: {}", cs.getHostname().toString());
        log.debug("Port: {}", cs.getPort());
        log.debug("AddOnConnectionString: {}", cs.getAddOnConnectionString());
        log.debug("ConnectionString: {}", cs.getConnectionString());
        log.debug("ConnectionString with prefix: {}", cs.getConnectionStringWithPrefix());
        log.debug("Management Server: {}", cs.getManagementServerName());
    }

    
    @Test
    public void testGuessIntelConnectionStringFromTxtHost() throws MalformedURLException {
        TxtHostRecord txtHostRecord = new TxtHostRecord();
        txtHostRecord.AddOn_Connection_String = null;
        txtHostRecord.IPAddress = "1.2.3.4";
        txtHostRecord.Port = 9999;
        ConnectionString cs = ConnectionString.from(txtHostRecord);
        log.debug("Intel vendor: {}", cs.getVendor());
        log.debug("Intel url: {}", cs.getConnectionStringWithPrefix());
    }
    
}
