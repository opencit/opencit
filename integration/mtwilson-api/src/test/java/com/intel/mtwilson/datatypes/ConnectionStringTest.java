/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.validators.ConnectionStringValidator;
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
    
    private void validate(String input) {
        ConnectionStringValidator validator = new ConnectionStringValidator();
        validator.setInput(input);
        if(!validator.isValid()) {
            log.error("Invalid connection string: {}", input);
            for(Fault fault : validator.getFaults()) {
                log.error("Validation error: {}", fault.toString());
            }
            throw new IllegalArgumentException("Validation failed for: "+input);
        }        
    }
    
     
    @Test 
    public void testParseIntelVendorConnectionString() throws MalformedURLException {
        String url = "intel:https://server.com:1443;user;password";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(url);
        log.debug("Intel vendor: {}", vc.vendor);
        log.debug("Intel url: {}", vc.url);
        validate(url);
    }

    @Test
    public void testParseCitrixVendorConnectionString() throws MalformedURLException {
        String url = "citrix:https://server.com:443;username;password";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(url);
        log.debug("Citrix vendor: {}", vc.vendor);
        log.debug("Citrix url: {}", vc.url);
        log.debug("Citrix option username: {}", vc.options == null ? "no options" : vc.options.getString("u"));
        log.debug("Citrix option password: {}", vc.options == null ? "no options" : vc.options.getString("p"));
        validate(url);
    }

    @Test
    public void testParseCitrixVendorConnectionStringWithNamedOptions() throws MalformedURLException {
        String url = "citrix:https://server.com:443;u=username;p=password";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(url);
        log.debug("Citrix vendor: {}", vc.vendor);
        log.debug("Citrix url: {}", vc.url);
        log.debug("Citrix option username: {}", vc.options == null ? "no options" : vc.options.getString("u"));
        log.debug("Citrix option password: {}", vc.options == null ? "no options" : vc.options.getString("p"));
        validate(url);
    }

    
    @Test
    public void testParseVmwareVendorConnectionStringWithHostname() throws MalformedURLException {
        String url = "vmware:https://server.com:443/sdk;username;password;hostname";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(url);
        log.debug("Vmware vendor: {}", vc.vendor);
        log.debug("Vmware url: {}", vc.url);
        log.debug("Vmware option username: {}", vc.options == null ? "no options" : vc.options.getString("u"));
        log.debug("Vmware option password: {}", vc.options == null ? "no options" : vc.options.getString("p"));
        log.debug("Vmware option hostname: {}", vc.options == null ? "no options" : vc.options.getString("h"));
        validate(url);
    }

    @Test
    public void testParseVmwareVendorConnectionString() throws MalformedURLException {
        String url = "vmware:https://server.com:443/sdk;username;password";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(url);
        log.debug("Vmware vendor: {}", vc.vendor);
        log.debug("Vmware url: {}", vc.url);
        log.debug("Vmware option username: {}", vc.options == null ? "no options" : vc.options.getString("u"));
        log.debug("Vmware option password: {}", vc.options == null ? "no options" : vc.options.getString("p"));
        log.debug("Vmware option hostname: {}", vc.options == null ? "no options" : vc.options.getString("h"));
        validate(url);
    }
    
    @Test
    public void testParseVmwareVendorConnectionStringWithNamedOptions() throws MalformedURLException {
        String url = "vmware:https://server.com:443/sdk;u=username;p=password;h=hostname";
        ConnectionString.VendorConnection vc = ConnectionString.parseConnectionString(url);
        log.debug("Vmware vendor: {}", vc.vendor);
        log.debug("Vmware url: {}", vc.url);
        log.debug("Vmware option username: {}", vc.options == null ? "no options" : vc.options.getString("u"));
        log.debug("Vmware option password: {}", vc.options == null ? "no options" : vc.options.getString("p"));
        log.debug("Vmware option hostname: {}", vc.options == null ? "no options" : vc.options.getString("h"));
        validate(url);
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
        validate(url);
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
        validate(url);
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
        validate(url);
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
        validate(url);
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
