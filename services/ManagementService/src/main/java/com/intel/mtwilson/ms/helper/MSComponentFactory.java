/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.helper;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.ms.business.HostBO;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class MSComponentFactory {
    private static Logger log = LoggerFactory.getLogger(MSComponentFactory.class);
    
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
            log.error("Cannot load class: "+e.toString());
        }
        return premium;
    }

    public HostBO getHostBO() {
        Object premium = load("com.intel.mtwilson.ms.premium.PremiumHostBO");
        HostBO bean;
        if( premium != null ) {            
            bean = (HostBO)premium;
        }
        else {
            bean = new HostBO();
        }
        /*
        String dataEncryptionKeyString = ASConfig.getConfiguration().getString("mtwilson.as.dek");
        if( dataEncryptionKeyString != null ) {
            bean.setDataEncryptionKey(Base64.decodeBase64(dataEncryptionKeyString));
        }
        */
        return bean;
    }

}
