/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

import com.intel.mtwilson.text.transform.PascalCaseNamingStrategy;
import java.util.Map;
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
 * @deprecated use PluginRegistry and PluginRegistryFactory
 */
public class HyphenatedCommandFinder extends PackageCommandFinder {
    private final Logger log = LoggerFactory.getLogger(getClass());
//    private Pattern camelCase = Pattern.compile("[a-z][A-Z]"); // TODO: use the character classes to support all unicode camelcase
//    private Pattern hyphenated = Pattern.compile("-([a-zA-Z0-9])"); // TODO: use the character classes to support all unicode letters / legal java class name letters
    private final PascalCaseNamingStrategy converter; //Map<String,String> map;
    
    public HyphenatedCommandFinder(String packageName) {
        super(packageName);
        converter = new PascalCaseNamingStrategy();
    }

    public HyphenatedCommandFinder(String packageName, Map<String,String> conversionMap) {
        super(packageName);
        converter = new PascalCaseNamingStrategy(conversionMap);
    }
    
    @Override
    public Command forName(String commandName) {
        try {
            // first try converting the package name from hyphenated (import-config) to camel case (ImportConfig)
            String pascalCase = converter.toPascalCase(commandName);
            if( pascalCase != null ) {
                Command command = super.forName(pascalCase);
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

    
}
