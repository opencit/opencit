/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.validators;

import com.intel.dcsg.cpg.validation.InputValidator;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.datatypes.ConnectionString;
import java.net.MalformedURLException;

/**
 *
 * @author jbuhacoff
 */
public class ConnectionStringValidator extends InputValidator<String> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionStringValidator.class);
    // TODO:  these need to be customized for what's valid on URLs, or maybe we need to look for encoded charactesr in URLs
    public static final String USERNAME = "(?:([a-zA-Z0-9_\\\\\\.@-]+))";
    public static final String PASSWORD = "(?:([a-zA-Z0-9_\\\\.\\\\, @!#$%^+=>?:{}()\\[\\]\\\"|;~`'*-]+))";

    @Override
    protected void validate() {
        String input = getInput();
        if (input != null && !input.isEmpty()) {
            try {
                // Construct the connection string object so that we can extract the individual elements and validate them
                ConnectionString.VendorConnection cs = ConnectionString.parseConnectionString(input);
                // validate the management server name, port, host name
//            validateInput(cs.url.getHost(), getPattern(RegExAnnotation.IPADDR_FQDN));
//            validateInput(Integer.toString(cs.url.getPort()), getPattern(RegExAnnotation.PORT));
                if (cs.options != null && !cs.options.isEmpty()) {
                    if (!ValidationUtil.isValidWithRegex(cs.options.getString(ConnectionString.OPT_HOSTNAME), RegexPatterns.IPADDR_FQDN)) {
                        fault("Invalid hostname or IP address");
                    }
                    if (!ValidationUtil.isValidWithRegex(cs.options.getString(ConnectionString.OPT_PASSWORD), PASSWORD)) {
                        fault("Invalid password");
                    }
                    if (!ValidationUtil.isValidWithRegex(cs.options.getString(ConnectionString.OPT_USERNAME), USERNAME)) {
                        fault("Invalid username");
                    }
                }
            } catch (MalformedURLException ex) {
                log.error("Connection string specified is invalid. {}", ex.getMessage());
                fault(ex, "Malformed URL");
            }
        }
    }
}
