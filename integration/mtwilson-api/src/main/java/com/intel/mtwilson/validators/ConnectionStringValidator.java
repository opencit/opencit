/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.validators;

import com.intel.dcsg.cpg.validation.InputValidator;
import com.intel.mtwilson.datatypes.ConnectionString;
import java.net.MalformedURLException;

/**
 *
 * @author jbuhacoff
 */
public class ConnectionStringValidator extends InputValidator<String> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionStringValidator.class);
    
    @Override
    protected void validate() {
        String input = getInput();
        
    }
    
    private void validateConnectionString(String input) {
        ConnectionString.VendorConnection cs = null;
        if (input != null && ! input.isEmpty()) {
            try {
                // Construct the connection string object so that we can extract the individual elements and validate them
                cs = ConnectionString.parseConnectionString(input);
            } catch (MalformedURLException ex) {
                log.error("Connection string specified is invalid. {}", ex.getMessage());
                throw new IllegalArgumentException();
            }
            // validate the management server name, port, host name
//            validateInput(cs.url.getHost(), getPattern(RegExAnnotation.IPADDR_FQDN));
//            validateInput(Integer.toString(cs.url.getPort()), getPattern(RegExAnnotation.PORT));
            if (cs.options != null && !cs.options.isEmpty()) {
//                validateInput(cs.options.getString(ConnectionString.OPT_HOSTNAME), getPattern(RegExAnnotation.IPADDR_FQDN));
//                validateInput(cs.options.getString(ConnectionString.OPT_USERNAME), getPattern(RegExAnnotation.DEFAULT));
//                validateInput(cs.options.getString(ConnectionString.OPT_PASSWORD), getPattern(RegExAnnotation.PASSWORD));
            }
        }
    }
    
}
