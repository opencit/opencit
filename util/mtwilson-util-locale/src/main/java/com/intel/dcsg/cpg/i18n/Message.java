/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.i18n;

import com.intel.dcsg.cpg.i18n.Localizable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Message class encapsulates localized strings. It's very generic so to 
 * maintain easy semantics in the
 * application it's recommended to subclass it, for example 
 * AttestationMessage, TagMessage, UserManagementMessage, etc. so each feature
 * can provide feature-specific messages in its own bundle. 
 * 
 * @author jbuhacoff
 */
public abstract class Message implements Localizable {
    private static Logger log = LoggerFactory.getLogger(Message.class);
    private String name;
    private Object[] args;
    private Locale locale = null;
    
    public Message(String name, Object... args) {
        this.name = name;
        this.args = args;
    }
    
    public abstract String getBundleName();
    
    /**
     * 
     * @param locale must not be null
     * @return 
     */
    public String toString(Locale locale) {
        // load the localized resource
        ResourceBundle bundle = ResourceBundle.getBundle(getBundleName(), locale);
        log.debug("Message toString with locale: {}", locale.toString());
        log.debug("Message toString loaded resource bundle: {}", bundle.getLocale().toString());
        try {
            String pattern = bundle.getString(name);
            // use this bundle!
            MessageFormat formatter = new MessageFormat("");
            formatter.setLocale(locale);
            formatter.applyPattern(pattern);
            String output = formatter.format(args);        
            return output;
        }
        catch(MissingResourceException e) {
            log.error("No translation for key {} in bundle {}: {}", e.getKey(), e.getClassName(), e.getLocalizedMessage());
            log.error("Message {} args {}", name, args);
            return getDefaultLocalizedMessage(locale);
        }
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
    
    /**
     * Called by toString(Locale) when there is no translation for the 
     * message;  this implementation returns the message name, subclasses
     * should override with what makes sense for them.
     * 
     * @return 
     */
    public String getDefaultLocalizedMessage(Locale locale) { return getName(); }
    
    @Override
    public void setLocale(Locale locale) { this.locale = locale; }
    
}
