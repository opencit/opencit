/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

import java.util.ArrayList;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

/**
 * Parses extended command line arguments in the form --option=value and 
 * makes available the reduced arguments (without options) and also the
 * extracted options. The special argument "--" stops parsing and makes
 * all subsequent arguments available in the reduced arguments list.
 * The boolean form --option is also allowed and creates an option of that
 * name with the value "true". The boolean form "--no-option" is allowed and
 * creates an option of that name with the value "false".
 * @author jbuhacoff
 */
public class ExtendedOptions {
    private final String[] args;
    private String[] argsWithoutOptions = null;
    private MapConfiguration options = null;
    public ExtendedOptions(String[] args) {
        this.args = args;
        parse();
    }
    
    private void parse() {
        ArrayList<String> rest = new ArrayList<>(args.length);
        int appendAll = -1;
        Properties opts = new Properties();
        for(int i=0; i<args.length; i++) {
            if( args[i].equals("--") ) {
                // end parsing, pass all susequent arguments verbatim
                appendAll = i+1;
                break;
            }
            else if( args[i].startsWith("--") && args[i].length() > 2 ) {
                String arg = args[i].substring(2);
                if( arg.contains("=") ) {
                    String[] parts = arg.split("=");
                    if( parts.length < 1 || parts[0] == null || parts[0].isEmpty() ) { continue; }
                    if( parts.length < 2 || parts[1] == null || parts[1].isEmpty() ) { opts.setProperty(parts[0], ""); }
                    else { opts.setProperty(parts[0], parts[1]==null?"":parts[1]); }
                }
                else if( arg.startsWith("no-") && arg.length() > 3 ) {
                    String argName = arg.substring(3);
                    opts.setProperty(argName, "false");                    
                }
                else {
                    opts.setProperty(arg, "true");
                }
            }
            else {
                rest.add(args[i]);
            }
        }
        if( appendAll > -1 ) {
            for(int i=appendAll; i<args.length; i++) {
                rest.add(args[i]);
            }
        }
        argsWithoutOptions = rest.toArray(new String[rest.size()]);
        options = new MapConfiguration(opts);
    }
    
    public String[] getArguments() { return argsWithoutOptions; }
    public Configuration getOptions() { return options; }
}
