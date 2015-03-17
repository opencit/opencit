/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author jbuhacoff
 */
public class MyRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyRepository.class);
    private File application; // for example /opt/mtwilson or C:\mtwilson

    public MyRepository(File application) {
        this.application = application;
    }

    /**
     * Get the repository directory for the given feature. For example,
     * getDirectory("privacyca-aik-requests") might return /opt/mtwilson/repository/privacyca-aik-requests
     * 
     * @param featureId
     * @return
     * @throws FileNotFoundException 
     */
    public File getDirectory(String featureId) throws FileNotFoundException {
        File featureDirectory = application.toPath().resolve("repository").resolve(featureId).toFile();
        if (featureDirectory.exists()) {
            return featureDirectory;
        }
        if (featureDirectory.mkdirs()) {
            log.debug("Created directory: {}", featureDirectory.getAbsolutePath());
            return featureDirectory;
        }
        throw new FileNotFoundException(featureDirectory.getAbsolutePath());
    }
}
