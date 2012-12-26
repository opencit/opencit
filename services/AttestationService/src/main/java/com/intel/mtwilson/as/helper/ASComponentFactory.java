/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.business.ReportsBO;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class ASComponentFactory {
    private static Logger log = LoggerFactory.getLogger(ASComponentFactory.class);
    
    private static Object load(String premiumClassName) {
        Object premium = null;
        try {
            Class premiumClass = Class.forName(premiumClassName);
            premium = premiumClass.newInstance();
        } catch (InstantiationException e) {
            log.error("Cannot load class: "+e.toString());
        } catch (IllegalAccessException e) {
            log.error("Cannot load class: "+e.toString());
        } catch(ClassNotFoundException e) {
            log.info("Class not found: "+e.toString());
        }
        return premium;
    }

    public HostBO getHostBO() {
        Object premium = load("com.intel.mtwilson.as.premium.PremiumHostBO");
        HostBO bean;
        if( premium != null ) {            
            bean = (HostBO)premium;
        }
        else {
            bean = new HostBO();
        }
        String dataEncryptionKeyString = ASConfig.getConfiguration().getString("mtwilson.as.dek");
        if( dataEncryptionKeyString != null ) {
            bean.setDataEncryptionKey(Base64.decodeBase64(dataEncryptionKeyString));
        }
        return bean;
    }

    public HostTrustBO getHostTrustBO() {
        Object premium = load("com.intel.mtwilson.as.premium.PremiumHostTrustBO");
        HostTrustBO bean;
        if( premium != null ) {            
            bean = (HostTrustBO)premium;
        }
        else {
            bean = new HostTrustBO();
        }
        HostBO hostBO = getHostBO();
        bean.setHostBO(hostBO);
        return bean;
    }
    
     public ReportsBO getReportsBO() {
        Object premium = load("com.intel.mtwilson.as.premium.PremiumReportsBO");
        ReportsBO bean;
        if( premium != null ) {            
            bean = (ReportsBO)premium;
        }
        else {
            bean = new ReportsBO();
        }
        String dataEncryptionKeyString = ASConfig.getConfiguration().getString("mtwilson.as.dek");
        if( dataEncryptionKeyString != null ) {
            bean.setDataEncryptionKey(Base64.decodeBase64(dataEncryptionKeyString));
        }
        return bean;
    }

}
