/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.i18n;

import com.intel.dcsg.cpg.i18n.Localizable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Message class encapsulates localized strings. It's very generic so to maintain easy semantics in the
 * application it's recommended to subclass it.
 * @author jbuhacoff
 */
public class Message implements Localizable {
    private static Logger log = LoggerFactory.getLogger(Message.class);
    private String name;
    private Object[] args;
    private Locale locale = null;
    
    public Message(String name, Object... args) {
        this.name = name;
        this.args = args;
    }
    
    /**
     * 
     * @param locale must not be null
     * @return 
     */
    public String toString(Locale locale) {
        // load the localized resource
        ResourceBundle bundle = ResourceBundle.getBundle(BundleName.MTWILSON_STRINGS.bundle(), locale);
        log.debug("Message toString with locale: {}", locale.toString());
        log.debug("Message toString loaded resource bundle: {}", bundle.getLocale().toString());
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
        if( locale == null ) {
            return toString(Locale.getDefault());
            
        }
        return toString(locale);
    }
    
    public String getName() { return name; }
    public Object[] getParameters() { return args; }
    
    @Override
    public void setLocale(Locale locale) { this.locale = locale; }
    
    public static String localize(Locale locale, String messageName, Object params) {
        Message message = new Message(messageName, params);
        return message.toString(locale);
    }
}
