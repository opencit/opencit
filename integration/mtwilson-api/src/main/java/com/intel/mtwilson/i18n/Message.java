/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The Message class encapsulates localized strings. It's very generic so to maintain easy semantics in the
 * application it's recommended to subclass it.
 * @author jbuhacoff
 */
public class Message {
    private String name;
    private Object[] args;
    
    public Message(String name, Object... args) {
        this.name = name;
        this.args = args;
    }
    
    public String toString(Locale locale) {
        // load the localized resource
        ResourceBundle bundle = ResourceBundle.getBundle("mtwilson-strings", locale);
        String pattern = bundle.getString(name);
        // use this bundle!
        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(locale);
        formatter.applyPattern(pattern);
        String output = formatter.format(args);        
        return output;
        
    }
    
    @Override
    public String toString() {
        return toString(Locale.getDefault());
    }
    
    public String getName() { return name; }
    public Object[] getParameters() { return args; }
    
}
