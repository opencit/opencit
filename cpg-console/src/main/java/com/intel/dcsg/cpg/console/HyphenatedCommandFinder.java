/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a package to search (packageName) and a command name (commandName), this class will attempt
 * to load packageName.commandName and cast it to a Command interface.
 * 
 * For example:
 * <code>
 * HyphenatedCommandFinder finder = new HyphenatedCommandFinder("test.cmd");
 * Command toRun = finder.forName("hello-world"); // will convert hello-world to HellowWorld and search for test.cmd.HelloWorld
 * </code>
 * 
 * Sometimes it's convenient to map certain words to their uppercased equivalent, for example if you want "mtwilson" to
 * be converted to "MtWilson" instead of "Mtwilson", you would provide a mapping: 
 * <code>
 * HashMap<String,String> map = new HashMap<String,String>();
 * map.put("mtwilson", "MtWilson");
 * HyphenatedCommandFinder finder = new HyphenatedCommandFinder("test.cmd", map);
 * Command toRun = finder.forName("mtwilson-start"); // will convert mtwilson-start to MtWilsonStart and search for test.cmd.MtWilsonStart
 * // whereas without the map it would have been converted to MtwilsonStart and searched for test.cmd.MtwilsonStart.
 * </code>
 *
 * @author jbuhacoff
 */
public class HyphenatedCommandFinder extends PackageCommandFinder {
    private final Logger log = LoggerFactory.getLogger(getClass());
//    private Pattern camelCase = Pattern.compile("[a-z][A-Z]"); // TODO: use the character classes to support all unicode camelcase
//    private Pattern hyphenated = Pattern.compile("-([a-zA-Z0-9])"); // TODO: use the character classes to support all unicode letters / legal java class name letters
    private final Map<String,String> map;
    
    public HyphenatedCommandFinder(String packageName) {
        super(packageName);
        map = null;
    }

    public HyphenatedCommandFinder(String packageName, Map<String,String> conversionMap) {
        super(packageName);
        map = conversionMap;
    }
    
    @Override
    public Command forName(String commandName) {
        try {
            // first try converting the package name from hyphenated (import-config) to camel case (ImportConfig)
            String camelCase = toCamelCase(commandName);
            if( camelCase != null ) {
                Command command = super.forName(camelCase);
                if( command != null ) {
                    return command;
                }
            }
            // didn't find a match, so try it "as is"
            return super.forName(commandName);
        }
        catch(Exception e) {
            log.debug("Cannot load command "+commandName+": "+e.toString());
            return null;
        }
    }
    
    
    /**
     * The transformation of command-name to CommandName:
     * First letter is uppercased
     * Every letter after a hyphen is uppercased
     * Hyphens are removed 
     * @param propertyName
     * @return all-uppercase version of property name, dots converted to underscores, and camelCase words separated by underscore
     */
    public String toCamelCase(String commandName) {
        StringBuilder camelCaseWords = new StringBuilder();
        String parts[] = commandName.split("-");
        if( parts == null || parts.length == 0 ) { return null; }
        for(int i=0; i<parts.length; i++) {
            if( map != null && map.containsKey(parts[i]) ) {
                camelCaseWords.append(map.get(parts[i]));
            }
            else {
                camelCaseWords.append(StringUtils.capitalize(parts[i]));
            }
        }
        return camelCaseWords.toString();
    }
    
}
