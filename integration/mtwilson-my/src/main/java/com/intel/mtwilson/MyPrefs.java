/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.util.prefs.Preferences;

/**
 * Examples:
 * java -jar my-1.2-SNAPSHOT-with-dependencies.jar  set mtwilson.config.dir C:\Intel\CloudSecurity
 * java -jar my-1.2-SNAPSHOT-with-dependencies.jar  remove mtwilson.config.dir    (will cause your config to be read from ~/.mtwilson, the default)
 * @author jbuhacoff
 */
public class MyPrefs {
    private static Preferences prefs = Preferences.userRoot().node("com.intel.mtwilson"); // should we use systemRoot() instead? userRoot() makes sense for developers, systemRoot() might sense for production systems, except that most sysadmins prefer to have production settings in easy-to-find configuration files rather than in java preferences anyway.

    public static void printUsage() {
        System.out.println("Usage: java -cp my.jar com.intel.mtwilson.MyPrefs get <key>");
        System.out.println("Usage: java -cp my.jar com.intel.mtwilson.MyPrefs set <key> <value>");
        System.out.println("Usage: java -cp my.jar com.intel.mtwilson.MyPrefs remove <key>");
    }
    
    public static void requireMinArgs(int min, String[] args) {
        if( args.length < min ) {
            printUsage();
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        //ExtendedOptions getopt = new ExtendedOptions(args);
        //Configuration options = getopt.getOptions();
        requireMinArgs(1, args); // for the verb:  get, set, remove
        String verb = args[0];
        if( verb != null && verb.equals("get") ) {
            requireMinArgs(2, args); // for the verb and the key
            String key = args[1];
            String value = prefs.get(key, "no preference");
            System.out.println(value);
            return;
        }
        if( verb != null && verb.equals("set") ) {
            requireMinArgs(2, args); // for the verb, key, and value
            String key = args[1];
            String value = args[2];
            prefs.put(key, value);
//            System.out.println(key+"="+value);
            return;
        }
        if( verb != null && verb.equals("remove") ) {
            requireMinArgs(2, args); // for the verb and the key
            String key = args[1];
            prefs.remove(key);
            return;
        }
        System.out.println("Unrecognized command");
        printUsage();
        System.exit(2);
    }
}
