/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.tag.TagConfiguration;

/**
 *
 * @author jbuhacoff
 */
public class CreateXmlEncryptionPassword extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateXmlEncryptionPassword.class);
    
    @Override
    protected void configure() throws Exception {
        TagConfiguration tagConfiguration = new TagConfiguration(getConfiguration());
        String password = tagConfiguration.getTagProvisionXmlEncryptionPassword();
        if( password == null || password.isEmpty() ) {
            password = RandomUtil.randomBase64String(16).replace("/", "!").replace("=", "-"); 
            log.info("Generated random password for xml encryption"); 
            getConfiguration().set(TagConfiguration.TAG_PROVISION_XML_ENCRYPTION_PASSWORD, password);
        }
    }

    @Override
    protected void validate() throws Exception {
        TagConfiguration tagConfiguration = new TagConfiguration(getConfiguration());
        String password = tagConfiguration.getTagProvisionXmlEncryptionPassword();
        if( password == null || password.isEmpty() ) {
            validation("XML encryption password is not set");
        }
    }

    @Override
    protected void execute() throws Exception {
        // nothing to do here, this setup task is only configuration
    }
    
}
