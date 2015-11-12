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
    public static final String USERNAME = "(?:([a-zA-Z0-9_\\\\\\.@-]+))";
    public static final String PASSWORD = "(?:([a-zA-Z0-9_\\\\.\\\\, @!#$%^+=>?:{}()\\[\\]\\\"|;~`'*-/]+))";

    @Override
    protected void validate() {
        String input = getInput();
        if (input != null && !input.isEmpty()) {
            try {
                // Construct the connection string object so that we can extract the individual elements and validate them
                ConnectionString.VendorConnection cs = ConnectionString.parseConnectionString(input);
                // validate the management server name, port, host name
                if (cs.url == null) {
                    fault("Invalid URL in connection string");
                } else {
                    if (!ValidationUtil.isValidWithRegex(cs.url.getHost(), RegexPatterns.IPADDR_FQDN)) {
                        fault("Invalid hostname or IP address");
                    }
                    if (cs.url.getPort() != -1) { // -1 means the port is not set in the url  so we don't need to validate if it's -1
                        String port = String.valueOf(cs.url.getPort());
                        if (!ValidationUtil.isValidWithRegex(port, RegexPatterns.PORT)) {
                            fault("Invalid port number");
                        }
                    }
                    if (cs.options != null && !cs.options.isEmpty()) {
                        String hostnameOption = cs.options.getString(ConnectionString.OPT_HOSTNAME);
                        String passwordOption = cs.options.getString(ConnectionString.OPT_PASSWORD);
                        String usernameOption = cs.options.getString(ConnectionString.OPT_USERNAME);
                        if (!isEmpty(hostnameOption) && !ValidationUtil.isValidWithRegex(hostnameOption, RegexPatterns.IPADDR_FQDN)) {
                            fault("Invalid hostname or IP address in option");
                        }
                        if (!isEmpty(passwordOption) && !ValidationUtil.isValidWithRegex(passwordOption, PASSWORD)) {
                            fault("Invalid password");
                        }
                        if (!isEmpty(usernameOption) && !ValidationUtil.isValidWithRegex(usernameOption, USERNAME)) {
                            fault("Invalid username");
                        }
                    }
                }
            } catch (MalformedURLException ex) {
                log.error("Connection string specified is invalid. {}", ex.getMessage());
                fault(ex, "Malformed URL");
            }
        }
    }

    private boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }
}
