/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.handler;

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOTE: the audit-api package exists so that the ASData/MSData packages can include it for
 * configuring auditing via annotations, yet not to depend on audit-data or audit-handler because
 * that would cause a circular dependency due to the "my" module importing all the *data 
 * modules and the audit-handler needing the "my" module for configuration.
 * 
 * @author dsmagadx
 */
public class AuditEventHandler implements
        DescriptorCustomizer {
    private static Logger log = LoggerFactory.getLogger(AuditEventHandler.class);

    @Override
    public void customize(ClassDescriptor cd) throws Exception {
        try {
            // at runtime... load the audit event handler implementation from audit-handler package 
            Class t = Class.forName("com.intel.mtwilson.audit.handler.AuditEventHandlerImpl");
            Object impl = t.newInstance();
            DescriptorEventListener listener = (DescriptorEventListener)impl;
            cd.getDescriptorEventManager().addListener(listener);
        }
        catch(Exception e) {
            log.error("Cannot attach audit listener: "+ e.toString(), e);
        }
    }


}
